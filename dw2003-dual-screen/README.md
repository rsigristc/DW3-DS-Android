# DW2003 Dual Screen

Prueba de concepto Android para ejecutar **Digimon World 2003 (Europa)** y aprovechar dos zonas de pantalla en:

- AYN Thor y otros equipos Android con una pantalla secundaria expuesta por `DisplayManager`.
- Samsung Galaxy Z Fold y otros plegables compatibles con Jetpack WindowManager.
- Tablets o pantallas grandes, con división lógica adaptable.

La aplicación incluye el núcleo PCSX-ReARMed, una capa Android basada en LibretroDroid y un panel complementario que lee el estado del juego desde la RAM emulada. **No incluye ROM, BIOS ni parches.** La POC 0.7 incorpora los sprites y mapas aportados por el usuario, corrige el nombre de ubicación frente a la pantalla de carga y añade viaje rápido, reorden de equipo y una pestaña de mods opcional.

## Estado de la POC 0.7

Implementado y compilado:

- Ejecución de una imagen `.bin` elegida con el selector seguro de Android.
- Verificación SHA-1 de las dos imágenes conocidas.
- Pantalla principal para el juego y `Presentation` independiente para la segunda pantalla del AYN Thor.
- División por bisagra o división lógica en Galaxy Z Fold y pantallas grandes.
- Tres vistas contextuales: Exploración, Batalla y Gestión.
- Lectura de área, etapa de historia, formación activa y perfiles de los ocho compañeros.
- Lectura de nivel, EXP, TP, HP, MP, parámetros, siete resistencias elementales y seis ranuras de equipo.
- Barras etiquetadas de HP/MP/EXP en Exploración y EXP acumulada, requerida y restante en Batalla.
- Selector de compañero en Gestión, con parámetros, resistencias y técnica característica de la forma Rookie.
- Icono Android personalizado y retratos pixel art transparentes en Batalla y Gestión.
- Exploración rediseñada con estado del Tamer, actividad, disponibilidad de pesca y Bits leídos de RAM.
- Catálogo de mapas por ID hexadecimal real, incluidos Ciudad Asuka (`0x0200`), Laboratorio Digimon (`0x0206`) y Centro Online (`0x02D8`).
- Nombres y bonificaciones de las seis piezas equipadas por cada compañero.
- Memory Card PlayStation formateada e instalada automáticamente desde el primer arranque.
- Botón `PAD ON/OFF` junto a `APP` para ocultar o mostrar el gamepad de la pantalla principal.
- Panel complementario sin botones PlayStation redundantes; todo el espacio inferior queda para la telemetría.
- Detección automática de los overlays de batalla y gestión del mod combinado.
- Lectura experimental del objetivo que expone el walkthrough integrado.
- Botones táctiles seguros para confirmar, volver, menú y acción.
- Gamepad virtual multitáctil sobre el juego: cruceta con diagonales, botones frontales, L1/R1, Start y Select.
- Barra rápida disponible con el gamepad visible u oculto: guardar/cargar estado, velocidad 1×/2× y sonido on/off.
- Estados rápidos privados y separados por SHA-1 de la ROM para evitar cargas cruzadas.
- Cada estado rápido guarda y restaura su propia Memory Card de 128 KiB; los estados 0.5 sin tarjeta emparejada se rechazan para evitar incoherencias.
- Pestaña seleccionada persistente: la telemetría ya no vuelve automáticamente a Exploración.
- Lectura del nombre del Tamer, ID/nombre de área y mapa lógico desde RAM.
- Radar regional con mapas Asuka/Amaterasu y selección por servidor y sector; el marcador central no pretende ser una coordenada exacta.
- Lectura de la digievolución activa, su nivel de habilidad y hasta tres técnicas aprendidas con su coste de MP conocido.
- Máscara de transparencia por borde para limpiar el fondo blanco del Tamer y de los compañeros sin eliminar detalles blancos interiores del pixel art.
- Título de ubicación sincronizado con `MAP_ID`, que cambia al destino antes que `AREA` durante las transiciones.
- ⚙ APP reabre la pantalla de configuración inicial y puede activar una pestaña Mods en el panel complementario.
- La pantalla azul de APP permite intercambiar juego/panel arriba-abajo o izquierda-derecha, además del modo automático.
- Viaje rápido con los 46 iconos ASKMAP de Flawe en Asuka (ciudades, campo, norte) más los hubs de Amaterasu; **Abrir pestaña Mapa** usa START → ↓↓ → ×.
- La lista localiza el dispatcher activo de Flawe por firma y referencia, fuerza temporalmente el ID interno del icono y conserva el spawn definido por el mod.
- Reorden de la formación activa desde Batalla fuera de combate y de eventos.
- Estado de espera sin compañeros ficticios antes de iniciar o cargar una partida.
- Mandos físicos, cruceta y sticks analógicos dirigidos al puerto 1.
- Tarjeta de memoria automática con respaldo local.
- Importación y exportación de Memory Card en formatos raw/SRM/MCR, VGS/MEM y GME.
- Restauración del respaldo automático sin que la sesión anterior lo sobrescriba.
- Importación opcional de un BIOS europeo de 512 KiB, validado antes de instalarlo.
- Menú de aplicación para cambiar BIN y gestionar BIOS/partidas sin borrar datos.
- Modo automático de una pantalla en el Fold cerrado y panel dual al desplegarlo.
- Modo demostración sin ROM.

La compilación, las pruebas unitarias y Android Lint pasan. Aún se requiere validación práctica en un AYN Thor y un Galaxy Z Fold para ajustar identificadores de pantalla, densidad, audio y distribución exacta.

