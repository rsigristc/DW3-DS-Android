#!/usr/bin/env python3
"""Write the selected CHANGELOG section for GitHub's release body."""
import argparse
import re
import sys
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("version")
args = parser.parse_args()
sys.stdout.reconfigure(encoding="utf-8")
version = args.version.removeprefix("v")
changelog = Path(__file__).resolve().parents[1] / "dw2003-dual-screen/CHANGELOG.md"
text = changelog.read_text(encoding="utf-8")
match = re.search(rf"^## {re.escape(version)}\s*\n(.*?)(?=^## |\Z)", text, re.M | re.S)
if match is None or not match.group(1).strip():
    raise SystemExit(f"No release notes found in CHANGELOG.md for {version}")
print(f"## Novedades / What's new — {version}\n")
print(match.group(1).strip())
print("\n---\nOfficial signed APK: `com.digitaladventure.dw2003`.")
print("Updates preserve the app's existing data. ROM and BIOS are not included.")
