# Security and release integrity

## Source and build

The application source, LibretroDroid frontend and PCSX-ReARMed core are stored
as ordinary files in this repository. GitHub Actions checks out this tree and
runs `scripts/build.sh`. The workflow does not decode or reconstruct source
code from an archive.

The only Base64 decode in CI writes the **release keystore** from an encrypted
GitHub Actions secret into a runner temp file. That secret is a signing key,
not application source. It is never committed.

Official debug builds do not need that key. Contributors can compile locally
without access to release secrets.

## APK authenticity

Official APKs are signed with a persistent Android release key. Android rejects
an update if the incoming APK uses a different certificate. Each GitHub Release
includes:

- the signed APK
- `SHA256SUMS.txt` for the APK bytes
- `CERT.txt` with the public signing-certificate digests from `apksigner`

Because the APK and checksum are hosted together, the Android signing
certificate—not the checksum alone—is the authenticity boundary.

## Updater behavior

- Release metadata and APKs are fetched over HTTPS from GitHub Releases.
- Downloads stay in the app-private cache.
- Installation is performed by Android's package installer; the app does not
  silently replace itself.
- The first official release (`v1.0.0`) uses package `com.digitaladventure.dw2003`.
  Earlier POC debug builds used `com.digitaladventure.dw2003.debug` and must be
  uninstalled once.

## What not to report in public

Do not publish keystores, passwords, BIOS images, ROM files or Memory Card
dumps. Open a minimal GitHub issue requesting a private contact channel and
include only the affected version and a short non-sensitive summary.
