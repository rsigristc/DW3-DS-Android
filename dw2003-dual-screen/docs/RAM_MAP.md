# Mapa de RAM usado por la POC

Las direcciones son virtuales de PS1. Para `RETRO_MEMORY_SYSTEM_RAM`, la aplicación usa `address & 0x1FFFFF`.

## Estado global

| Dirección | Tipo | Uso |
|---:|---|---|
| `0x80048D68` | `u16` | ID de área/escenario |
| `0x80048D88` | cadena DW | Nombre del Tamer (máximo 10 caracteres útiles) |
| `0x80048DA0` | `u32` | Bits actuales del Tamer |
| `0x80048DA4` | `u32` | Perfil activo 1 |
| `0x80048DA8` | `u32` | Perfil activo 2 |
| `0x80048DAC` | `u32` | Perfil activo 3 |
| `0x8004B370` | `u16` | Etapa de historia |
| `0x8004B3F8` | `u16` | ID del mapa lógico actual |
| `0x8005CCA8` | `u32` | Idioma PAL: 0 japonés, 1 inglés US, 2 inglés europeo, 3 francés, 4 italiano, 5 alemán, 6 español |
| `0x80080000` | `u32` | Firma inicial del overlay cargado |

Firmas observadas en Flawe's Mod 2.0 combinado:

- `0x80085390`: overlay `FIGHTST2`, vista Batalla.
- `0x8008428C`: overlay `STSTATUS`, vista Gestión.
- cualquier otra firma: vista Exploración.

## Perfiles de compañero

Base `0x8004949C`, ocho perfiles, stride `0x3DC`.

| Offset | Tipo | Campo |
|---:|---|---|
| `+0x18` | `u32` | Experiencia |
| `+0x1C` | `u16` | Nivel |
| `+0x1E` | `u16` | TP |
| `+0x20` | `u16` | HP actual |
| `+0x22` | `u16` | HP máximo |
| `+0x24` | `u16` | MP actual |
| `+0x26` | `u16` | MP máximo |
| `+0x28` | `u16` | Fuerza |
| `+0x2A` | `u16` | Defensa |
| `+0x2C` | `u16` | Espíritu |
| `+0x2E` | `u16` | Sabiduría |
| `+0x30` | `u16` | Velocidad |
| `+0x32` | `u16` | Carisma |
| `+0x34…+0x40` | `7 × u16` | Resistencias: fuego, agua, hielo, viento, trueno, máquina y oscuridad |
| `+0x3C0…+0x3CA` | `6 × u16` | IDs de equipo: cabeza, cuerpo, mano derecha, mano izquierda y dos accesorios |
| `-0x04` | `u16` | ID de digievolución activa; `0/FFFF` representa forma Rookie |
| `+0x50 + n×0x14` | registro | Digievolución desbloqueada: ID `+0`, nivel `+2`, DVXP `+4` |

Gestión 0.7.15 trata `+0x28…+0x40` como base de RAM y suma las bonificaciones parseadas del catálogo de equipo. El color azul/rojo sale del signo de esa suma, no de otro campo de RAM. En 0.7.16 la lista de formas antepone la Rookie (`ID 0`, nivel `+0x1C`) y recorre hasta 44 registros `+0x50`; el nivel de Champion/Ultimate es el de habilidad (`+2`), no el del partner.

Orden de perfiles: Kotemon, Kumamon, Monmon, Agumon, Veemon, Guilmon, Renamon y Patamon.

Los IDs de área se presentan como hexadecimales (`0x0200`, `0x0206`, `0x0261`, `0x02D8`, etc.). No deben usarse como índices de una lista consecutiva. El catálogo incluye el Sector Norte confirmado por el IPS de Flawe (`0x0261` Montaña de Bota … `0x026F` Ciudad Genbu) y conserva el ID hexadecimal cuando todavía no existe un nombre confirmado.

