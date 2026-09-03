# Changelog

## 0.7.11-poc

- Si Flawe no instala su dispatcher en `0x8000C000`, busca la firma en los 2 MiB de RAM y selecciona únicamente la copia referenciada por un salto `j/jal` del overlay.
- Tolera que cambie el desplazamiento del `bne` de validación entre compilaciones, pero conserva la validación de registros e instrucciones de lectura.
- Mantiene la cruceta como fallback si no existe una única copia activa.

## 0.7.10-poc

- Selección directa de Ciudad Asuka (`0x14`) y Central Park (`0x1E`) mediante el dispatcher de Flawe en `0x8000C000`; el propio mod conserva su tabla de mapa y spawn.
- Antes de escribir se validan tres instrucciones exactas del dispatcher. La modificación solo permanece durante × y después se restaura byte por byte.
- Si la firma de Flawe no coincide, el viaje conserva automáticamente la selección mediante cruceta de 0.7.9.

## 0.7.9-poc

- Flawe recibe ↑/↓ para cambiar entre Ciudad Asuka y Central Park, espera antes de × y sale completamente con △. L1/R1 no seleccionan iconos; □ cambia de servidor.
- MP/HP infinito ya no elimina al primer compañero de la segunda pantalla: los valores temporales superiores al máximo se limitan solo para la telemetría.
- Elimina de Mods **Animaciones rápidas**, **Dithering 2D/3D off** y **Silenciar música**.
- Restaura en la pantalla azul de APP la opción **Distribución de pantallas**: automático, juego/panel arriba o abajo, y juego/panel a izquierda o derecha.

## 0.7.8-poc

- Central Park ↔ Entrada del Bosque Alambre usa `MAP_ID`, que ya contiene el destino mientras `AREA` conserva temporalmente el mapa anterior.
- Se elimina el sondeo lento del menú: **Abrir pestaña Mapa** envía START → ↓↓ → × con pulsaciones cortas. No existe una entrada directa documentada al mapa de Flawe.
- La cruceta virtual captura el dedo desde que comienza dentro de ella y mantiene la marcha aunque se deslice fuera del círculo, hasta soltarlo.
- El icono actual del menú de viaje también se resuelve desde `MAP_ID`, evitando resaltar el mapa anterior durante una transición.

## 0.7.7-poc

- Al volver a Ciudad Asuka, si `MAP_ID` se queda en un interior (Salón `0x0203`), el título usa `AREA` del hub.
- **Abrir pestaña Mapa** recorre START con la cruceta (Ítems → Ordenar → Mapa) y pulsa ×. L1/R1 no mueven esa lista; solo ciclan iconos ya dentro del mapa de Flawe.
- Elegir un destino abre ese mapa, mueve L1/R1 al icono y confirma con × + △. Ya no escribe IDs en RAM (eso cambiaba el mapa y dejaba el spawn mal).
- La lista de viaje rápido solo muestra iconos del mapa de Flawe. El puente y las salas cuentan como Ciudad Asuka; al inicio: Ciudad Asuka y Central Park.

## 0.7.6-poc

- El título sigue el escenario del overlay del juego (Posada, Salón, Puente Asuka). `AREA` suele quedarse en el hub `0x0200`; `MAP_ID` es la sala o el puente. Ya no se fuerza Ciudad Asuka.
- El menú START se detecta por la firma `STSTATUS`, no por `0x1000` en el ID de área (esa palabra no cambia al abrir START).
- Abrir pestaña Mapa sondea un R1, localiza el índice de pestaña y camina hasta MAPA desde la pestaña actual, no asume Ítems ni Estado.
- Elegir un destino escribe el ID, confirma con × en MAPA y cierra con △.

## 0.7.5-poc

- El título y el radar siguen el ID de área en vivo (`0x80048D68`). `0x8004B3F8` a menudo se queda en el hub o en el mapa anterior, así que ya no se usa como nombre público salvo en interiores de ciudad.
- **Abrir pestaña Mapa** espera a que el overlay `0x1000` esté listo y envía dos R1 (Ítems → Ordenar → Mapa). Dos L1 desde Ítems acababan en Estado, el menú principal de START.
- Elegir un destino ya no pulsa × (eso confirma el icono con foco y spawnea en tiles inaccesibles). Tras la carga se cierra el menú si sigue abierto, para no quedar en Artículos.
- La lista de viaje rápido solo ofrece iconos de Flawe ya visitados (ciudades, parque, playa, estaciones), no salas interiores.

## 0.7.4-poc

- **Abrir pestaña Mapa** envía START y dos veces L1 (STATUS → TÉCNICAS → MAPA), no solo el menú de estado.
- Elegir un destino abre esa pestaña, escribe el ID y pulsa × + △ para marcar y salir del menú, que es cuando Flawe dispara la pantalla de carga.

## 0.7.3-poc

- Reaplica silencio y velocidad 2× al núcleo nativo al crear, reanudar o recargar la sesión, para que SONIDO OFF coincida con el audio real.
- El mapa regional abre un menú de viaje rápido solo con localidades ya visitadas e incluye **Abrir mapa del juego** (START).
- Publica el APK de depuración `DW2003-Dual-Screen-v0.7.3-poc-debug.apk` mediante el flujo etiquetado de GitHub.

