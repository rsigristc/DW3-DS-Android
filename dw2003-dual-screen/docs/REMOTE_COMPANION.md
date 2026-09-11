# DW2003 Pocket Companion

Pocket Companion convierte una consola Android con controles físicos (por
ejemplo AYANEO Pocket AIR Mini) en un mando remoto y una segunda pantalla para
DW2003 Dual Screen ejecutándose en otro Android.

## Estado del MVP Wi-Fi

- `:remote-controller` genera la APK independiente para AYANEO.
- `:remote-protocol` contiene el protocolo binario versionado compartido.
- La app principal escucha UDP en el puerto `37603` mientras su actividad está
  iniciada.
- Un código aleatorio de seis dígitos autentica los paquetes de entrada.
- Cada paquete contiene el estado completo de botones y sticks, no eventos
  aislados. Un timeout de 750 ms libera todos los controles en el Fold.
- La AYANEO envía cambios inmediatamente y un heartbeat cada 250 ms.
- El Fold devuelve modo, ubicación, objetivo, Bits y HP/MP/EXP del equipo.
- Todos los botones físicos admitidos pueden reasignarse a cualquier acción de
  RetroPad o deshabilitarse.

## Compilar

Desde `dw2003-dual-screen`:

```powershell
./gradlew.bat :app:assembleDebug :remote-controller:assembleDebug
```

Artefactos:

- Fold: `app/build/outputs/apk/debug/app-debug.apk`
- AYANEO: `remote-controller/build/outputs/apk/debug/remote-controller-debug.apk`

## Probar

1. Instala la APK principal en el Fold y Pocket Companion en la AYANEO.
2. Conecta ambos dispositivos a la misma red Wi-Fi.
3. En el Fold abre `APP` y toca `Pocket Companion` para consultar IP, puerto y
   código.
4. Introduce la IP y el código en AYANEO y toca `CONECTAR`.
5. El estado cambia a `CONECTADO` cuando llega la primera telemetría.
6. `BOTONES` permite reasignar cada entrada física.

## Seguridad y alcance

El código evita que otro equipo de la red inyecte controles por accidente,
pero el tráfico del MVP no está cifrado. No debe exponerse el puerto a Internet.
La siguiente iteración debe añadir intercambio de claves y autenticación de
mensajes antes de habilitar descubrimiento automático.

## Transportes siguientes

El protocolo no depende de UDP. La ruta prevista es:

1. Wi-Fi UDP para validación rápida y telemetría.
2. USB-C mediante Android Open Accessory o ADB-free USB accessory, conservando
   los mismos `RemoteInputFrame` y `RemoteTelemetryFrame`.
3. Bluetooth HID como modo mando; no transportaría por sí solo toda la
   telemetría de segunda pantalla.

La latencia real debe medirse en hardware. El paquete incluye
`sentAtNanos`, secuencia y timestamp de muestreo para añadir métricas sin cambiar
la versión del protocolo.
