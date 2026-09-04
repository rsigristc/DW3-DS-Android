# Validación de POC 0.7

Fecha de compilación: 4 de septiembre de 2026 (0.7.17-poc).

## Entorno

- JDK Temurin 17.0.20.1
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Android Platform / Build Tools 35
- NDK 27.3.13750724
- CMake 3.22.1
- ABI empaquetada: `arm64-v8a`

## Comandos validados

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Resultado: pendiente de esta revisión (0.7.17-poc).

## Pruebas unitarias

- `GameStateReaderTest`: verifica área/mapa, Tamer, región, formación, lista Monmon NV 5 + Hookmon NV 1, que MP infinito no oculte al primer compañero y que `isFishing` siga en falso sin dump de pesca.
- `DwTextDecoderTest`: verifica caracteres latinos, espacios, salto de línea y dígitos del formato de texto europeo.
- `AppFileRulesTest`: verifica tamaños, cabeceras de Memory Card y firma regional del BIOS europeo.
- `PanePolicyTest`: verifica juego completo en una pantalla exterior estrecha, panel dual al desplegar y juego completo cuando existe display secundario.
- `PaneArrangementTest`: verifica las cinco distribuciones y el fallback automático.
- `VirtualPadMathTest`: verifica zona muerta, diagonales y captura de la cruceta fuera de su círculo.
- `QuickStateManagerTest`: verifica que cada clave de ROM produzca un nombre de estado privado, seguro y acotado.
- `ExperienceTableTest`: verifica umbral acumulado, progreso dentro del nivel y tratamiento de nivel 99.
- `TechniqueCatalogTest`: verifica las técnicas características Rookie, MP/poder de Swing Swing y el rechazo de perfiles desconocidos.
- `TechniquePowerTest`: verifica alias (`Vee Headbutt`/`Pyro Sphere`) y que no se pise el MP del catálogo.
- `PalLanguageTest`: verifica `0x8005CCA8` → inglés (2) / español (6) y que francés o un 0 no cambian AUTO.
- `AreaCatalogTest`: verifica IDs reales no consecutivos, salas SSTNAME de 0.7.14 y puntos de pesca confirmados.
- `EquipmentCatalogTest`: verifica nombre, tipo, parser de bonificaciones (`FUE`, `-VEL`, `FUEGO`, `MÁQ`, `OSC`) y la carga de Evil Fang + Iron Helmet + Glasses.
- `DigimonStateTest`: verifica totales RAM+equipo del demo Guilmon, lista Guilmon/Growlmon y MP/poder Rookie de Monmon.
- `CheatCodeParserTest`: verifica pares PAL `800XXXXX YYYY` y el rechazo de texto vacío.
- `AppFileRulesTest`: añade validación de la tarjeta formateada generada automáticamente.
- `MapRegionCatalogTest`: verifica las listas Wikimon (Lago de Divermon y Bosque Alambre en Este; Entrada del Bosque en Central).
- `AppVersionTest`: verifica que `v0.7.17-poc` sea más nuevo que `0.7.16-poc-debug` y que el JSON público de GitHub elija el APK.
- `DigievolutionCatalogTest`: verifica nombre, nivel mínimo, MP y poder (`Picking Claw` = 60; Double Power/Guard sin poder).
- `TransparencyMaskTest`: verifica que solo el blanco conectado al borde se vuelva transparente.
- `LocationResolverTest`: verifica interiores, puente y Central Park ↔ Entrada del Bosque usando el destino de `MAP_ID`.
- `FastTravelCatalogTest`: verifica que el puente no es un icono, que laboratorio visita cuenta como Ciudad Asuka, que Park y la Entrada del Bosque son iconos distintos, que el norte se desbloquea al visitarlo y que un recorrido Asuka→Seiryu lista los siete iconos visitados.
- `FlaweFastTravelTableTest`: verifica los 46 códigos ASKMAP del IPS y que Amaterasu no inventa iconos.
- `FastTravelNavigatorTest`: verifica anclaje ↑↑↑↑ + ↓↓ ×, `stepsToMapTab` cuando se conoce la pestaña, □ de servidor y × + △△.
- `FlaweDirectWarpPatchTest`: verifica firmas, reubicación por referencia `j/jal`, IDs internos de ASKMAP y rechazo seguro de versiones desconocidas.
- `CheatCatalogTest`: verifica códigos PAL de calidad de vida.
- `GameMemoryControllerTest`: verifica el empaquetado little-endian de la formación.
- `WalkthroughTextFinderTest`: verifica guía DW y ASCII, idioma y rechazo de ruido de menú.
- `WalkthroughCatalogTest`: verifica traducción inglesa conocida, pista española sin etiqueta de Flawe y prompts de sincronización.
- `CompanionUiTextTest`: verifica nombres de mapa, cheats, distribución, idioma del panel y abreviaturas de equipo.

Resultado: las pruebas unitarias de aplicación deben ejecutarse con `./gradlew :app:testDebugUnitTest`.

## Auditoría del APK

- Bibliotecas nativas: `lib/arm64-v8a/libretro.so` y `lib/arm64-v8a/liblibretrodroid.so`.
- No contiene extensiones de imagen de disco ni BIOS propietario.
- Contiene el icono, sprites y mapas regionales aportados para la interfaz 0.7.
- La única entrada con sufijo `.bin` es `DebugProbesKt.bin`, un recurso interno estándar de Kotlin Coroutines.
- Android Lint: 0 errores. Hay 3 avisos `GradleDependency` de bibliotecas AndroidX (no bloquean y no los toca este corte).

## Pendiente físico

No hay emulador en el entorno de compilación y no se simula la API de múltiples displays del AYN Thor. Antes de llamar estable a la aplicación deben ejecutarse las listas de la Fase 1 en hardware real.

Las rutas de importación/exportación usan Android Storage Access Framework y están cubiertas por validadores puros, pero todavía deben probarse con los proveedores de documentos instalados en los dispositivos finales.

La distribución compacta de tres líneas HP/MP/EXP elimina las coordenadas rígidas que recortaban la última barra de Exploración. La validación final de densidad y legibilidad debe realizarse con las mismas posturas del Fold mostradas en las capturas del usuario.

Gestión 0.7.16 (lista de formas, mods personalizados, crash log y anclaje de mapa) no puede verificarse en dispositivo en este entorno: no hay ROM ni emulador Android. Las pruebas unitarias cubren el parser de formas, códigos PAL y la secuencia de pad.
