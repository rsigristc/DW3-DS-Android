# Validación de POC 0.7

Fecha de compilación: 1 de septiembre de 2026 (0.7.6-poc).

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

Resultado: `BUILD SUCCESSFUL`.

## Pruebas unitarias

- `GameStateReaderTest`: verifica área/mapa, Tamer, región, digievolución, etapa, firma de batalla, formación y estadísticas, y que la pantalla de título no invente a Kotemon.
- `DwTextDecoderTest`: verifica caracteres latinos, espacios, salto de línea y dígitos del formato de texto europeo.
- `AppFileRulesTest`: verifica tamaños, cabeceras de Memory Card y firma regional del BIOS europeo.
- `PanePolicyTest`: verifica juego completo en una pantalla exterior estrecha, panel dual al desplegar y juego completo cuando existe display secundario.
- `VirtualPadMathTest`: verifica zona muerta, direcciones cardinales y diagonales de la cruceta táctil.
- `QuickStateManagerTest`: verifica que cada clave de ROM produzca un nombre de estado privado, seguro y acotado.
- `ExperienceTableTest`: verifica umbral acumulado, progreso dentro del nivel y tratamiento de nivel 99.
- `TechniqueCatalogTest`: verifica las técnicas características Rookie y el rechazo de perfiles desconocidos.
- `AreaCatalogTest`: verifica IDs reales no consecutivos y puntos de pesca confirmados.
- `EquipmentCatalogTest`: verifica nombre, tipo y bonificaciones de equipo.
- `AppFileRulesTest`: añade validación de la tarjeta formateada generada automáticamente.
- `MapRegionCatalogTest`: verifica servidor/sector para Asuka, Amaterasu y menús externos al mundo.
- `DigievolutionCatalogTest`: verifica nombre, nivel mínimo y MP de técnicas desbloqueadas.
- `TransparencyMaskTest`: verifica que solo el blanco conectado al borde se vuelva transparente.
- `LocationResolverTest`: verifica Posada/Laboratorio frente al hub, Puente Asuka cuando AREA sigue en `0x0200`, y el bloqueo de viaje/reorden.
- `FastTravelCatalogTest`: verifica que solo el mapa actual y los visitados se desbloquean, y que servidores desconocidos siguen cerrados.
- `FastTravelNavigatorTest`: verifica la detección de START por `STSTATUS`, el índice de pestaña tras un R1 y × + △ para confirmar.
- `CheatCatalogTest`: verifica códigos PAL de calidad de vida.
- `GameMemoryControllerTest`: verifica el empaquetado little-endian de la formación.

Resultado: las pruebas unitarias de aplicación deben ejecutarse con `./gradlew :app:testDebugUnitTest`.

## Auditoría del APK

- Bibliotecas nativas: `lib/arm64-v8a/libretro.so` y `lib/arm64-v8a/liblibretrodroid.so`.
- No contiene extensiones de imagen de disco ni BIOS propietario.
- Contiene el icono, sprites y mapas regionales aportados para la interfaz 0.7.
- La única entrada con sufijo `.bin` es `DebugProbesKt.bin`, un recurso interno estándar de Kotlin Coroutines.
- Android Lint: 0 errores y 0 advertencias.

## Pendiente físico

No hay emulador en el entorno de compilación y no se simula la API de múltiples displays del AYN Thor. Antes de llamar estable a la aplicación deben ejecutarse las listas de la Fase 1 en hardware real.

Las rutas de importación/exportación usan Android Storage Access Framework y están cubiertas por validadores puros, pero todavía deben probarse con los proveedores de documentos instalados en los dispositivos finales.

La distribución compacta de tres líneas HP/MP/EXP elimina las coordenadas rígidas que recortaban la última barra de Exploración. La validación final de densidad y legibilidad debe realizarse con las mismas posturas del Fold mostradas en las capturas del usuario.
