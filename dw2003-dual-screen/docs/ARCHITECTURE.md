# Arquitectura

## Flujo principal

1. `MainActivity` recibe una URI persistente de Android Storage Access Framework.
2. `GLRetroView` carga `libretro.so` (PCSX-ReARMed) y abre el BIN mediante un descriptor VFS, sin duplicar el archivo.
3. La extensión `LibretroDroid.readMemory()` obtiene una copia validada de una región expuesta por `RETRO_MEMORY_SYSTEM_RAM`.
4. `MemoryPoller` toma muestras cada 350 ms fuera del hilo de interfaz.
5. `GameStateReader` convierte direcciones de PS1 en un `GameSnapshot` inmutable.
6. `GameStateRepository` publica el estado y ambas instancias del panel lo dibujan.

La capa Android nunca recibe un puntero nativo. Toda lectura o escritura verifica ID, offset y longitud bajo el mismo mutex que protege al núcleo. `writeMemory()` existe para reordenar la formación y enviar un destino de viaje; `setCheat()` aplica los códigos de la pestaña Mods.

## Presentación según dispositivo

| Entorno | Juego | Panel complementario |
|---|---|---|
| AYN Thor | Display de la actividad | `Presentation` en el display secundario |
| Galaxy Z Fold desplegado | Primera zona lógica | Segunda zona lógica o lado separado por la bisagra |
| Galaxy Z Fold cerrado | Pantalla exterior completa | Oculto; se conserva el menú de aplicación con Atrás |
| Plegable sin bisagra separadora | Zona lógica principal | Zona lógica secundaria |
| Teléfono estrecho (< 600 dp) | Pantalla completa | Oculto |
| Tablet/pantalla grande | Zona superior o izquierda | Zona inferior o derecha |

`AdaptiveDualPaneLayout` utiliza los límites que entrega `FoldingFeature` cuando la bisagra es separadora. Con una bisagra vertical divide izquierda/derecha; con una horizontal divide arriba/abajo. La pantalla azul de APP permite forzar juego/panel arriba-abajo o izquierda-derecha, o conservar el modo automático. En ausencia de una bisagra útil decide por relación de aspecto y exige al menos 600 dp para mostrar dos zonas. Si el panel ya vive en un display físico secundario, la pantalla principal queda dedicada al juego.

## Modos del panel

- **Exploración:** área/mapa, objetivo, radar regional y estado del Tamer con nombre, Bits y pesca disponible.
- **Batalla:** tarjetas de telemetría HP/MP/EXP/TP y digievolución activa para el equipo.
- **Gestión:** selector de compañero, parámetros, resistencias, equipo y técnicas disponibles con MP.

La versión 0.5 elimina los comandos PlayStation del panel complementario: la entrada táctil permanece exclusivamente sobre el juego. El botón `PAD ON/OFF` del encabezado conmuta esa capa mediante la actividad principal, incluso cuando el panel está en un `Presentation` físico.

La firma del overlay cargado se conserva como telemetría, pero la pestaña elegida por el usuario es persistente y no se reemplaza durante el muestreo de RAM.

## Entrada

Los controles físicos Android se normalizan al esquema RetroPad:

| Android físico | RetroPad / PlayStation |
|---|---|
| A (sur) | B / Cruz |
| B (este) | A / Círculo |
| X (oeste) | Y / Cuadrado |
| Y (norte) | X / Triángulo |

Los botones táctiles envían un pulso de 65 ms. No existen macros que escriban RAM ni secuencias dependientes del estado.

## Guardado

Al pausar, `SaveManager` serializa `RETRO_MEMORY_SAVE_RAM`, conserva una copia de respaldo y reemplaza la tarjeta principal de forma temporal-atómica. La ROM solo se abre en modo lectura.

La versión 0.2 normaliza tarjetas raw/SRM/MCR de 128 KiB y elimina las cabeceras VGS/MEM de 64 bytes o GME de 3904 bytes durante la importación. Antes de cargar una tarjeta importada o restaurada se evita que el núcleo anterior vuelva a sobrescribirla al pausar.

La versión 0.3 superpone `VirtualControllerView` únicamente al panel de juego. Mantiene una barra de utilidades cuando el gamepad se oculta y transmite cada cambio multitáctil como eventos independientes del RetroPad. `QuickStateManager` serializa el núcleo en el hilo de emulación y separa el archivo por SHA-1 de ROM. Antes de publicar formación, `GameStateReader` exige una sesión activa y estadísticas plausibles.

La versión 0.4 conserva la EXP acumulada leída de RAM y calcula el progreso dentro del nivel con umbrales específicos de cada uno de los ocho compañeros. La selección de Gestión solo cambia el panel; no envía controles ni escribe en RAM.

La versión 0.5 instala una tarjeta formateada de 128 KiB antes de iniciar el núcleo si no existe una válida. Los IDs de mapa se resuelven mediante una tabla dispersa hexadecimal y los seis IDs de equipo se enriquecen con nombres y bonificaciones de referencia. Los sprites `drawable-nodpi` se dibujan sin filtrado para conservar el pixel art.

La versión 0.6 empareja cada serialización del núcleo con los 128 KiB de SRAM del mismo instante. Al cargar, restaura ambos y persiste la tarjeta antes de continuar; los estados antiguos sin pareja se rechazan. El lector añade nombre del Tamer, mapa lógico y registros de digievolución, mientras el radar selecciona una imagen por servidor/sector sin afirmar una coordenada exacta.

Tras crear o reanudar el núcleo, `GLRetroView.applyRuntimeOptions()` vuelve a enviar silencio y velocidad al audio nativo. `create()` reinicia esos flags a valores por defecto, y asignar el mismo valor en Kotlin no dispara el `observable`; por eso el botón SONIDO OFF podía quedar desincronizado al cerrar y reabrir el juego.

El menú de viaje del radar lista solo iconos del mapa de Flawe (Ciudad Asuka y Central Park al inicio). «Abrir pestaña Mapa» usa la secuencia corta START → ↓↓ → ×, sin el sondeo de RAM anterior. Un destino mueve la cruceta hasta el icono, confirma con × y envía △ dos veces para salir completamente; □ cambia de servidor. La cruceta captura el puntero que comienza dentro de su círculo y conserva la dirección fuera de sus límites hasta levantar ese dedo.

En 0.7.10, `FlaweDirectWarpPatch` valida el dispatcher que Flawe copia a `0x8000C000`. Solo durante la pulsación × omite la comprobación del cursor y fuerza el ID interno solicitado; el mod sigue resolviendo su propia entrada y spawn. La ventana original se restaura antes de salir del mapa. Si cualquiera de las instrucciones esperadas difiere, la app no parchea RAM y conserva la ruta por cruceta.

## BIOS aportado por el usuario

`BiosManager` acepta únicamente una imagen europea de PlayStation de 512 KiB con firma y marcador regional compatibles con la detección de PCSX-ReARMed. Se instala en el directorio privado del sistema como `scph5502.bin`. Sin ese archivo, el núcleo conserva su alternativa HLE.