## 0.7.0-poc

- El título del panel usa el nombre público del mapa (p. ej. Ciudad Asuka) cuando el ID de sala es un interior del hub, para coincidir con la pantalla de carga. El subtítulo y el radar muestran el mismo par Sector + mapa; ya no se pinta el nombre interno de sala (Laboratorio Digimon) en la esquina superior izquierda.
- Aplica la máscara de transparencia al sprite del Tamer, no solo a los compañeros.
- ⚙ APP abre la ventana de configuración inicial, con aviso de BIOS y un interruptor que muestra u oculta la pestaña Mods.
- El mapa regional abre un menú de viaje rápido por servidor, sector y destinos ya visitados o liberados.
- La pestaña Batalla permite reordenar la formación activa solo fuera de combate y de eventos.
- Los mods opcionales aplican códigos PAL (bits, encuentros, dithering, HP/MP de batalla) mediante `retro_cheat_set`.
- Evita serializar la Memory Card durante `0x0C01`, desactiva el hilo de GPU y avisa si se usa BIOS HLE.

## 0.6.0-poc

- Empareja cada estado rápido con una copia de SRAM/Memory Card y la persiste al cargarlo.
- Rechaza estados 0.5 sin tarjeta asociada y bloquea capturas durante la pantalla de guardado del juego.
- Mantiene indefinidamente la pestaña elegida, incluso al recrear el panel.
- Añade nombre del Tamer, nombre/ID de mapa lógico, servidor y sector leídos o derivados de RAM.
- Sustituye el radar ficticio por los mapas regionales Asuka y Amaterasu aportados.
- Añade digievolución activa, nivel de habilidad, técnicas aprendidas y coste de MP.
- Elimina el fondo blanco de los sprites con una máscara limitada al fondo conectado al borde.
- Amplía el catálogo de nombres de Amaterasu y la cobertura de pruebas.

## 0.5.0-poc

- Usa `DW3 Android Icon` como icono de la aplicación.
- Añade sprites transparentes de los ocho compañeros y del Tamer.
- Sustituye Formación Activa de Exploración por estado del Tamer, actividad, pesca disponible y Bits.
- Corrige los nombres de áreas usando IDs hexadecimales reales no consecutivos.
- Añade nombres y bonificaciones de las seis piezas equipadas en Gestión.
- Instala automáticamente una Memory Card PlayStation formateada de 128 KiB y preserva archivos inválidos.
- Elimina los cuatro botones PlayStation del panel complementario y recupera su espacio.
- Añade `PAD ON/OFF` junto al botón de aplicación para controlar el gamepad principal.
- Amplía la validación a quince pruebas unitarias.

## 0.4.0-poc

- Corrige el recorte de la barra de MP en la formación de Exploración.
- Añade etiquetas y barras compactas de HP, MP y EXP acumulada/requerida.
- Añade EXP actual, umbral del próximo nivel y EXP restante en Batalla y Gestión.
- Añade selector circular de compañero en Gestión sin modificar la formación del juego.
- Muestra parámetros y siete resistencias elementales leídas de RAM.
- Muestra la técnica característica confirmada de la forma Rookie seleccionada.
- Amplía la lectura de equipo a sus seis ranuras y la validación a once pruebas unitarias.

## 0.3.0-poc

- Añade gamepad virtual multitáctil con cruceta diagonal, botones frontales, L1/R1, Start y Select.
- Añade guardado y carga rápida por ROM mediante la serialización nativa del núcleo.
- Añade velocidad conmutable 1×/2× y activación/desactivación de audio.
- Permite ocultar el gamepad desde `⚙ APP` sin perder la barra de acciones rápidas.
- Corrige el Kotemon ficticio en las pantallas de idioma/título validando primero una sesión activa.
- Añade una vista explícita de “Partida aún no iniciada”.
- Amplía la validación a ocho pruebas unitarias.

## 0.2.0-poc

- Añade modo automático de una pantalla para el Fold cerrado y panel dual desde 600 dp.
- Respeta bisagras separadoras verticales y horizontales mediante Jetpack WindowManager.
- Añade el menú `⚙ APP` y acceso alternativo con Atrás de Android.
- Permite cambiar la imagen BIN sin borrar los datos de la aplicación.
- Permite importar un BIOS europeo de PlayStation de 512 KiB aportado por el usuario.
- Permite importar y exportar Memory Card raw/SRM/MCR, VGS/MEM y GME.
- Añade restauración protegida del respaldo automático de la Memory Card.
- Amplía la validación a cinco pruebas unitarias.

## 0.1.0-poc

- Primera integración Android ARM64 con PCSX-ReARMed y LibretroDroid.
- Panel complementario para AYN Thor, plegables y pantallas grandes.
- Lectura experimental del estado de Digimon World 2003 desde RAM emulada.
- Modos Exploración, Batalla y Gestión.
