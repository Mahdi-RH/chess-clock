# Chess Clock

A focused Android chess clock built with Kotlin and Jetpack Compose. It provides two face-to-face clocks, common chess time controls, Fischer increment, custom game settings, lifecycle-safe state handling, and process-death restoration.

## Features

- Two large face-to-face player clocks
- Start, pause, resume, and reset controls
- Tap the active clock to complete a move and start the opponent's clock
- Bullet (`1+0`), Blitz (`3+2`), Rapid (`10+0`), and Classical (`30+0`) presets
- Custom base time and Fischer increment for both players
- Move counters and clear active-player feedback
- Tenths-of-a-second display below one minute
- Timeout detection and accessible clock-state descriptions
- State restoration after configuration changes and Android-initiated process death

`3+2` means three minutes of starting time plus a two-second increment after every completed move.

## Architecture

The project uses a lightweight layered architecture with unidirectional data flow:

```text
Compose UI
    |
    | ClockUiAction
    v
ChessClockViewModel
    |
    | ClockAction
    v
ChessClockEngine (pure reducer)
    |
    | ChessGameState
    v
ClockUiStateMapper -> StateFlow<ClockUiState> -> Compose UI
```

### Presentation layer

- Compose components render immutable `ClockUiState` and emit user actions.
- `ChessClockViewModel` coordinates the timer, reducer, UI-state mapping, and state restoration.
- `ClockUiStateMapper` converts domain state into presentation-ready, immutable UI models.
- `collectAsStateWithLifecycle` keeps state collection lifecycle-aware.

### Domain layer

- `ChessGameState`, `TimeControl`, `Player`, and `ClockStatus` model the game.
- `StandardChessClockEngine` is a deterministic reducer that applies start, pause, reset, tick, time-control, and player-clock actions.
- The reducer contains no Android dependencies, which makes the game rules fast and straightforward to unit test.
- `TimeProvider` and `TimeControlProvider` define abstractions used by the ViewModel.

### Data layer

- `BuiltInTimeControlProvider` supplies the predefined time controls.
- `BuiltInTimeControls` owns the built-in Bullet, Blitz, Rapid, and Classical configurations.

### Timing and state restoration

The timer uses `SystemClock.elapsedRealtime()` rather than counting coroutine callbacks. Each update is calculated from a monotonic timestamp, so delayed callbacks do not accumulate clock drift. Elapsed time is also settled immediately before pausing or switching players.

`SavedStateHandle` stores:

- Selected time control, including custom settings
- Remaining time for both players
- Active player and game status
- Move counts
- Last monotonic timing checkpoint

When Android recreates the process, a running game deducts the elapsed background interval and resumes automatically. If the device has rebooted and `elapsedRealtime()` has reset, the saved clock state is restored without incorrectly deducting the old checkpoint.

## Tech Stack

- Kotlin 2.2.10
- Jetpack Compose with Material 3
- Compose BOM 2026.02.01
- Android Architecture Components: ViewModel, `SavedStateHandle`, lifecycle-aware Compose collection
- Kotlin Coroutines and `StateFlow`
- Hilt for dependency injection
- Kotlin immutable collections for stable UI state
- JUnit 4 and `kotlinx-coroutines-test` for local unit tests
- Compose UI Test, AndroidX Test, and Espresso for instrumentation tests
- Gradle Kotlin DSL and version catalogs
- Minimum SDK 24; target SDK 36

## Project Structure

```text
app/src/
├── main/
│   ├── java/com/example/chessclock/
│   │   ├── data/clock/
│   │   │   ├── BuiltInTimeControlProvider.kt
│   │   │   └── BuiltInTimeControls.kt
│   │   ├── di/
│   │   │   └── AppModule.kt
│   │   ├── domain/
│   │   │   ├── clock/
│   │   │   │   ├── engine/       # Pure state transitions and actions
│   │   │   │   ├── model/        # Immutable game models
│   │   │   │   └── provider/     # Time-control abstraction
│   │   │   └── time/             # Monotonic-time abstraction
│   │   ├── presentation/
│   │   │   ├── clock/
│   │   │   │   ├── components/   # Compose screen and reusable UI
│   │   │   │   ├── ChessClockViewModel.kt
│   │   │   │   ├── ClockUiState.kt
│   │   │   │   └── ClockUiStateMapper.kt
│   │   │   └── theme/             # Colors, typography, and dimensions
│   │   ├── ChessClockApplication.kt
│   │   └── MainActivity.kt
│   └── res/                        # Strings, themes, and launcher resources
├── test/                           # Reducer, formatter, mapper, and ViewModel tests
└── androidTest/                    # Compose UI and accessibility tests
```

The application remains a single Gradle module because it contains one focused feature. The package boundaries keep responsibilities separated without adding unnecessary module-level complexity.

## Testing

Local unit tests cover:

- Clock ticking, turn switching, Fischer increment, invalid taps, timeout, and reset
- Time formatting
- Domain-to-UI state mapping
- ViewModel persistence and restoration through `SavedStateHandle`
- Paused and running process-death scenarios
- Monotonic checkpoint behavior after a device reboot

Compose instrumentation tests cover:

- Starting the game activates Player 1
- Tapping the active clock switches players
- The inactive clock cannot change the turn
- Pausing freezes both clocks
- Custom-time validation and selection
- Visual timeout state and accessibility semantics

## Build and Run

Open the project in Android Studio and run the `app` configuration on an API 24+ device or emulator.

## Product Decisions and Limitations

- Both players share one time control, matching standard over-the-board chess clocks.
- Player 1 starts every new game.
- Fischer increment is added after the active player completes a move.
- Presets and custom controls cannot be changed while a game is running.
- `SavedStateHandle` protects against normal Android state recreation and system-initiated process death; it is not durable storage for uninstall, cleared app data, or every explicit force-stop scenario.
- Powered-off time is not deducted across a device reboot because the monotonic clock resets. Handling that case would require an explicit product decision and a wall-clock persistence strategy.
