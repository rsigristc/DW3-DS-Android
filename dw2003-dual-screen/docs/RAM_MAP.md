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

El bloque temporal se consulta desde `0x8000B200`. Los candidatos de puntero están en:

- `0x8000B208`
- `0x8000B20C`
- `0x8000B210`

Cada palabra alineada del bloque se trata como puntero candidato (`0x80000000`…`0x801FFFFF`). El texto se decodifica con la tabla europea de DW2003 y también como cadena ASCII. Si START carga `STSTATUS` y todavía no hay objetivo, se busca el mismo patrón en el overlay `0x80080000` y, si hace falta, en los 2 MiB. Flawe publica la guía solo en el idioma inglés del juego; el español del panel usa pistas propias cuando esos punteros no aparecen. En AUTO, `0x8005CCA8` decide el idioma del panel antes que ese texto, para que una partida española no se pinte en inglés solo porque Flawe dejó la guía en inglés.

## Escrituras controladas (0.7)

La POC 0.7 escribe solo cuando el usuario lo pide y la sesión no está en batalla ni en un overlay de evento:

| Dirección | Uso |
|---|---|
| `0x80048DA4`…`0x80048DAC` | Orden de la formación activa (tres `u32`) |

Los destinos del panel son iconos del mapa de Flawe (no el puente ni interiores). START y los iconos del mapa se recorren con la cruceta; □ cambia de servidor. No se escriben `0x80048D68` / `0x8004B3F8` para viajar: Flawe usa el icono seleccionado con × al salir completamente con △. En las transiciones observadas, `MAP_ID` ya contiene el destino mientras `AREA` aún conserva el mapa anterior; por eso títulos e icono actual usan `MAP_ID`.

### Dispatcher de Flawe 2.0

El parche público copia su selector a `0x8000C000`. La selección directa valida:

| Dirección | Instrucción esperada | Uso |
|---:|---:|---|
| `0x8000C000` | `0x8E230180` | Lee si el cursor está sobre un icono |
| `0x8000C00C` | `0x14680271` | Rechaza la selección si no es válida |
| `0x8000C04C` | `0x8E230184` | Lee el ID interno del icono |

Durante ×, la app sustituye temporalmente la rama por `NOP` y la lectura del icono por `ori v1, zero, ID`. Los IDs son los índices de ASKMAP extraídos del IPS de [dmw_2003_patcher](https://github.com/markisha64/dmw_2003_patcher) (`0x14` Ciudad Asuka, `0x1E` Central Park, `0x16` Entrada del Bosque Alambre, `0x0A` Montaña de Bota, `0x06` Ciudad Genbu, y el resto de los 46 campos). Flawe ejecuta después su tabla original, que escribe el mapa y las coordenadas exactas; al soltar × se restaura la ventana completa de 80 bytes. Una firma distinta no se modifica y usa la cruceta como fallback. Amaterasu reutiliza los mismos índices de icono en otro mapa; el blob del parche solo escribe MAP_ID de Asuka, así que esos hubs siguen el fallback por cruceta.

Algunas compilaciones combinadas reubican el dispatcher. En ese caso se busca la misma estructura en toda la RAM, se admite cualquier destino inmediato de `bne v1,t0,*` y se exige que un `j` o `jal` apunte a la copia encontrada. Una coincidencia ausente o ambigua nunca se escribe.

Los mods opcionales se aplican con `retro_cheat_set` del núcleo, no con parches permanentes. Datos fuera de rango, perfiles inválidos y punteros externos a RAM se descartan.

No se ha documentado todavía un indicador estable para distinguir el cuadro exacto de pesca del estado normal de campo. ddw3 carga la pesca dentro de `FIELDSTG` (no hay overlay FISH/CAST/ROD). Recompilar el `.bin` no aportaría una firma nueva. Sigue haciendo falta un dump de los 2 MiB (o al menos la ventana principal y overlay) **de pie** y **con la caña echada** en el mismo mapa para localizar el byte de pose. Sin ese par, `isFishing` permanece en falso y la UI solo ofrece previsualización táctil.
