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

Orden de perfiles: Kotemon, Kumamon, Monmon, Agumon, Veemon, Guilmon, Renamon y Patamon.

Los IDs de área se presentan como hexadecimales (`0x0200`, `0x0206`, `0x02D8`, etc.). No deben usarse como índices de una lista consecutiva. El catálogo 0.6 añade ubicaciones principales de Amaterasu y conserva el ID hexadecimal cuando todavía no existe un nombre confirmado.

El umbral acumulado de EXP para el siguiente nivel no está almacenado junto al perfil. La interfaz lo obtiene de una tabla por compañero y nivel; la barra usa el intervalo entre el umbral actual y el siguiente. En nivel 99 se muestra “Nivel máx.”.

## Walkthrough del mod

El bloque temporal se consulta desde `0x8000B200`. Los candidatos de puntero están en:

- `0x8000B208`
- `0x8000B20C`
- `0x8000B210`

Cada puntero se valida contra los 2 MiB de RAM y se decodifican hasta 256 bytes con la tabla europea de DW2003. Se selecciona el texto legible más largo. Si no existe, la UI solicita abrir el menú para que el mod actualice sus punteros.

## Escrituras controladas (0.7)

La POC 0.7 escribe solo cuando el usuario lo pide y la sesión no está en batalla ni en un overlay de evento:

| Dirección | Uso |
|---|---|
| `0x80048DA4`…`0x80048DAC` | Orden de la formación activa (tres `u32`) |
| `0x80048D68` y `0x8004B3F8` | Destino de viaje rápido (ID de área/mapa) |

Los mods opcionales se aplican con `retro_cheat_set` del núcleo, no con parches permanentes. Datos fuera de rango, perfiles inválidos y punteros externos a RAM se descartan.

No se ha documentado todavía un indicador estable para distinguir el cuadro exacto de pesca del estado normal de campo. La UI solo marca si el área admite pesca y permite previsualizar el sprite aportado; no deduce automáticamente “pescando” por la ubicación.
