# Publicación de POC

Cada versión generada (0.7.x-poc) se publica así:

1. Fusionar la rama de trabajo en `main` (fast-forward cuando la historia es lineal).
2. Empujar `main`.
3. Crear la etiqueta anotada `vX.Y.Z-poc` en ese commit y empujarla.
4. El workflow `Publish Android release` construye el APK **release firmado**, prueba y publica el GitHub Release con el APK y `SHA256SUMS.txt`.

La firma vive en secretos del repo (`DW2003_KEYSTORE_BASE64`, `DW2003_STORE_PASSWORD`, `DW2003_KEY_ALIAS`, `DW2003_KEY_PASSWORD`). Sin ese keystore, Android rechaza actualizar («App not installed») porque cada runner de CI tendría otra firma debug.

El paquete de release es `com.digitaladventure.dw2003`. Las POC 0.7.0–0.7.19 eran `com.digitaladventure.dw2003.debug`: hay que desinstalar esa una vez e instalar el APK release. A partir de 0.7.20 el actualizador in-app puede sustituir la app.

Para firmar en local, copia `keystore.properties.example` a `keystore.properties` (está en `.gitignore`).

No se publican releases desde ramas laterales. Las ramas `cursor/companion-*-7606` de 0.7.11–0.7.16 ya están contenidas en `main` tras 0.7.16.
