RhythmExercises
A Jetpack Compose-based rhythm game and metronome prototype for Android, featuring real-time BPM adjustment and low-latency audio feedback using SoundPool.


🎵 Features
Interactive Metronome: Plays distinct audio tones for the accented beat (1st beat) and normal beats (2nd–4th beats).
Real-Time BPM Control: Dynamically alter the tempo (60–200 BPM) on the fly using a UI slider without interrupting playback.
Rhythm Game Mechanics: Practice your timing with tap interactions, featuring real-time hit judgments (PERFECT, GREAT, GOOD, MISS) along with score and combo counters.
Visual Indicator: Smooth pulse animation synchronized with each beat.


🛠️ Tech Stack
Language: Kotlin
UI Framework: Jetpack Compose (Material 3)
Architecture: MVVM with ViewModel and StateFlow
Audio Engine: Android SoundPool for low-latency sound playback
Concurrency: Kotlin Coroutines


📁 Project Structure
app/src/main/

├── java/com/example/rhythmexercises/

│   ├── MainActivity.kt           # UI layouts, ViewModel, and game loop

│   └── MetronomeSoundPlayer.kt   # SoundPool helper for audio playback

└── res/raw/

    ├── tick_high.wav             # Accented beat sound (Beat 1)

    └── tick_low.wav              # Normal beat sound (Beats 2–4)


🚀 Getting Started
Prerequisites
Android Studio: Ladybug (or recommended recent version)
Min SDK: 24 (Android 7.0)
Target SDK: 34 / 35
JDK: 17
Building & Running
Clone the repository:

git clone https://github.com/<your-username>/RhythmExercises.git

Open in Android Studio: Launch Android Studio and select Open, then choose the cloned project directory.
Sync Project with Gradle Files: Let Android Studio download and configure the required dependencies.
Run: Connect a physical Android device or start an emulator, then click Run (Shift + F10).


📜 Credits & Acknowledgments
Audio Assets: The click sound effects (tick_high.wav and tick_low.wav) are provided by Sound Effect Lab (効果音ラボ).


📄 License
Source Code: Distributed under the terms of the GNU General Public License v3.0 or later (GPL-3.0-or-later).
Audio Assets: Subject to the Terms of Use of Sound Effect Lab.