## ROM verificadas

| Variante | SHA-1 | Tamaño |
|---|---|---:|
| SLES-03936 Europa original | `457cb233349ba841e03b33d8060f8fbcadd45cb3` | 692,146,560 bytes |
| Flawe's Mod 2.0 combinado | `5277dfd1b7b6b237ea93bfca2723c9b4baaa75d1` | 692,489,952 bytes |

La POC permite continuar con otra imagen, pero desactiva cualquier garantía sobre la interpretación de RAM. Para Flawe's Mod se recomienda el parche combinado, ya que contiene Fast Travel, Battle Speed-Up e In-game Walkthrough en los módulos esperados.

## Uso

1. Instala `DW2003-Dual-Screen-v0.7.15-poc-debug.apk` (release `v0.7.15-poc`) en un dispositivo Android ARM64.
2. Abre la aplicación y pulsa **Seleccionar BIN**.
3. Elige tu copia personal ya parcheada o la imagen original europea.
4. En AYN Thor, la aplicación moverá automáticamente el panel complementario a la segunda pantalla disponible.
5. En un Fold desplegado, el juego y el panel se reparten según la bisagra; si Android no reporta una bisagra separadora, se aplica una división lógica.
6. Abre una vez el menú del juego para que el mod actualice los punteros del walkthrough. En inglés Flawe muestra la guía en START; el panel la copia cuando esos punteros o el texto ASCII están en RAM.
7. En español, AUTO lee el idioma PAL en `0x8005CCA8` (6 = español) para no copiar el inglés de Flawe. El panel muestra la pista en español sin una nota extra sobre el menú del mod. Puedes forzar el idioma en ⚙ APP → **Idioma del panel**.
8. Usa la barra superior del juego para guardar/cargar estado, activar 2× o silenciar. Pulsa **⚙ APP** —o Atrás de Android cuando solo se vea el juego— para volver a la configuración inicial, activar Mods, cambiar BIN o gestionar BIOS y Memory Card.
9. Para guardar partidas dentro del juego, importa un BIOS europeo. El BIOS HLE de PCSX-ReARMed puede quedarse en «Comprobando la Tarjeta de Memoria».

La aplicación conserva permiso de lectura del archivo mediante Storage Access Framework; no copia la imagen de aproximadamente 700 MB al almacenamiento interno.

## Limitaciones conocidas

- El radar selecciona una imagen regional real según servidor/sector y muestra el nombre del mapa local. La coordenada exacta del jugador dentro de esa imagen todavía no se extrae de RAM.
- Flawe no expone una entrada directa documentada a su mapa; la app abre START y camina ↑/↓ hasta la pestaña Mapa (por defecto desde Ítems: ↓↓ → ×). No escribe el índice del widget.
- En Flawe 2.0 la lista prueba `0x8000C000` y, si difiere, busca una copia reubicada referenciada por el overlay. Fuerza el icono interno solo durante × y lo restaura antes de salir con △△. Si no hay una firma activa única, vuelve a la cruceta.
- El objetivo en inglés depende de Flawe: punteros en `0x8000B200`, texto ASCII en scratch/overlay, o un barrido al abrir START. En español el panel muestra pistas propias (sin etiqueta extra sobre Flawe).
- La detección automática de Batalla/Gestión usa firmas del mod combinado. La imagen original continúa funcionando, pero puede requerir firmas adicionales.
- La POC abre una imagen BIN individual. El soporte formal para CUE multitrack y CHD queda para una fase posterior.
- El núcleo puede usar su BIOS HLE. Si el usuario importa su propio BIOS europeo, la aplicación lo valida y lo guarda de forma privada como `scph5502.bin`; nunca se distribuye uno.
- La división automática considera pantalla amplia a partir de 600 dp. En la pantalla exterior estrecha de un Fold se prioriza el juego a pantalla completa.
- La velocidad 2× depende del margen térmico y de rendimiento del dispositivo; puede ser menor bajo carga sostenida.
- Las técnicas activas muestran MP y poder cuando el catálogo los tiene. Las de apoyo (cura, campos, Double Power/Guard) dejan el poder en `—`.
- El sprite de pesca está integrado y puede previsualizarse tocando el panel del Tamer en un punto de pesca conocido. ddw3 no publica un overlay de pesca; sin un dump de RAM mientras se pesca, `isFishing` permanece en falso.
- Los nombres del equipo corresponden al catálogo inglés de referencia; parámetros y bonificaciones sí se muestran con abreviaturas españolas.

## Compilación

Requisitos:

- JDK 17 completo.
- Android SDK Platform 35 y Build Tools 35.0.0.
- Android NDK `27.3.13750724`.
- CMake 3.22.1.

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk` y solo empaqueta bibliotecas `arm64-v8a`.

## Estructura

- `app/`: actividad, distribución adaptable, panel Digivice y lectores de RAM.
- `libretrodroid/`: frontend libretro vendorizado, con una extensión de lectura defensiva de memoria.
- `cores/pcsx_rearmed/`: núcleo PlayStation vendorizado.
- `docs/`: arquitectura, mapa de RAM, validación y próximas fases.

## Legal y licencia

Este proyecto fan no está afiliado con Bandai Namco, AYN ni Samsung. Digimon y Digimon World son marcas de sus respectivos titulares. Debes aportar tu propia copia legal del juego.

El código se distribuye bajo GPL-3.0-or-later debido a LibretroDroid. PCSX-ReARMed permite GPL-2.0-or-later y se distribuye aquí bajo los términos compatibles de GPL-3.0-or-later. Consulta `LICENSE`, `THIRD_PARTY_NOTICES.md` y `licenses/`.
