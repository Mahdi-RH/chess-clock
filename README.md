# Chess Clock

A focused Android chess clock.

## Features

- Two large face-to-face player clocks
- Start, pause, resume, and reset controls
- Tap the active clock to end a turn and start the opponent's clock
- Bullet (1+0), Blitz (3+2), Rapid (10+0), and Classical (30+0) presets
- Custom base time and Fischer increment for both players
- Move counters, active-player feedback, tenths below one minute, and timeout state

`3+2` means three minutes of starting time plus two seconds after each completed move.

## Technical approach

The app uses unidirectional state flow:

`Compose UI -> user action -> ViewModel -> pure reducer -> StateFlow -> Compose UI`

- `ChessClock.kt` contains immutable models and deterministic state transitions.
- `ChessClockViewModel.kt` owns Android lifecycle and monotonic elapsed-time measurement.
- `ChessClockScreen.kt` renders state and sends user intent back to the ViewModel.
- Reducer unit tests cover ticking, turn switching, increments, invalid taps, timeout, and reset.

The clock uses `SystemClock.elapsedRealtime()` instead of counting timer callbacks. Callback delays therefore do not accumulate drift, and elapsed time is settled immediately at pause/turn boundaries.

## Run and verify

Open the project in Android Studio and run the `app` configuration on an API 24+ device or emulator.

From PowerShell, with `JAVA_HOME` pointing to a compatible JDK:

## Scope decisions

- Both players share one time control, matching standard over-the-board chess clocks and the assignment wording.
- Player 1 starts; either side can be supported later by adding a pre-game first-player choice.
- State survives configuration changes through the ViewModel. Persistence across process death was intentionally left out because a dependable background clock would need explicit product decisions around notifications and foreground-service behavior.