Los IDs de sala añadidos en 0.7.14 salen de `esstname` / `ssstname` interpolando solo huecos entre IDs ya confirmados (p. ej. `0x0214` → `0x0217` da `0x0215` Escaleras del sótano y `0x0216` Torre de Prisión). `0x026D` aparece en la tabla de posadas `0x8003EDCC`. `0x02DE` / `0x02DF` aparecen en la tabla de transiciones de ddw3.

El umbral acumulado de EXP para el siguiente nivel no está almacenado junto al perfil. La interfaz lo obtiene de una tabla por compañero y nivel; la barra usa el intervalo entre el umbral actual y el siguiente. En nivel 99 se muestra “Nivel máx.”.

## Walkthrough del mod

El bloque de Flawe 2.0 en `0x8000B200` contiene:

- `+0x00`: puntero absoluto a la base del módulo cargado.
- `+0x04`: puntero al objeto de menú original.
- `+0x08`: desplazamiento relativo del título.
- `+0x0C`: desplazamiento relativo de la primera línea.
- `+0x10`: desplazamiento relativo de la segunda línea, que puede estar vacía.

**No son tres punteros absolutos.** El renderer suma la base a los desplazamientos.
La app valida las instrucciones del selector en `base + 0xD4C`
(`ACC30008`, `ACC4000C`, `ACC50010`), decodifica ambas líneas con la tabla DW
y las concatena en orden de pantalla. No elige misiones por palabras clave ni
por el relleno entre cadenas.

Evidencia: los PPF locales `Flawe's Mod - In-Game Walkthrough 2.0.ppf` y
`Flawe's Mod - Combined 2.0.ppf`. Extrayendo el payload de los sectores de
2352 bytes (2048 bytes desde +24), el módulo está en `0x21CEE000`.
En `+0xBFC` se construye `0x8000B200`; `+0xD4C` guarda los desplazamientos;
`+0x27D8`, `+0x280C` y `+0x2840` suman la base para dibujar título y líneas.
Estas secuencias coinciden en ambos parches. Protocol Ruins usa título
`0x8060`, primera línea `0x801C`, segunda `0x7FEC`; Repeating Tom usa
`0x80D0`, `0x8084`, `0x8080`. La segunda línea puede estar antes de la primera
en el archivo: buscar una sola cadena no recupera el objetivo completo.

