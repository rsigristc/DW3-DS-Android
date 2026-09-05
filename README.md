# DW2003 Dual Screen

> v1.0.8 · Android companion for **Digimon World 2003** (PAL `SLES-03936`), Flawe's Mod 2.0 and USA `SLUS-01436`

[![License: GPL v3 or later](https://img.shields.io/badge/License-GPL_v3_or_later-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/rsigristc/DW3-DS-Android)](https://github.com/rsigristc/DW3-DS-Android/releases)

Open-source Android app that runs Digimon World 2003 through PCSX-ReARMed and
shows a live Digivice-style companion on a second display (AYN Thor), a foldable
hinge, or a split pane on tablets.

**The source used to build v1.0.0 and later APKs is visible in this repository.**
CI clones this tree and runs [`scripts/build.sh`](scripts/build.sh).

The app does **not** include a ROM, BIOS or Flawe patch. You must supply your
own legal copy.

## Why this repo looks the way it does

Parts of the companion and the release automation were written with AI
assistance.

| Expectation | Here |
|---|---|
| Source browsable in Git | `dw2003-dual-screen/app`, `libretrodroid`, `cores/pcsx_rearmed` |
| License stated | [LICENSE](LICENSE) (GPL-3.0-or-later, required by LibretroDroid) |
| Upstream credited | [NOTICE](NOTICE), `dw2003-dual-screen/THIRD_PARTY_NOTICES.md` |
| One build command | [`scripts/build.sh`](scripts/build.sh) / [`scripts/build.ps1`](scripts/build.ps1) |
| CI matches local | [`.github/workflows`](.github/workflows) only installs the SDK and calls that script |
| Signing is a secret, not source | Base64 in CI decodes the **keystore** from GitHub Secrets |
| History | `main` keeps the real commit history. POC tags `v0.7.x-poc` were retired after v1.0.0 |

## Features

- Dual-screen / fold / large-display layout with Explore, Battle and Management.
- Live RAM read of Tamer, partners, story stage, equipment and an offline Flawe walkthrough (EN/ES/FR/DE/IT).
- Flawe Fast Travel list of visited ASKMAP icons; signed in-app updates from GitHub Releases.
- Optional quality-of-life cheats, crash log, bilingual companion panel.
- Virtual pad, physical controllers, Memory Card import/export, optional European BIOS.

See [`dw2003-dual-screen/README.md`](dw2003-dual-screen/README.md) for the detailed
Spanish feature list and RAM caveats.

## Building

See [Building from source](docs/BUILDING.md). A local debug build does not
require the private release signing key.

## Releases and verification

Official APKs are published at
[GitHub Releases](https://github.com/rsigristc/DW3-DS-Android/releases).
Each release includes the APK, `SHA256SUMS.txt` and `CERT.txt` (public signing
certificate digests).

Package id: `com.digitaladventure.dw2003`. Uninstall any older
`com.digitaladventure.dw2003.debug` POC before the first official install.

## License and attribution

This combined work is distributed under the **GNU General Public License,
version 3 or later**. See [LICENSE](LICENSE) and [NOTICE](NOTICE).

Digimon and Digimon World belong to their respective owners. This unofficial
fan project is not affiliated with Bandai Namco, AYN or Samsung.

Contributions and device reports are welcome. See
[CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md).
