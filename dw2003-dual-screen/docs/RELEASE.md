# Publicación de POC

Cada versión generada (0.7.x-poc) se publica así:

1. Fusionar la rama de trabajo en `main` (fast-forward cuando la historia es lineal).
2. Empujar `main`.
3. Crear la etiqueta anotada `vX.Y.Z-poc` en ese commit y empujarla.
4. El workflow `Publish Android release` construye, prueba y publica el GitHub Release con el APK y `SHA256SUMS.txt`.

No se publican releases desde ramas laterales. Las ramas `cursor/companion-*-7606` de 0.7.11–0.7.16 ya están contenidas en `main` tras 0.7.16.
