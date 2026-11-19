# Fast travel: Flawe 2.0 / Harmony audit

Fast travel changes first tested in the 1.5.0 candidate are included in 1.6.0.

## Verified reference

Source: the Flawe 2.0 STSTATUS module imported and audited by the local Harmony project (`ddw3_development_kit-3/build/current-audit/flawe-mod-20261005`). No game binary is copied into the Android project.

- Module RAM base: `0x80082CB0`.
- Dispatcher: `0x8009BBE4`; reads map server selector `0x8000B214`.
- Selector: 0 = automatic, 1 = Asuka, 2 = Amaterasu.
- Native server detector at `0x8009A670` recognizes Amaterasu field modes `0x0270..0x02D6`.
- Destination table: `0x8009BE70`, 46 records, stride 20. Fields: Asuka u16, Amaterasu u16, icon u32, spawn X u32, spawn Y u32, map-marker coordinates u16/u16.
- Dispatcher reads the first MAP_ID column for Asuka, the second for Amaterasu, preserving its own spawn handling. Shared icon codes alone do not identify a server.
- Examples: City `0x0200 / 0x0270`, Central Park `0x021D / 0x028C`, Seiryu/Qing Long `0x022E / 0x029C`, Suzaku/Zhu Que `0x023E / 0x02AB`, Byakko/Bai Hu `0x025D / 0x02C6`, Genbu/Xuan Wu `0x026F / 0x02D6`.
- Mirage Tower is `0x025A`, not its interior `0x025B`.
- The original table gives Shaman House and Swamp Inn the same Amaterasu MAP_ID `0x02A6`. The native stage/icon map identifies `0x02A6` as Shaman House. The catalog uses that icon once and omits the erroneous Swamp Inn alias.

## Behavior

L2/R2 are available on the touch overlay. Physical triggers accept LTRIGGER/RTRIGGER and BRAKE/GAS. Key and axis sources are merged with hysteresis; pause releases held input. Pocket Companion also accepts BRAKE/GAS.

Travel reads native packed visit flags (`FLAGS_20`, `0x8004B3C0`, length `0x1E`). STSTATUS `0x8009A6B8` checks condition `0x2000 | (fieldMode & 0xFF)`, value 1; EXE `0x800163B0` chooses this bitset and `0x80015590` tests the bit. The stage/icon lookup at `0x8009B5FC` uses zero-based icon indices (low 7 bits) and the server in the high bit. There is no 24-icon or story-stage cutoff. Old speculative bytes at `0x8004B200` are ignored. App-global visit history is not merged into native save flags, preventing another save or old atlas ghosts from unlocking destinations.

Travel lists known visited icons. Field visits normalize interiors to existing hub mappings; map overlays never record hovered icons as visits. Amaterasu IDs are classified separately from Asuka.

Travel opens the normal in-game map and verifies its server selector before temporarily replacing the validated dispatcher input instructions. Amaterasu is accepted only by the paired-table dispatcher, not the older Asuka-only implementation. Restoration checks that the patched code is still present before writing original bytes, because the field overlay may already have replaced STSTATUS.

Arrival requires a field MAP_ID matching the requested destination with no menu overlay active. A hovered icon or stale AREA word cannot report success.

## Device validation pending

Build checks completed: signed app and Pocket Companion APKs, release lint, and 202 app unit tests with zero failures. The signed 1.5.0 APK (versionCode 44) was installed over 1.4.0 on AYN Thor via ADB; the SHA-256 certificate matches the installed version. The activity starts without a fatal exception in the checked log. Visual/gameplay checks were blocked initially because Android reported the device asleep.

AYN Thor checks still require a real custom-pack selection (including L2/R2), a same-server trip and a cross-server trip on a save with visited Amaterasu destinations. No release should be inferred from a successful APK build alone.