Referencias del juego original: [STSTATUS](https://github.com/markisha64/ddw3/blob/master/asm/dw2003/pro/ststatus.s)
y [tabla de caracteres](https://github.com/markisha64/ddw3/blob/master/tools/ddw3-lang-file/src/codepoint.rs).
Los desplazamientos anteriores proceden del parche, no del juego original.

El lector se consulta cada 200 ms fuera de batalla y conserva el último
objetivo si el módulo deja de estar cargado. Un cambio de etapa de historia
invalida la caché. AUTO sigue usando `0x8005CCA8` para el idioma del panel.
El perfil USA mantiene la guía deshabilitada: este análisis verifica los dos
parches PAL locales, no un port del mod a USA. Las pruebas usan bytes del
parche y RAM simulada; falta validar la sincronización en una partida real.

## Escrituras controladas (0.7)

La POC 0.7 escribe solo cuando el usuario lo pide y la sesión no está en batalla ni en un overlay de evento:

| Dirección | Uso |
|---|---|
| `0x80048DA4`…`0x80048DAC` | Orden de la formación activa (tres `u32`) |

Los destinos del panel son iconos del mapa de Flawe (no el puente ni interiores). START y los iconos del mapa se recorren con la cruceta; □ cambia de servidor. No se escriben `0x80048D68` / `0x8004B3F8` para viajar: Flawe usa el icono seleccionado con × al salir completamente con △. En las transiciones observadas, `MAP_ID` ya contiene el destino mientras `AREA` aún conserva el mapa anterior; por eso títulos e icono actual usan `MAP_ID`.

### Dispatcher antiguo del IPS

El parche público copia su selector a `0x8000C000`. La selección directa valida:

| Dirección | Instrucción esperada | Uso |
|---:|---:|---|
| `0x8000C000` | `0x8E230180` | Lee si el cursor está sobre un icono |
| `0x8000C00C` | `0x14680271` | Rechaza la selección si no es válida |
| `0x8000C04C` | `0x8E230184` | Lee el ID interno del icono |

Durante ×, la app sustituye temporalmente la rama por `NOP` y la lectura del icono por `ori v1, zero, ID`. Los IDs son los índices de ASKMAP extraídos del IPS de [dmw_2003_patcher](https://github.com/markisha64/dmw_2003_patcher) (`0x14` Ciudad Asuka, `0x1E` Central Park, `0x16` Entrada del Bosque Alambre, `0x0A` Montaña de Bota, `0x06` Ciudad Genbu, y el resto de los 46 campos). Flawe ejecuta después su tabla original, que escribe el mapa y las coordenadas exactas; al soltar × se restaura la ventana completa de 80 bytes. Una firma distinta no se modifica y usa la cruceta como fallback. Amaterasu reutiliza los mismos índices de icono en otro mapa; el blob del parche solo escribe MAP_ID de Asuka, así que esos hubs siguen el fallback por cruceta.

Algunas compilaciones combinadas reubican el dispatcher. Se busca la misma estructura en toda la RAM y se prefiere la copia referenciada por un `j` o `jal`. Si no hay thunk, una coincidencia única sigue siendo válida. Las coincidencias sueltas de `lw 0x184` y `bne` se rechazan: también aparecen en el renderer normal del mapa. Varias copias sin desempate nunca se escriben.

Los mods opcionales se aplican con `retro_cheat_set` del núcleo, no con parches permanentes. Datos fuera de rango, perfiles inválidos y punteros externos a RAM se descartan.

No se ha documentado todavía un indicador estable para distinguir el cuadro exacto de pesca del estado normal de campo. ddw3 carga la pesca dentro de `FIELDSTG` (no hay overlay FISH/CAST/ROD). Recompilar el `.bin` no aportaría una firma nueva. Sigue haciendo falta un dump de los 2 MiB (o al menos la ventana principal y overlay) **de pie** y **con la caña echada** en el mismo mapa para localizar el byte de pose. Sin ese par, `isFishing` permanece en falso y la UI solo ofrece previsualización táctil.

### Función de viaje de Flawe 2.0 y caché de código

El volcado del mapa inglés inicializado contiene la función activa en
`0x8009BBE4` y otra copia en `0x8016F860`. La firma valida múltiples
instrucciones del prólogo; las copias reubicadas se resuelven por referencias
`j/jal` y se rechazan cuando son ambiguas. Se sustituye temporalmente
`+0x08` por NOP y `+0x38` por `ori a3, zero, icono`. Se conserva la
comprobación del servidor en `+0x24`. La ventana original de 60 bytes se
restaura al finalizar, también ante errores.

Las escrituras de RAM del frontend notifican al núcleo mediante la extensión
opcional `retro_notify_memory_write`. PCSX invalida los bloques recompilados
que contienen esas instrucciones, tanto al aplicar como al restaurar el
parche. No vacía toda la caché en cada sondeo. La presencia de esta función
en RAM no demuestra que el menú esté abierto.

### Guía independiente del idioma del menú

El panel evalúa una copia acotada de la rutina `+0xC68…+0x2723` sobre la
instantánea principal existente (`0x80048D00`, 10 KiB). Se detiene antes de
los stores de `+0xD4C` y usa los registros de desplazamientos para seleccionar
las líneas. No ejecuta código dentro del emulador ni escribe RAM.
`assets/guide/objectives.json` contiene los 157 pares alcanzables y sus cinco
idiomas. AUTO utiliza el byte PAL `0x8005CCA8`: 2 inglés, 3 francés, 4 italiano,
5 alemán, 6 español. La selección explícita del panel prevalece. El lector
del módulo vivo se conserva como respaldo cuando no se resuelve el catálogo.
La guía PAL no habilita automáticamente soporte para la edición USA.
