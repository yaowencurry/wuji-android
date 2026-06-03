# Biji Android

Native Android client for 吾记. The app mirrors the current iOS workflows with Kotlin, Compose, Room, Retrofit, and a Spannable-backed editor.

## Build

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

The emulator build uses `http://10.0.2.2:8080/` for the Biji API. Start the server from `../server`:

```sh
go run ./cmd/api
```

## Emulator

```sh
emulator -avd biji_api35
./gradlew connectedDebugAndroidTest
```
