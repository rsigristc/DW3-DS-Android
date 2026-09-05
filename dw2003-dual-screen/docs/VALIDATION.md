# Validación de POC 0.7

Fecha de compilación: 4 de septiembre de 2026 (1.0.0).

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
./scripts/build.sh :app:testDebugUnitTest :app:lintRelease :app:assembleRelease
```

Resultado: `BUILD SUCCESSFUL` (91 pruebas unitarias, 0 fallos). Android Lint: 0 errores; 3 avisos `GradleDependency` preexistentes (no introducidos en 0.7.19).

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
- `AppVersionTest`: verifica que `v1.0.0` sea más nuevo que `0.7.20-poc`, que el JSON público de GitHub elija el APK y que acepte URLs con barras escapadas.
- `DigievolutionCatalogTest`: verifica nombre, nivel mínimo, MP y poder (`Picking Claw` = 60; Double Power/Guard sin poder).
- `TransparencyMaskTest`: verifica que solo el blanco conectado al borde se vuelva transparente.
- `LocationResolverTest`: verifica interiores, puente y Central Park ↔ Entrada del Bosque usando el destino de `MAP_ID`.
- `FastTravelCatalogTest`: verifica que el puente no es un icono, que laboratorio visita cuenta como Ciudad Asuka, que Park y la Entrada del Bosque son iconos distintos, que el norte se desbloquea al visitarlo, que un recorrido Asuka→Seiryu lista los siete iconos visitados y que el Este no finge un orden vertical de cruceta.
- `FlaweFastTravelTableTest`: verifica los 46 códigos ASKMAP del IPS y que Amaterasu no inventa iconos.
- `FastTravelNavigatorTest`: verifica anclaje ↑↑↑↑ + ↓↓ ×, `stepsToMapTab` cuando se conoce la pestaña, □ de servidor, × + △△ y que un solo icono del Este no inventa cruceta.
- `FlaweDirectWarpPatchTest`: verifica firmas, reubicación por referencia `j/jal`, copia única sin thunk, firmas de la función 2.0, rechazo de coincidencias sueltas del renderer y de copias ambiguas.
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
- Incluye `DebugProbesKt.bin` de Kotlin y `assets/guide/selector.bin`: 6844 bytes de la rutina de selección de objetivos de Flawe; no es una imagen de disco ni un BIOS.
- Android Lint: 0 errores. Hay 3 avisos `GradleDependency` de bibliotecas AndroidX (no bloquean y no los toca este corte).

## Pendiente físico

Revisión de rendimiento y Fast Travel (septiembre 2026): el sondeo del panel
publica una sola instantánea y omite cambios de timestamp sin cambios de estado.
La búsqueda de nombres en 256 KiB solo se reintenta tres veces tras cambios de
ubicación/overlay, separadas al menos 600 ms; no se repite en una ubicación
estable ni por faltar un objetivo. `OverlayScanScheduleTest` cubre un minuto
simulado en reposo y transiciones rápidas. El sondeo se detiene en `onStop`.
Esto elimina trabajo periódico identificado en código; no demuestra todavía
una reducción medida de consumo o ventilador en hardware Ayn Thor.

La lista de viajes usa ubicaciones registradas por la app y el mapa resuelto
actual. No importa automáticamente el historial de una partida anterior a la
app. Una selección valida el destino visitado, establece un estado conocido
del menú y espera la llegada antes de informar éxito. El código del dispatcher
puede persistir en RAM: su presencia ya no se interpreta como mapa abierto.
Los destinos Asuka usan el selector de Flawe; Amaterasu conserva selección
manual. No se ha verificado un punto de entrada que omita START; la navegación
se automatiza, pero mantiene el ciclo de carga del menú original.

El marcador del radar señala el sector en el arte del mapa; no representa
coordenadas exactas dentro de una sala. `RadarPositionTest` comprueba el cambio
de sector y la ausencia de una posición inventada para mapas desconocidos.

No hay emulador en el entorno de compilación y no se simula la API de múltiples displays del AYN Thor. Antes de llamar estable a la aplicación deben ejecutarse las listas de la Fase 1 en hardware real.

Las rutas de importación/exportación usan Android Storage Access Framework y están cubiertas por validadores puros, pero todavía deben probarse con los proveedores de documentos instalados en los dispositivos finales.

La distribución compacta de tres líneas HP/MP/EXP elimina las coordenadas rígidas que recortaban la última barra de Exploración. La validación final de densidad y legibilidad debe realizarse con las mismas posturas del Fold mostradas en las capturas del usuario.

Gestión 0.7.16 (lista de formas, mods personalizados, crash log y anclaje de mapa) no puede verificarse en dispositivo en este entorno: no hay ROM ni emulador Android. Las pruebas unitarias cubren el parser de formas, códigos PAL y la secuencia de pad.

## Validación del 5 de septiembre de 2026 (cambios sin publicar)

La compilación Windows usa `W:/dw2003-dual-screen` mediante `subst` para
evitar el límite de rutas de NDK. `:app:testDebugUnitTest`, `:app:lintRelease`
y `:app:assembleRelease` pasan: 150 pruebas, cero fallos.

La guía incluye 157 pares de líneas alcanzables del selector de Flawe, con
traducciones inglés, español, francés, alemán e italiano. Las pruebas cubren
el cambio Repeating Tom → Protocol Ruins dentro de la misma etapa 4, selección
por idioma, cobertura del catálogo y evaluación acotada sin escrituras.
El panel permite desplazar objetivos largos. Los nombres propios de lugares
no están verificados contra cada traducción oficial del juego.

La apertura automatizada del mapa se observó funcionando en español en el
Samsung SM-F971B conectado. El viaje completo con la nueva firma 2.0 y la
invalidación del código recompilado se verificó posteriormente por ADB Wi-Fi:
desde Torre Seiryu se seleccionó Pradera del Viento en la lista del panel,
el menú se cerró y el personaje apareció en destino, con RAM `0x0229`. La
última compilación se instaló posteriormente por ADB Wi-Fi en el SM-F971B
(`install -r --user 0`: Success, versión 1.0.6/código 34). La actividad abrió
correctamente y se pudo cargar el estado guardado de Seiryu Tower. Se verificó
visualmente el objetivo en español «Habla con Repeating Tom en la Torre
Seiryu» sin abrir START, y el botón del panel abrió el mapa localizado.
El viaje a Pradera del Viento conservó ese objetivo, coherente con los
indicadores de misión sin cambios. No se sobrescribió el estado guardado.
Los otros idiomas se validaron mediante pruebas unitarias, no físicamente.
No se ha medido rendimiento ni ventilador en una Ayn Thor.

El diálogo de actualización lee el cuerpo de la release de GitHub. Las pruebas
cubren JSON escapado, Unicode, ausencia de notas y selección del APK. El flujo
de publicación extrae las notas de la versión desde CHANGELOG.md; no se ha
publicado una actualización durante estas pruebas.
