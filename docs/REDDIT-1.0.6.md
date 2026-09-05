# Android DS companion for Digimon World 2003 (PAL) + Flawe’s Mod 2.0

Hello guys. I brought you something — **v1.0.6 is out**.

I grew up with Digimon World games and wanted to play again without any struggle that this game have, so I built an app using Cursor to reach a better gameplay.

**DW2003 Dual Screen** runs the PAL game (SLES-03936) through PCSX-ReARMed and shows a live companion pane on a second display: Galaxy Z Fold (hinge or split), AYN Thor’s second screen, or a tablet.

The companion reads emulated RAM and updates while you play:
- **Exploration** — area, story stage, walkthrough hint, regional radar, Tamer, Bits
- **Battle** — party HP / MP / EXP, reorder, unlocked digivolutions
- **Management** — stats, elemental resists, skills, equipment
- **Flawe MOD Fast Travel** — list of visited map icons.
- Spanish / English panel, virtual pad, physical controllers, Memory Card import/export
- Default and optional Gameshark cheats.
- In-app updates from GitHub Releases

**What’s new since v1.0.4**
- Offline walkthrough in **English, Spanish, French, German and Italian**. Current Objective follows Flawe’s quest conditions even when the in-game START menu does not show the guide (common in Spanish).
- Fast travel fixes: tap the radar, pick a recorded Asuka destination, and the app starts the trip and checks arrival. “Open Map” is fixed too. I verified Seiryu Tower → Wind Prairie in Spanish on a Galaxy Fold.
- Less repeated RAM work and fewer redundant panel redraws; polling stops in the background. This targets the extra CPU work reported on AYN Thor — I have not measured fan/CPU on a Thor yet, so feedback is welcome.
- Radar marker now follows your current sector (still not exact room coordinates).
- USA (SLUS-01436) memory reads for map / Tamer / Bits / equipment were corrected. Flawe walkthrough and fast travel remain PAL-only.
- The in-app updater now shows the GitHub release notes before downloading.

The APK does **not** include a ROM, BIOS, or Flawe’s patch — you need your own legal copy. Combined Flawe 2.0 is the best match for travel + in-game walkthrough.

Limitations: Amaterasu travel is still manual. The destination list uses visits recorded by the app, not the full history of an older save. FR/DE/IT walkthrough text has automated coverage but still needs player testing.

- Release (APK): https://github.com/rsigristc/DW3-DS-Android/releases/tag/v1.0.6
- Repo: https://github.com/rsigristc/DW3-DS-Android

Please feel free to test it out and give me your comments or feedback. If you report a bug, include device, app version, game region/language and Flawe version. Thor owners: speed setting and underclock profile help a lot.

This mod was possible to built thanks to markisha64 decompilation proyect for DW3/2003.
So all thanks to him! Also thanks to Flawe, EmeraldPhoenix and the modding community for the walkthrough and QoL mods.
