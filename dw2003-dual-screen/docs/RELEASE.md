# Publicación

Cada versión oficial (`1.x`) se publica así:

1. Fusionar el trabajo en `main`.
2. Empujar `main`.
3. Crear la etiqueta anotada `vX.Y.Z` en ese commit y empujarla.
4. El workflow `Publish Android release` ejecuta `scripts/build.sh` (las mismas tareas que un desarrollador), firma el APK y publica el GitHub Release con el APK, `SHA256SUMS.txt` y `CERT.txt`.

No se publican releases desde ramas laterales. Las POC `v0.7.x-poc` se retiraron al pasar a v1.0.0.
