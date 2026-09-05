# Flawe 2.0 companion guide

`selector.bin` contains only the objective decision routine (relative offsets
`0xC68` through `0x2723`) from the user-supplied **In-Game Walkthrough 2.0 PPF**.
`FlaweGuideSelector` evaluates a bounded subset of its MIPS arithmetic, loads,
and branches against the existing 10 KiB PAL save-state snapshot. It stops
before the routine's stores. It cannot call game functions or write PSX RAM.

`objectives.json` contains all 157 reachable text pairs, including one empty
entry. English strings and pair IDs are extracted from the same patch.
Spanish, French, German and Italian translations are supplied by the app.
Location names in the latter three translations generally retain the English
guide's proper names to avoid claiming unverified official localized names.

The original guide is by **Flawe**, based on **EmeraldPhoenix's walkthrough**
and **markisha64's quest-system research**, as credited in the supplied patch
readme. This is the main-story guide; it does not add side quests or postgame.

Regenerate from the repository root with Python 3:

```sh
python scripts/extract-flawe-guide.py "path/to/Flawe's Mod - In-Game Walkthrough 2.0.ppf"
```

The extractor symbolically follows branches and delay slots to collect every
text pair. Existing translations are retained only when their English text
matches. The app loads the resulting files once. It does not download a guide
or send game/save data to a translation service.

PAL language codes: 2 English, 3 French, 4 Italian, 5 German, 6 Spanish.
The app's explicit Spanish/English preference overrides automatic selection.
USA remains disabled because this selector uses PAL quest-state addresses.
