# Building from source

Developers and GitHub Actions use the same command.

## Requirements

- JDK 17
- Android SDK Platform 35 and Build Tools 35.0.0
- Android NDK `27.3.13750724`
- CMake 3.22.1

## Debug build

```bash
git clone https://github.com/rsigristc/DW3-DS-Android.git
cd DW3-DS-Android
./scripts/build.sh
```

On Windows:

```powershell
git clone https://github.com/rsigristc/DW3-DS-Android.git
cd DW3-DS-Android
.\scripts\build.ps1
```

The default tasks are `:app:testDebugUnitTest`, `:app:lintDebug` and
`:app:assembleDebug`. Extra Gradle tasks can be passed through:

```bash
./scripts/build.sh :app:assembleRelease
```

The debug APK is written to
`dw2003-dual-screen/app/build/outputs/apk/debug/app-debug.apk`
(`applicationId` `com.digitaladventure.dw2003.debug`).

## Release signing

Local debug builds do not need the project's private release key. Official
releases are signed by GitHub Actions using encrypted repository secrets. The
Base64 step in `.github/workflows/release.yml` only decodes that keystore; it
does not contain source code.

Never commit a keystore or its passwords. Builds signed with a different key
cannot update the official APK in place.

To sign locally, copy `dw2003-dual-screen/keystore.properties.example` to
`dw2003-dual-screen/keystore.properties` and point it at your own store, or
export `DW2003_STORE_FILE`, `DW2003_STORE_PASSWORD`, `DW2003_KEY_ALIAS` and
`DW2003_KEY_PASSWORD`.

## Reviewing the source

There is no overlay, patch archive or hidden tarball. Browse:

- `dw2003-dual-screen/app/src/main/java` — companion UI and RAM readers
- `dw2003-dual-screen/libretrodroid` — vendored Libretro frontend
- `dw2003-dual-screen/cores/pcsx_rearmed` — vendored PlayStation core
