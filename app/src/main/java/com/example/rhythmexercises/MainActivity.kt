package com.example.rhythmexercises

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RhythmGameScreen()
            }
        }
    }
}

// 判定結果の定義
enum class Judgment(val text: String, val color: Color) {
    PERFECT("PERFECT!!", Color(0xFF4CAF50)),
    GREAT("GREAT!", Color(0xFF2196F3)),
    GOOD("GOOD", Color(0xFFFF9800)),
    MISS("MISS...", Color(0xFFF44336)),
    NONE("-", Color.Gray)
}

// ゲームの状態データ
data class RhythmGameState(
    val isPlaying: Boolean = false,
    val currentBeat: Int = 1,
    val bpm: Int = 120,
    val score: Int = 0,
    val combo: Int = 0,
    val lastJudgment: Judgment = Judgment.NONE,
    val isBeatFlash: Boolean = false // メトロノームの点滅フラグ
)

// ViewModel: 時間管理と判定ロジック
class RhythmGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RhythmGameState())
    val uiState: StateFlow<RhythmGameState> = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var lastBeatTime: Long = 0L

    fun toggleGame() {
        if (_uiState.value.isPlaying) {
            stopGame()
        } else {
            startGame()
        }
    }

    fun setBpm(newBpm: Int) {
        // 40〜240の範囲内に制限（極端な値によるクラッシュを防ぐため）
        val clampedBpm = newBpm.coerceIn(40, 240)
        _uiState.update { it.copy(bpm = clampedBpm) }
    }

    private fun startGame() {
        _uiState.value = RhythmGameState(isPlaying = true, bpm = _uiState.value.bpm)

        var beatCount = 0

        gameJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                lastBeatTime = System.currentTimeMillis()
                beatCount = (beatCount % 4) + 1 // 1 -> 2 -> 3 -> 4 -> 1

                // 拍数更新と点滅ON
                _uiState.update { currentState ->
                    currentState.copy(
                        isBeatFlash = true,
                        currentBeat = beatCount
                    )
                }

                // 100ms後に点滅のみOFFにする非同期処理
                launch {
                    delay(100)
                    _uiState.update { it.copy(isBeatFlash = false) }
                }

                // 最新のBPMから1拍あたりの待機時間を計算
                val currentBpm = _uiState.value.bpm
                val intervalMs = 60_000L / currentBpm

                // 次の拍まで待機
                delay(intervalMs)
            }
        }
    }

    private fun stopGame() {
        gameJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    // タップボタンが押された時の処理
    fun onTap() {
        if (!_uiState.value.isPlaying) return

        val tapTime = System.currentTimeMillis()
        val diff = abs(tapTime - lastBeatTime)

        val judgment = when {
            diff <= 80 -> Judgment.PERFECT
            diff <= 180 -> Judgment.GREAT
            diff <= 300 -> Judgment.GOOD
            else -> Judgment.MISS
        }

        val newCombo = if (judgment == Judgment.MISS) 0 else _uiState.value.combo + 1
        val points = when (judgment) {
            Judgment.PERFECT -> 1000
            Judgment.GREAT -> 500
            Judgment.GOOD -> 200
            Judgment.MISS -> 0
            Judgment.NONE -> 0
        }

        _uiState.value = _uiState.value.copy(
            score = _uiState.value.score + points,
            combo = newCombo,
            lastJudgment = judgment
        )
    }
}

// UI画面
@Composable
fun RhythmGameScreen(viewModel: RhythmGameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. ComposeからContext（画面の情報）を取得
    val context = LocalContext.current

    // 2. SoundPlayerのインスタンスを生成・保持
    val soundPlayer = remember { MetronomeSoundPlayer(context) }

    // 3. 画面が閉じられたときに音源リソースを解放する
    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    // 4. 拍（currentBeat）が更新されるたびに音を鳴らす
    LaunchedEffect(uiState.currentBeat) {
        if (uiState.isPlaying && uiState.currentBeat > 0) {
            // currentBeat (1〜4) を 0〜3 のインデックスに変換して再生
            soundPlayer.playBeat((uiState.currentBeat - 1) % 4)
        }
    }

    val state by viewModel.uiState.collectAsState()

    // メトロノームの点滅色アニメーション
    val beatColor by animateColorAsState(
        targetValue = if (state.isBeatFlash) Color(0xFFFF4081) else Color.DarkGray,
        animationSpec = tween(durationMillis = 80), label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ヘッダー・スコア表示
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Rhythm Exercises", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("SCORE: ${state.score}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("COMBO: ${state.combo}", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "BPM: ${state.bpm}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = state.bpm.toFloat(),
                onValueChange = { newBpm ->
                    viewModel.setBpm(newBpm.toInt())
                },
                valueRange = 60f..200f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // メトロノームのビジュアルインジケーター（円が点滅）
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(beatColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isPlaying) "BPM ${state.bpm}" else "OFF",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // 判定表示
        Text(
            text = state.lastJudgment.text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = state.lastJudgment.color
        )

        // 操作ボタン領域
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // メインのタップボタン
            Button(
                onClick = { viewModel.onTap() },
                enabled = state.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("TAP!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // スタート / ストップボタン
            OutlinedButton(
                onClick = { viewModel.toggleGame() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isPlaying) "Exit" else "Start")
            }
        }
    }
}