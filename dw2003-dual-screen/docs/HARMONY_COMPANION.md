# Harmony Terriermon v50 companion data

Terriermon support first tested in the 1.5.1 candidate is included in 1.6.0. The release also supports the public Harmony Beta 1.5 (r48) catalog and includes an in-app patch installer.

## Supported revision and detection

The catalog matches the exact Harmony Terriermon v50 BIN supplied for AYN Thor,
SHA-256 `fceaf6bd1e1bf58ec6acd2ef954a0f8712eaff1eb4891e9d64e845a2e706d288`.
Runtime detection compares the installed profile-dispatch hook and the complete
Terriermon profile fingerprint. It does not infer Harmony from the filename or
apply its catalog to stock PAL/USA games. Future Harmony revisions need a fresh
extraction and validation.

The ninth roster member is index 8, but **not** the ninth entry of the stock
save array: Harmony stores its independent 988-byte record at RAM `0x800B1400`.
The unlocked marker is 11; stats begin at record +12. Formation, HP/MP, EXP,
equipment, active form and evolution records are read from there. Reordering
allows index 8 only when the live snapshot identifies Harmony. The EXP curve
matches Agumon's native rate 9 and cap 100.

Evolution IDs: Terriermon `0x6100`, Gargomon `0x6101`, Rapidmon `0x6102`,
MegaGargomon 372, BK MegaGargomon `0x602E`. A saved evolution's level is byte +2;
byte +3 can be Harmony's `0xD6` EXP-curve marker and must not be combined with it.

## Extraction and techniques

`scripts/extract-harmony-companion.py` boots the actual disc EXE in the Harmony
kit's existing Unicorn audit runtime and calls its profile dispatcher. It
follows the **EXE loader file table**, since ISO directory records can still
point at superseded stock resources. It exports factual JSON, not game code or
a ROM: 115 supported profiles and 592 skill assignments, including the 60 added
Harmony forms and the Terriermon line.

Names, learning/mastery levels, signature IDs, status resistance and unlock
conditions are read from that revision. The actual patched PAL MP routine is
evaluated for ordinary/loaded skills and all combinations of the two MP-related
accessories. This includes form-tier discounts and scoped finisher overrides.
Inherited skills use the active form's MP tier. Shown power is the technique's
base potency (including audited scoped overrides), not predicted battle damage.

The panel reads the six saved skill slots and masks their flags to obtain IDs.
It does not invent learned skills merely because a DV level meets a threshold.
Technique details distinguish learned skills with current equipment from the
full future repertoire with accessory-free MP and learning/mastery levels.

Tap the Digivolutions heading to list the partner's Harmony routes; tap a form
or the active-skills card for details. The regular telemetry mapper also sends
Terriermon, his forms and updated skills to Pocket Companion. The portrait is
reused unchanged from the user's Harmony portrait assets.

## Validation

- Signed app and Pocket Companion release APKs build successfully with release lint.
- 207 unit tests pass, including stock parsing regressions, ninth-record isolation,
  revision detection, D6 markers, learned slots, new signatures and MP variants.
- Installed over 1.5.0 on AYN Thor without clearing app data. Created a quick state
  before updating and restored that session afterward.
- Device screenshots confirm three party members, Terriermon's portrait and
  HP/MP, all four evolutions, Petit Twister (28 MP), and the Rapidmon detail with
  Terriermon LV20 / Gargomon DV15 requirements and Rapid Fire at DV70.
