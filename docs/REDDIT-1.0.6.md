# Android DS companion for Digimon World 2003 (PAL) + Flawe’s Mod 2.0 — v1.0.6 update

Hey everyone! **v1.0.6 is now available.** Thanks to everyone who tried the app and reported issues, especially with fast travel, the walkthrough in Spanish, and CPU usage on the AYN Thor.

I grew up with the Digimon World games and wanted to revisit DW2003 with some quality-of-life improvements, so I built this Android companion with help from Cursor and Codex.

DW2003 Dual Screen runs the PAL game (`SLES-03936`) through PCSX-ReARMed and displays a live companion pane alongside the game or on a second display. It supports layouts for Galaxy Z Fold devices, tablets, and devices such as the AYN Thor.

**What’s new since v1.0.4?**

- **Walkthrough in five languages:** the companion’s Current Objective panel now supports English, Spanish, French, German and Italian offline. It follows Flawe’s quest conditions even when the selected game language does not show the walkthrough in START. This translates the app panel; it does not change the game’s menus.
- **Fast travel fixes:** tapping the companion map opens a list of destinations recorded as visited. Selecting an Asuka destination starts the travel sequence automatically. The Open Map button has also been fixed. I verified a complete trip from Seiryu Tower to Wind Prairie with the game in Spanish on my Galaxy Fold.
- **Performance changes:** reduced repeated RAM searches and unnecessary panel redraws, and stopped companion polling while the app is in the background. These changes address unnecessary work identified after the Thor CPU/fan report. I haven’t measured the results on an AYN Thor yet, so feedback from Thor owners would be very helpful.
- **Radar improvements:** the marker follows your current sector instead of staying in the middle of the map. It is a sector indicator, not an exact room-position tracker.
- **USA and stability fixes:** corrected several memory readings for Digimon World 3 USA (`SLUS-01436`) and improved handling when the activity closes or is recreated. Flawe’s walkthrough and fast travel remain PAL-only.
- **Update notes:** the in-app updater now shows the release changelog before downloading the APK.

The companion also includes:

- Exploration: location, story stage, current objective, regional radar, Tamer and Bits.
- Battle: party HP/MP/EXP, party ordering and unlocked digivolutions.
- Management: stats, elemental resistances, skills and equipment.
- English/Spanish interface, virtual pad and physical controller support.
- Memory Card import/export, optional GameShark cheats and GitHub in-app updates.

**A few current limitations:** Amaterasu fast travel still requires manual selection. The destination list tracks visits recorded by the app; it does not automatically recover every location visited in an older save. Spanish walkthrough/map/travel were tested on my Fold; the other walkthrough languages have automated coverage but still need player testing.

The APK does **not** include a game ROM, BIOS or a ready-to-apply Flawe patch. Bring your own game and BIOS, and use a compatible Flawe mod for fast travel. Combined Flawe 2.0 is the setup used for this work. The app bundles the objective selector and translated guide text used by its companion panel.

- **Latest APK / release notes:** https://github.com/rsigristc/DW3-DS-Android/releases/tag/v1.0.6
- **Source code:** https://github.com/rsigristc/DW3-DS-Android

If you try it, please share your device, app version, game region/language and mod version with any bug report. For Thor performance feedback, including your speed setting and underclock profile would help compare results.

Huge thanks to **markisha64** for the DW3/2003 decompilation and tooling, and to **Flawe, EmeraldPhoenix and the modding community** for the work behind the walkthrough and quality-of-life mods.
