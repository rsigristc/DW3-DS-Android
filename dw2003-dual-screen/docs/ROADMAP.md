# Roadmap posterior a POC 0.7

## Completado en 0.2

- Cambio de BIN desde el menú de aplicación sin borrar sus datos.
- BIOS europeo opcional aportado por el usuario y validado localmente.
- Importación/exportación de Memory Card raw, VGS/MEM y GME.
- Restauración segura del respaldo automático.
- Modo juego completo en pantallas exteriores estrechas y doble panel desde 600 dp.
- Tratamiento separado de bisagras que Android marca como separadoras.

## Completado en 0.3

- Gamepad virtual multitáctil adaptable con opción de ocultarlo.
- Guardado/carga rápida aislada por ROM, velocidad 2× y silencio.
- Detección defensiva de sesión iniciada antes de publicar formación y estadísticas.

## Completado en 0.4

- HP, MP y EXP etiquetados en Exploración y Batalla.
- Umbrales acumulados y EXP restante hasta el siguiente nivel.
- Selector de compañero, parámetros y resistencias elementales en Gestión.
- Técnica característica de cada forma Rookie.

## Completado en 0.5

- Icono Android y sprites transparentes de Tamer/compañeros.
- Estado del Tamer y Bits en Exploración.
- Catálogo disperso de 108 IDs de mapa confirmados.
- Catálogo de 268 objetos equipables con nombres y bonificaciones.
- Creación automática de Memory Card formateada.
- Botón remoto para ocultar el gamepad y eliminación de comandos duplicados del panel.

## Completado en 0.6

- Estado rápido y Memory Card emparejados, con rechazo seguro del formato antiguo.
- Pestaña seleccionada persistente.
- Nombre de Tamer, mapa lógico, servidor y sector.
- Radar regional Asuka/Amaterasu con los mapas aportados.
- Digievolución activa, nivel de habilidad y técnicas con coste de MP.
- Fondo transparente de sprites mediante máscara conectada al borde.

## Completado en 0.7

- Título de ubicación alineado con la pantalla de carga del hub.
- Transparencia del sprite del Tamer.
- Configuración inicial desde ⚙ APP, pestaña Mods opcional y códigos PAL.
- Viaje rápido por región desde el radar, con destinos visitados.
- Reorden de formación desde Batalla fuera de combate/eventos.
- Memory Card: no serializar durante el guardado del juego y aviso de BIOS HLE.

## Fase 1 — Validación de hardware

- Probar instalación, arranque, audio, guardado y controles en AYN Thor.
- Confirmar nombre/ID del segundo display y comportamiento al cerrarlo o reabrirlo.
- Probar Galaxy Z Fold abierto, cerrado, rotado y en modo flex.
- Ajustar densidades, márgenes y proporción entre juego/panel con capturas reales.
- Registrar FPS, latencia de audio, uso de CPU/GPU, temperatura y batería.

## Fase 2 — Datos exactos del juego

- Resolver firmas adicionales para la ROM europea original.
- Extraer geometría y marcadores del mapa cargado para reemplazar el radar provisional.
- Catalogar IDs de equipo, objetos y técnicas heredadas/aprendidas con nombres localizados.
- Resolver objetivo/título del walkthrough de manera determinista en todos los menús.
- Identificar enemigo, turno y selección de comando para una vista de batalla más contextual.
- Resolver mediante capturas comparativas el indicador de animación/estado de pesca.

## Fase 3 — Experiencia de uso

- Importar CUE multitrack y CHD.
- Perfiles gráficos y de rendimiento por dispositivo.
- Editor de mapeo para mandos integrados y Bluetooth.
- Configuración avanzada de posición, opacidad y vibración de controles táctiles.
- Rotación de múltiples ranuras y copias de estados rápidos.

## Fase 4 — Distribución y mantenimiento

- Pruebas instrumentadas en varios tamaños y posturas.
- Build reproducible firmado por CI.
- Telemetría local opcional de rendimiento, sin datos personales.
- Paquete de traducciones separado de los datos del juego.
- Matriz de compatibilidad por revisión de ROM/mod.
