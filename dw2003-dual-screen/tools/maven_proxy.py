#!/usr/bin/env python3
"""Tiny curl-backed Maven cache for constrained CI environments.

Normal builds do not use this helper. Set DW3_MAVEN_PROXY to its local URL.
"""

from __future__ import annotations

import argparse
import mimetypes
import os
import subprocess
import tempfile
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote, urlsplit


GOOGLE = "https://dl.google.com/dl/android/maven2"
CENTRAL = "https://repo.maven.apache.org/maven2"
PLUGIN_PORTAL = "https://plugins.gradle.org/m2"
LOCKS: dict[str, threading.Lock] = {}
LOCKS_GUARD = threading.Lock()


def lock_for(path: str) -> threading.Lock:
    with LOCKS_GUARD:
        return LOCKS.setdefault(path, threading.Lock())


class MavenHandler(BaseHTTPRequestHandler):
    cache_root = Path("/tmp/dw3-maven-cache")

    def do_HEAD(self) -> None:  # noqa: N802
        self.serve(send_body=False)

    def do_GET(self) -> None:  # noqa: N802
        self.serve(send_body=True)

    def serve(self, send_body: bool) -> None:
        relative = unquote(urlsplit(self.path).path).lstrip("/")
        if not relative or ".." in Path(relative).parts:
            self.send_error(404)
            return
        destination = self.cache_root / relative
        if not destination.is_file():
            with lock_for(relative):
                if not destination.is_file() and not self.fetch(relative, destination):
                    self.send_error(404)
                    return

        size = destination.stat().st_size
        mime = mimetypes.guess_type(destination.name)[0] or "application/octet-stream"
        self.send_response(200)
        self.send_header("Content-Type", mime)
        self.send_header("Content-Length", str(size))
        self.end_headers()
        if send_body:
            with destination.open("rb") as source:
                while chunk := source.read(1024 * 256):
                    self.wfile.write(chunk)

    def fetch(self, relative: str, destination: Path) -> bool:
        destination.parent.mkdir(parents=True, exist_ok=True)
        google_owned = relative.startswith(("com/android/", "androidx/"))
        upstreams = (GOOGLE, CENTRAL, PLUGIN_PORTAL) if google_owned else (CENTRAL, GOOGLE, PLUGIN_PORTAL)
        for upstream in upstreams:
            with tempfile.NamedTemporaryFile(dir=destination.parent, delete=False) as temporary:
                temporary_path = Path(temporary.name)
            result = subprocess.run(
                ["curl", "-fLsS", "--retry", "2", "--connect-timeout", "15", "-o", str(temporary_path), f"{upstream}/{relative}"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                check=False,
            )
            if result.returncode == 0:
                os.replace(temporary_path, destination)
                return True
            temporary_path.unlink(missing_ok=True)
        return False

    def log_message(self, fmt: str, *args: object) -> None:
        print(f"maven-cache: {fmt % args}", flush=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--cache", type=Path, default=Path("/tmp/dw3-maven-cache"))
    args = parser.parse_args()
    MavenHandler.cache_root = args.cache
    server = ThreadingHTTPServer(("127.0.0.1", args.port), MavenHandler)
    server.serve_forever()


if __name__ == "__main__":
    main()
