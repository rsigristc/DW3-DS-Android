# Changelog

## 1.0.2

- El listado con scroll de 1.0.1 solo se usa cuando el panel va a un lado (Fold horizontal). Si el juego y el panel están arriba/abajo, se recupera el diseño de columnas para que no quede cortado.

## 1.0.1

- El panel complementario se puede desplazar en Exploración, Batalla y Gestión para que el contenido no quede cortado en foldables horizontales.
- El radar regional conserva la proporción original del mapa y centra el título «Radar Regional · Servidor Asuka».
- Batalla muestra HP, MP, EXP y la lista de digievoluciones (forma activa en azul) en cada carta.
- Gestión deja las columnas comprimidas y pasa a una lista vertical: partner, parámetros, resistencias, habilidades y equipo.

## 1.0.0

- Primera release oficial firmada (`com.digitaladventure.dw2003`). El código, la licencia GPL-3.0-or-later y `scripts/build.sh` están en el árbol público.
- Las etiquetas y APKs `v0.7.x-poc` se retiran. Quien tenga la POC debug debe desinstalarla una vez.

## 0.7.20-poc

- El APK publicado es **release firmado** (`com.digitaladventure.dw2003`), no debug. CI ya no usa el `debug.keystore` efímero que provocaba «App not installed» al actualizar.
- La primera instalación sobre la POC debug exige desinstalar `com.digitaladventure.dw2003.debug`; después el actualizador puede reemplazar la app.

## 0.7.19-poc

- Buscar actualización ya no trata un 404 de GitHub como «ya tienes la última versión». Si el repo es privado o no hay APK, muestra el error real.

## 0.7.18-poc

- El viaje rápido ya no pulsa △ en el campo: eso abría Estado (`Botón Δ : Cerrar Estado`) antes de START.
- La firma directa de Flawe acepta una copia única aunque no haya un `j/jal`, y también un `lw *, 0x184(*)` con un `bne` cercano. Combined 2.0 deja de caer siempre al fallback.
- Si no hay camino 1D de cruceta (todo el Este/Oeste/Norte), no se confirma el icono actual. El mapa se deja abierto para elegir el hexágono a mano.

## 0.7.17-poc

- El radar usa las listas de Wikimon: Lago de Divermon, Bosque Alambre y Pradera del Viento son Sector Este; Central termina en Entrada del Bosque, Playa de Conchas y Cabo Conexión.
- El viaje rápido guarda el icono Flawe de cada visita y lista todos esos destinos (no solo Asuka y Central Park). Si el mapa de Flawe ya está abierto, no lo cierra para reabrirlo.
- ⚙ APP → **Buscar actualización** consulta el último release de GitHub e instala el APK. También avisa al arrancar si hay una versión más nueva.

## 0.7.16-poc

- Gestión lista las digievoluciones desbloqueadas bajo el compañero: nombre a la izquierda, `NV n` a la derecha y la forma activa en azul. La Rookie usa el nivel del partner (Monmon NV 5), no el nivel de habilidad 1.
- Mods permite añadir códigos PAL personalizados (`800XXXXX YYYY`) y borrarlos. Los cuatro mods incluidos se mantienen.
- ⚙ APP muestra **Ver último crash**: un handler guarda hilo, versión, última acción del panel y la traza en `last-crash.log`.
- **Abrir pestaña Mapa** y el viaje rápido ya no asumen Ítems: envían ↑↑↑↑ (ancla en Ítems) y ↓↓ ×. Si el destino está en el otro servidor, pulsan □. Amaterasu sigue sin códigos ASKMAP inventados.
- Pesca: no hay dump comparativo de pie vs. caña. `isFishing` permanece en falso y el sprite se previsualiza tocando el Tamer.

## 0.7.15-poc

- Quita la etiqueta extra de que Flawe no muestra la guía en el menú español. El objetivo conserva la pista del panel o el texto en vivo, sin esa nota.
- Gestión junta parámetros y resistencias en una sola columna (arriba/abajo). Los números suman las bonificaciones del catálogo de equipo: azul si el equipo sube el valor, rojo si lo baja.
- La columna que ocupaban las resistencias pasa a habilidades activas con MP y poder de ataque. Las Rookie dejan de mostrar `MP —` cuando el coste está catalogado.
- El sprite de pesca sigue listo y la demo lo muestra. ddw3 no tiene overlay de pesca (`FIELDSTG` sigue en el campo), así que no se recompila el `.bin`: hace falta un dump de RAM pescando para activar el icono solo.

## 0.7.14-poc

- AUTO lee el índice de idioma PAL en `0x8005CCA8` (ddw3 / parche japonés): 2 inglés europeo, 6 español. Ese byte manda sobre la guía inglesa de Flawe, que ya no fuerza el panel a inglés en una partida española.
- La pestaña Mapa de START se recorre con `stepsToMapTab` (Ítems=0 … Mapa=2 … Estado=4). La lista no envuelve, así que Estado usa ↑↑, no ↓. No se escribe el índice en overlay: la base del widget no es un global estable.
- Catálogo de salas confirmadas por SSTNAME e interpolación entre IDs conocidos: Estadio Digimon, Torre de Prisión, Cuartel A.o.A, Laberinto de Bichos, Sala de Bombeo, mazmorras Kulon, Cámara (`0x026D` en la tabla de posadas) y Magasta/Gunslinger (`0x02DA`–`0x02DF`).
- Amaterasu sigue sin códigos ASKMAP inventados: el IPS solo escribe MAP_ID de Asuka. Esos hubs usan la cruceta.

## 0.7.13-poc

- El dispatcher de Flawe ya no se limita a Ciudad Asuka y Central Park: los 46 iconos de ASKMAP del IPS público (`dmw_2003_patcher`, SLES `0x095500`) se fuerzan con el ID interno real y el spawn del mod.
- La lista de viaje rápido desbloquea esos campos al visitarlos (posadas, pantanos, desierto, montañas del norte, Ciudad Genbu). La Entrada del Bosque Alambre deja de agruparse con Central Park porque Flawe le da icono propio.
- Nombres EN/ES de Montaña de Bota, nieve, hielo, Mina Kulon, Lago de Hielo, Gimnasio Legendario, Agujero Kulon y Ciudad Genbu salen de `ddw3` (`esstname` / `esaskmap`). El Sector Norte empieza en `0x0261`, no en `0x0270`.
- La guía del panel añade pistas por mapa para este y oeste, posadas y el norte. Flawe sigue sin walkthrough en el menú español.

## 0.7.12-poc

- El panel complementario puede mostrarse en español o inglés. ⚙ APP añade **Idioma del panel**: automático, español o English.
- En automático, la guía de Flawe detectada en RAM decide el idioma. La extracción acepta el texto compacto europeo y una copia ASCII, recorre todos los punteros de `0x8000B200` y, al abrir START, busca en el overlay y en los 2 MiB.
- Flawe no publica walkthrough en el menú español. El panel ofrece pistas propias por mapa/etapa y traduce las frases inglesas conocidas, con una nota que lo deja explícito.

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
