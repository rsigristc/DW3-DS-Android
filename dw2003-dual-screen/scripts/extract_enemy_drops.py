#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path

KIT = Path(r"C:\Users\RSigrist\Documents\DDW3_Development_Kit")
OUT = Path(__file__).resolve().parents[1] / "app/src/main/java/com/digitaladventure/dw2003/data/EnemyDrops.kt"


def parse_enum(path: Path, enum_name: str) -> dict[str, int]:
    text = path.read_text(encoding="utf-8")
    body = re.search(rf"enum {enum_name} \{{(.*?)\n\}}", text, re.S)
    if not body:
        raise SystemExit(f"enum {enum_name} not found")
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
    return [
        bytes(match.group(1), "utf-8").decode("unicode_escape")
        for match in re.finditer(r'"((?:\\.|[^"\\])*)"', path.read_text(encoding="utf-8"))
    ]


def ident(value: str) -> str:
    return value.split("::")[-1]


def main() -> None:
    enemy_ids = parse_enum(KIT / "rust/types/src/enemy_id.rs", "EnemyId")
    item_ids = parse_enum(KIT / "rust/types/src/item_id.rs", "ItemId")
    en_items = parse_toml_strings(KIT / "lang_file/dw2003/eng/esitmnam.toml")
    es_items = parse_toml_strings(KIT / "lang_file/dw2003/spn/ssitmnam.toml")
    text = (KIT / "rust/dw2003_pro_sdigiedt/src/data.rs").read_text(encoding="utf-8")
    start = text.find("pub static mut ENEMY_DIGIMON_STATS")
    blocks = re.findall(r"EnemyStats \{((?:[^{}]|\{[^{}]*\})*)\}", text[start:] if start >= 0 else text)

    lines = [
        "package com.digitaladventure.dw2003.data",
        "",
        "/** Drops from DDW3 EnemyStats + PAL item name tables. */",
        "object EnemyDrops {",
        "    data class Drop(val itemId: Int, val nameEn: String, val nameEs: String, val rate: Int)",
        "",
        "    fun drop(enemyId: Int): Drop? = byEnemy[enemyId]",
        "",
        "    fun label(enemyId: Int, spanish: Boolean): String {",
        "        val drop = drop(enemyId) ?: return if (spanish) \"Sin botín\" else \"No loot\"",
        "        if (drop.itemId == 0 || drop.rate <= 0) return if (spanish) \"Sin botín\" else \"No loot\"",
        "        val name = if (spanish) drop.nameEs else drop.nameEn",
        "        return if (spanish) \"$name · tasa ${drop.rate}\" else \"$name · rate ${drop.rate}\"",
        "    }",
        "",
        "    private val byEnemy = mapOf(",
    ]
    for block in blocks:
        enemy = ident(re.search(r"enemy_id:\s*([^,\n]+)", block).group(1))
        item = ident(re.search(r"droppable_item:\s*([^,\n]+)", block).group(1))
        rate = int(re.search(r"drop_rate:\s*([^,\n]+)", block).group(1), 0)
        enemy_id = enemy_ids[enemy]
        item_id = item_ids[item]
        name_en = en_items[item_id] if item_id < len(en_items) else item
        name_es = es_items[item_id] if item_id < len(es_items) else name_en
        if not name_en or name_en == "a":
            name_en = item
        if not name_es or name_es == "a":
            name_es = name_en
        lines.append(
            "        {eid} to Drop({iid}, {en}, {es}, {rate}),".format(
                eid=enemy_id,
                iid=item_id,
                en=json.dumps(name_en, ensure_ascii=False),
                es=json.dumps(name_es, ensure_ascii=False),
                rate=rate,
            )
        )
    lines.append("    )")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"wrote {len(blocks)} drops to {OUT}")


if __name__ == "__main__":
    main()
