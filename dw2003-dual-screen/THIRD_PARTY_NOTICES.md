# Third-party notices

## Bundled source

### LibretroDroid

- Upstream: `Swordfish90/LibretroDroid`
- Revision vendored: `8835c3098514390a271e36983957f7bb5f40abf1`
- License: GNU GPL v3 or later
- Local changes: defensive libretro memory-read API; NDK 27 include fixes; ARM64-only build configuration.

### PCSX-ReARMed

- Upstream: `libretro/pcsx_rearmed`
- Revision vendored: `ba61a4fdee1f789e8012f205f1b63826667644fa`
- License: GNU GPL v2 or later; distributed in this combined work under GPL v3 or later.

### Oboe

- Bundled as a LibretroDroid dependency.
- License: Apache License 2.0.

### libchdr and other PCSX-ReARMed dependencies

- Bundled in the upstream PCSX-ReARMed tree.
- Individual licenses remain in their source directories; the libchdr BSD notice is also copied to `licenses/`.

## Android dependencies

AndroidX Activity, Core, Lifecycle and WindowManager are resolved through Gradle and licensed under Apache License 2.0. JUnit is used only for tests under Eclipse Public License 1.0.

## Research references

The following public projects informed the reverse engineering and device strategy but are not redistributed as application assets:

- `Xive080/D-W-3-Recomp`
- `markisha64/ddw3`
- `igawa6/ctr-native-android`
- `codm2000/Dual-Screen-Games`
- `jeanheck/digivice` (offsets de perfiles y tabla pública de EXP, basada en la investigación de Mehdi en GameFAQs)

La POC 0.6 utiliza también los catálogos públicos de ubicaciones, equipo, digievoluciones y técnicas de `jeanheck/digivice` para asociar IDs con nombres, bonificaciones y costes de MP.

## Recursos visuales aportados

El icono `DW3 Android Icon`, los sprites PNG de Tamer/compañeros y las imágenes regionales de Asuka/Amaterasu fueron aportados por el usuario para integrarlos en esta POC. Digimon, los personajes, mapas y el arte derivado del juego pertenecen a sus respectivos titulares.

La aplicación no contiene ROM, BIOS, parche ni audio del juego. Los únicos recursos visuales derivados son el icono, los pequeños sprites y los mapas regionales descritos arriba.
