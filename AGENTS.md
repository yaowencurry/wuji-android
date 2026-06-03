# AGENTS.md

## Project Overview

This directory contains the native Android client for Biji ("吾记"), a local-first reading notes app. Treat `android/` as the project root for Gradle commands.

The app uses Kotlin, Jetpack Compose, Room, Retrofit/OkHttp, Coroutines, and an `EditText + Spannable` rich text editor. It targets Android 8.0 and newer (`minSdk = 26`).

## Common Commands

Run these from `android/`:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"

./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug assembleRelease
```

## Git Repository

This directory is the repository root for the Android project.

The remote is:

```sh
git@github.com:yaowencurry/wuji-android.git
```

Keep identity configuration local to this repository. Do not use or modify global Git configuration:

```sh
git config --local user.name "yw.0316"
git config --local user.email "18334310417@163.com"
git config --local --get user.name
git config --local --get user.email
```

Push the current branch with:

```sh
git push -u origin main
```

## Release Build And Upload

The release APK is built from `android/`:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"

./gradlew clean assembleRelease
```

The generated APK is:

```sh
app/build/outputs/apk/release/app-release.apk
```

Release tags should match the app version. For `versionName = "1.0"` and `versionCode = 1`, use:

```sh
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

Upload the APK to GitHub Releases with GitHub CLI when authenticated:

```sh
gh release create v1.0.0 app/build/outputs/apk/release/app-release.apk \
  --repo yaowencurry/wuji-android \
  --title "v1.0.0" \
  --notes "Initial Android release."
```

If the tag already exists, attach or replace the APK on the existing release:

```sh
gh release upload v1.0.0 app/build/outputs/apk/release/app-release.apk \
  --repo yaowencurry/wuji-android \
  --clobber
```

The emulator uses `http://10.0.2.2:8080/` as the default API base URL. Override `BuildConfig.BIJI_API_BASE_URL` only when the task requires another environment.

## Architecture Notes

- `domain/`: models, repository contract, validation, search normalization, and export helpers.
- `data/local/`: Room entities, DAO, and database.
- `data/remote/`: Retrofit API contract and JSON adapters.
- `data/LibraryRepositoryImpl.kt`: local-first orchestration, remote refresh, writes, and rollback.
- `editor/`: cross-platform rich text JSON and the Android `Spannable` editor.
- `upload/`: TOS V4 signing and direct image upload.
- `ui/`: Compose app navigation, screens, theme, and shared visual components.

Keep business behavior out of composables. UI files may format values for display, but validation, persistence, search, export, and rollback belong below `ui/`.

## Product Conventions

The app is a Chinese reading-notes experience. Keep visible copy in Simplified Chinese and preserve the four top-level tabs: 最近、书籍、搜索、设置.

Use the brand green `#2ABB6A` as an accent, not as a large background fill. Prefer warm neutral surfaces, comfortable reading spacing, and clear hierarchy. Dangerous actions must use the semantic danger color.

新增或修改 Android UI 时必须同时适配 `跟随系统`、`浅色`、`深色` 三种外观。优先使用 `BijiTheme.kt` 中的语义色，不要新增只适合浅色背景的硬编码颜色；确实需要固定色时，先确认浅色和深色下文字、边框、卡片、弹窗都有足够对比度。

Core flows must remain local-first. Network failure must not prevent users from reading local books and notes.

## Data Expectations

- Trim user-created text before storage.
- Book titles are normalized with `《》`.
- Soft deletes keep tombstones and remote failures restore local snapshots.
- Notes persist plain summaries, compatibility blocks, and `richTextDocument`.
- Prefer `richTextDocument`, then `contentBlocks`, then plain content when reading legacy notes.
- Long-lived TOS secrets must never be added to the Android app.

## Testing Guidance

Add or update tests when changing repository behavior, export, rich text, signing, Room persistence, or visible navigation landmarks.

Before handing off meaningful changes, run:

```sh
./gradlew test connectedDebugAndroidTest assembleDebug assembleRelease
```

For UI changes, install the Debug APK on an emulator and inspect at least the home screen in both light and dark appearances:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n com.personal.biji.android/.MainActivity
```

## When Editing

Read surrounding files first. Preserve unrelated work. Keep additions scoped to the requested behavior and avoid introducing account, subscription, sync queue, or WeRead import work unless explicitly requested.
