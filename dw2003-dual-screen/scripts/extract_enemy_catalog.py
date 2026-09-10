#!/usr/bin/env python3
"""Build EnemyCatalog.kt from DDW3_Development_Kit typed data + skill names."""
from __future__ import annotations

import json
import re
from pathlib import Path

KIT = Path(r"C:\Users\RSigrist\Documents\DDW3_Development_Kit")
OUT = Path(__file__).resolve().parents[1] / "app/src/main/java/com/digitaladventure/dw2003/data/EnemyCatalog.kt"


def parse_enum(path: Path, enum_name: str) -> dict[str, int]:
    text = path.read_text(encoding="utf-8")
    body = re.search(rf"enum {enum_name} \{{(.*?)\n\}}", text, re.S)
    if not body:
        raise SystemExit(f"enum {enum_name} not found in {path}")
    values: dict[str, int] = {}
    next_value = 0
    for raw in body.group(1).splitlines():
        line = raw.split("//")[0].strip().rstrip(",")
        if not line:
            continue
        if "=" in line:
            name, expr = [part.strip() for part in line.split("=", 1)]
            next_value = int(expr, 0)
        else:
            name = line
        values[name] = next_value
        next_value += 1
    return values


def parse_toml_strings(path: Path) -> list[str]:
    text = path.read_text(encoding="utf-8")
    names: list[str] = []
    for match in re.finditer(r'"((?:\\.|[^"\\])*)"', text):
        names.append(bytes(match.group(1), "utf-8").decode("unicode_escape"))
    return names


def parse_enemy_stats(path: Path) -> list[dict]:
    text = path.read_text(encoding="utf-8")
    start = text.find("pub static mut ENEMY_DIGIMON_STATS")
    text = text[start:] if start >= 0 else text
    blocks = re.findall(r"EnemyStats \{((?:[^{}]|\{[^{}]*\})*)\}", text)
    rows = []
    for block in blocks:
        fields = {}
        for key in (
            "enemy_id", "digimon_id", "attack", "move_1", "move_2",
            "stat_str", "stat_def", "stat_spt", "stat_wis", "stat_spd",
            "stat_fire", "stat_water", "stat_ice", "stat_wind",
            "stat_thunder", "stat_machine", "stat_dark",
        ):
            match = re.search(rf"{key}:\s*([^,\n]+)", block)
            if not match:
                raise SystemExit(f"missing {key}")
            fields[key] = match.group(1).strip()
        rows.append(fields)
    return rows


def ident(value: str) -> str:
    return value.split("::")[-1]


def main() -> None:
    enemy_ids = parse_enum(KIT / "rust/types/src/enemy_id.rs", "EnemyId")
    digimon_ids = parse_enum(KIT / "rust/types/src/digimon_id.rs", "DigimonId")
    en_names = parse_toml_strings(KIT / "lang_file/dw2003/eng/essklnam.toml")
    es_names = parse_toml_strings(KIT / "lang_file/dw2003/spn/sssklnam.toml")
    rows = parse_enemy_stats(KIT / "rust/dw2003_pro_sdigiedt/src/data.rs")

    entries = []
    for raw in rows:
        enemy_id = enemy_ids[ident(raw["enemy_id"])]
        digimon_id = digimon_ids[ident(raw["digimon_id"])]
        name = ident(raw["digimon_id"])
        entries.append(
            {
                "enemyId": enemy_id,
                "digimonId": digimon_id,
                "name": name,
                "str": int(raw["stat_str"], 0),
                "def": int(raw["stat_def"], 0),
                "spt": int(raw["stat_spt"], 0),
                "wis": int(raw["stat_wis"], 0),
                "spd": int(raw["stat_spd"], 0),
                "fire": int(raw["stat_fire"], 0),
                "water": int(raw["stat_water"], 0),
                "ice": int(raw["stat_ice"], 0),
                "wind": int(raw["stat_wind"], 0),
                "thunder": int(raw["stat_thunder"], 0),
                "machine": int(raw["stat_machine"], 0),
                "dark": int(raw["stat_dark"], 0),
                "attack": int(raw["attack"], 0),
                "move1": int(raw["move_1"], 0),
                "move2": int(raw["move_2"], 0),
            }
        )

    lines = [
        "package com.digitaladventure.dw2003.data",
        "",
        "/** Static PAL enemy templates extracted from DDW3_Development_Kit. */",
        "object EnemyCatalog {",
        "    data class Template(",
        "        val enemyId: Int,",
        "        val digimonId: Int,",
        "        val name: String,",
        "        val strength: Int,",
        "        val defense: Int,",
        "        val spirit: Int,",
        "        val wisdom: Int,",
        "        val speed: Int,",
        "        val resistances: List<Int>,",
        "        val attackIds: List<Int>",
        "    )",
        "",
        "    private val byId by lazy { ENTRIES.associateBy { it.enemyId } }",
        "",
        "    fun template(enemyId: Int): Template? = byId[enemyId]",
        "",
        "    fun techniqueName(skillId: Int, spanish: Boolean): String? {",
        "        if (skillId <= 0) return null",
        "        val names = if (spanish) SKILL_ES else SKILL_EN",
        "        return names.getOrNull(skillId)?.takeIf { it.isNotBlank() && it != \"a\" }",
        "    }",
        "",
        "    private val ENTRIES = listOf(",
    ]
    for entry in entries:
        resists = ", ".join(
            str(entry[key])
            for key in ("fire", "water", "ice", "wind", "thunder", "machine", "dark")
        )
        attacks = ", ".join(
            str(value)
            for value in (entry["attack"], entry["move1"], entry["move2"])
            if value
        )
        lines.append(
            "        Template({eid}, {did}, \"{name}\", {str}, {defn}, {spt}, {wis}, {spd}, "
            "listOf({res}), listOf({atk})),".format(
                eid=entry["enemyId"],
                did=entry["digimonId"],
                name=entry["name"],
                str=entry["str"],
                defn=entry["def"],
                spt=entry["spt"],
                wis=entry["wis"],
                spd=entry["spd"],
                res=resists,
                atk=attacks,
            )
        )
    lines.append("    )")
    lines.append("")
    lines.append("    private val SKILL_EN = listOf(")
    for name in en_names:
        lines.append(f"        {json.dumps(name, ensure_ascii=False)},")
    lines.append("    )")
    lines.append("")
    lines.append("    private val SKILL_ES = listOf(")
    for name in es_names:
        lines.append(f"        {json.dumps(name, ensure_ascii=False)},")
    lines.append("    )")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"wrote {len(entries)} enemies to {OUT}")


if __name__ == "__main__":
    main()
