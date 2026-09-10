"""Generate enemy-only skill names and EXE power table for EnemyCatalog.kt."""
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CAT = ROOT / "app/src/main/java/com/digitaladventure/dw2003/data/EnemyCatalog.kt"
DATA = Path(r"C:\Users\RSigrist\Documents\DDW3_Development_Kit\rust\dw2003_exe_data\src\data.rs")

# Franchise / community names for enemy-only IDs left as "a" in SKLNAM.
SIGNATURES = {
    "Kunemon": ("Static Elect", "Electrostática"),
    "Tapirmon": ("Nightmare Syndrome", "Síndrome Pesadilla"),
    "Crabmon": ("Scissors Attack", "Ataque Tijera"),
    "Gizamon": ("Spiral Blade", "Hoja Espiral"),
    "DemiDevimon": ("Demi Dart", "Dardo Demi"),
    "Betamon": ("Electric Shock", "Choque Eléctrico"),
    "Vegiemon": ("Sweet Breath", "Aliento Dulce"),
    "Kuwagamon": ("Scissor Arms", "Brazos Tijera"),
    "Yanmamon": ("Thunder Ray", "Rayo Trueno"),
    "Kiwimon": ("Little Peck", "Picotazo"),
    "Dokugumon": ("Poison Thread", "Hilo Venenoso"),
    "Goburimon": ("Goblin Strike", "Golpe Goblin"),
    "Baronmon": ("Flying Sword", "Espada Voladora"),
    "Minotarumon": ("Dark Side Attack", "Ataque Oscuro"),
    "Flymon": ("Poison Stinger", "Aguijón Venenoso"),
    "Woodmon": ("Branch Drain", "Absorción de Ramas"),
    "Tyrannomon": ("Fire Breath", "Aliento de Fuego"),
    "Triceramon": ("Horn Rush", "Embestida de Cuerno"),
    "Airdramon": ("Wing Cutter", "Corte de Ala"),
    "Gekomon": ("Symphony Crash", "Choque Sinfónico"),
    "Coelamon": ("Variable Darts", "Dardos Variables"),
    "Bakemon": ("Death Charm", "Encanto Mortal"),
    "Phantomon": ("Soul Chopper", "Cortaalmas"),
    "Kokatorimon": ("Foul Wind", "Viento Fétido"),
    "Apemon": ("Mega Bone Stick", "Mega Bastón Óseo"),
    "Shellmon": ("Hydro Pressure", "Hidropresión"),
    "Tortomon": ("Shell Panic", "Pánico de Caparazón"),
    "Dolphmon": ("Shining Ring", "Anillo Brillante"),
    "Gesomon": ("Deadly Shade", "Sombra Mortal"),
    "Octomon": ("Acid Ink", "Tinta Ácida"),
    "Divermon": ("Lush Cutter", "Corte Profundo"),
    "ShogunGekomon": ("Musical Fist", "Puño Musical"),
    "Seadramon": ("Ice Blast", "Ráfaga Helada"),
    "Mammothmon": ("Tusk Strike", "Golpe de Colmillo"),
    "Etemon": ("Dark Network", "Red Oscura"),
    "Ogremon": ("Pummel Whack", "Mamporro"),
    "Tuskmon": ("Guilty Cannon", "Cañón Culpable"),
    "Rockmon": ("Crazy Giggle", "Risa Loca"),
    "Quetzalmon": ("Rainbow Storm", "Tormenta Arcoíris"),
    "Vademon": ("Akumander", "Akumander"),
    "Maildramon": ("Rider Kick", "Patada Jinete"),
    "Mamemon": ("Smiley Bomb", "Bomba Sonriente"),
    "Guardromon": ("Guardian Barrage", "Andanada Guardiana"),
    "Thundermon": ("Thunder Fall", "Caída de Trueno"),
    "Hagurumon": ("Darkness Gear", "Engranaje Oscuro"),
    "Clockmon": ("Chrono Breaker", "Rompechrono"),
    "Andromon": ("Gatling Missile", "Misil Gatling"),
    "Numemon": ("Poop Attack", "Ataque Lodo"),
    "Sukamon": ("Poop Toss", "Lanzamiento Lodo"),
    "Garbagemon": ("Junk Dunk", "Mate de Chatarra"),
    "Roachmon": ("Funky Kick", "Patada Funky"),
    "Raremon": ("Kusai Gas", "Gas Fétido"),
    "Cyclonemon": ("Hyper Heat", "Hipercalor"),
    "Megadramon": ("Genocide Attack", "Ataque Genocida"),
    "Monzaemon": ("Hearts Attack", "Ataque de Corazones"),
    "Tankmon": ("Hyper Cannon", "Hiper Cañón"),
    "Nanimon": ("Party Time", "Hora de Fiesta"),
    "Musyamon": ("Kirikomi", "Kirikomi"),
    "Kurisarimon": ("Worm Venom", "Veneno Gusano"),
    "Snimon": ("Twin Sickles", "Hoces Gemelas"),
    "Blossomon": ("Spiral Flower", "Flor Espiral"),
    "Cherrymon": ("Cherry Bomb", "Bomba Cereza"),
    "Gargoylemon": ("Black Ashes", "Cenizas Negras"),
    "Mummymon": ("Snake Bandage", "Venda Serpiente"),
    "Arukenimon": ("Spider Thread", "Hilo de Araña"),
    "Okuwamon": ("Double Scissor", "Tijera Doble"),
    "MetalTyrannomon": ("Nuclear Laser", "Láser Nuclear"),
    "Deltamon": ("Triplex Force", "Fuerza Triple"),
    "Whamon": ("Tidal Wave", "Maremoto"),
    "MarineDevimon": ("Guilty Wave", "Ola Culpable"),
    "Dragomon": ("Forbidden Trident", "Tridente Prohibido"),
    "MegaSeadramon": ("Lightning Javelin", "Jabalina Relámpago"),
    "Ebidramon": ("Twin Scissors", "Tijeras Gemelas"),
    "Vilemon": ("Nightmare", "Pesadilla"),
    "SkullSatamon": ("Nail Bone", "Hueso Uña"),
    "Brachiomon": ("Mini Ice Blast", "Mini Ráfaga Helada"),
    "Drimogemon": ("Drill Spin", "Giro Taladro"),
    "Flarerizamon": ("Flame Tower", "Torre de Llama"),
    "Meramon": ("Burning Fist", "Puño Ardiente"),
    "Mojyamon": ("Bone Boomerang", "Bumerán Óseo"),
    "Frigimon": ("Sub-zero Ice Punch", "Puño Gélido"),
    "Icemon": ("Iceball Bomb", "Bomba de Hielo"),
    "Giromon": ("Deadly Bomb", "Bomba Mortal"),
    "Vikemon": ("Arctic Blizzard", "Ventisca Ártica"),
    "Lynxmon": ("Meteor Squall", "Chubasco Meteoro"),
    "SkullMeramon": ("Heavy Metal Fire", "Fuego Heavy Metal"),
    "Devidramon": ("Crimson Nail", "Uña Carmesí"),
    "Kimeramon": ("Heat Viper", "Víbora de Calor"),
    "Pukumon": ("Needle Squall", "Chubasco de Agujas"),
    "MetalSeadramon": ("River of Power", "Río de Poder"),
    "Tylomon": ("Terror Waterfall", "Cascada del Terror"),
    "Scorpiomon": ("Stinger Surprise", "Aguijón Sorpresa"),
    "Antylamon": ("Asipatravana", "Asipatravana"),
    "HKabuterimon": ("Electro Shocker", "Electroshock"),
    "Puppetmon": ("Puppet Pummel", "Mamporro Marioneta"),
    "SkullMammothmon": ("Spiral Bone", "Hueso Espiral"),
    "Valkyrimon": ("Fenrir Sword", "Espada Fenrir"),
    "LadyDevimon": ("Darkness Wave", "Ola de Oscuridad"),
    "MetalEtemon": ("Full Metal Hip Attack", "Cadera Metal"),
    "KingEtemon": ("Monkey Wrench", "Llave Inglesa"),
    "Gryphonmon": ("Supersonic Voice", "Voz supersónica"),
    "Infermon": ("Cocoon Attack", "Ataque Capullo"),
    "Shadramon": ("Dancing Sword", "Espada Danzante"),
    "Boltmon": ("Tomahawk Crusher", "Triturador Tomahawk"),
    "Piedmon": ("Trump Sword", "Espada Trump"),
    "Machinedramon": ("Giga Cannon", "Giga Cañón"),
    "VenomMyotismon": ("Venom Infusion", "Infusión de Veneno"),
    "Apokarimon": ("Gran Death Big Bang", "Gran Big Bang"),
    "Ghoulmon": ("Death Claw", "Garra Mortal"),
    "Creepymon": ("Flame Inferno", "Inferno de Llama"),
    "Armageddemon": ("Ultimate Flare", "Bengala Final"),
    "RedVegiemon": ("Rotten Breath", "Aliento Podrido"),
    "Fugamon": ("Evil Hurricane", "Huracán Maligno"),
    "BlueMeramon": ("Ice Phantom", "Fantasma de Hielo"),
    "Pharaohmon": ("Necro Magic", "Necromagia"),
    "MasterTyrannomon": ("Fire Blast", "Ráfaga de Fuego"),
    "HiAndromon": ("Atomic Ray", "Rayo Atómico"),
    "WaruMonzaemon": ("Heartbreak Attack", "Ataque Rompecorazones"),
    "Datamon": ("Digital Bomb", "Bomba Digital"),
    "Persiamon": ("Cat's Eye", "Ojo de Gato"),
    "Bulbmon": ("Giga Volt", "Giga Voltio"),
    "Knightmon": ("Berserk Sword", "Espada Berserker"),
    "Raidenmon": ("Electric Cloud", "Nube Eléctrica"),
    "Fujinmon": ("Wind Blade", "Hoja de Viento"),
    "Raijinmon": ("Thunder Blade", "Hoja de Trueno"),
    "BKMegaGargomon": ("Dark Gauntlet", "Guantelete Oscuro"),
    "BKImperialdramon": ("Dark Splendor", "Esplendor Oscuro"),
    "BKWarGrowlmon": ("Atomic Blaster", "Bláster Atómico"),
    "BKWarGreymon": ("Dramon Killer", "Mata Dramon"),
    "BKSeraphimon": ("Testament", "Testamento"),
    "Armormon": ("Justice Missile", "Misil Justicia"),
    "MetalGreymon": ("Giga Destroyer", "Giga Destructor"),
    "Paildramon": ("Desperado Blaster", "Bláster Desperado"),
    "WarGrowlmon": ("Atomic Blaster", "Bláster Atómico"),
    "GrapLeomon": ("The King of Fists", "Rey de los Puños"),
    "Kyukimon": ("Blade Twister", "Torbellino de Hojas"),
    "Taomon": ("Talisman of Light", "Talismán de Luz"),
    "MagnaAngemon": ("Gate of Destiny", "Puerta del Destino"),
    "Cardmon": ("Card Flash", "Destello de Carta"),
    "BKKingNumemon": ("Royal Sludge", "Lodo Real"),
    "Vemmon": ("Vem Laser", "Láser Vem"),
    "Galacticmon": ("Ragnarok Cannon", "Cañón Ragnarok"),
}


def base_owner(name: str) -> str:
    return re.sub(r"\d+$", "", name)


def label_for(owner: str) -> tuple[str, str]:
    base = base_owner(owner)
    pair = SIGNATURES.get(owner) or SIGNATURES.get(base)
    if pair:
        en, es = pair
        if owner != base:
            return f"{en} II", f"{es} II"
        return en, es
    if owner != base:
        return f"{base} Strike II", f"Golpe {base} II"
    return f"{owner} Strike", f"Golpe {owner}"


def main() -> None:
    text = CAT.read_text(encoding="utf-8")
    en_block = re.search(r"private val SKILL_EN = listOf\((.*?)\)\n\n    private val SKILL_ES", text, re.S).group(1)
    names = re.findall(r'"((?:\\.|[^"])*)"', en_block)
    templates = re.findall(r'Template\((\d+), (\d+), "([^"]+)".*listOf\(([^)]*)\)\),', text)
    owners = defaultdict(list)
    used = set()
    for _eid, _did, name, atks in templates:
        ids = [int(x) for x in atks.split(",") if x.strip()]
        for skill_id in ids:
            used.add(skill_id)
            owners[skill_id].append(name)

    unnamed = []
    for skill_id in sorted(used):
        name = names[skill_id] if skill_id < len(names) else ""
        if not name or name in ("a", "None"):
            unnamed.append(skill_id)

    data = DATA.read_text(encoding="utf-8")
    rows = re.findall(r"Unknown3 \{ f0: (0x[0-9a-fA-F]+), f1: (0x[0-9a-fA-F]+)", data)
    powers = {}
    for skill_id in sorted(used):
        if 1 <= skill_id <= len(rows):
            powers[skill_id] = int(rows[skill_id - 1][1], 16)

    en_lines = []
    es_lines = []
    for skill_id in unnamed:
        en, es = label_for(owners[skill_id][0])
        en_lines.append(f"        {skill_id} to \"{en}\",")
        es_lines.append(f"        {skill_id} to \"{es}\",")

    power_lines = [f"        {sid} to {pw}," for sid, pw in powers.items() if pw]

    replacement = (
        "    // essklnam leaves enemy-only IDs as \"a\". Names follow the owner signature.\n"
        "    private val ENEMY_TECH_EN = mapOf(\n"
        + "\n".join(en_lines)
        + "\n    )\n"
        "    private val ENEMY_TECH_ES = mapOf(\n"
        + "\n".join(es_lines)
        + "\n    )\n\n"
        "    private val SKILL_POWER = mapOf(\n"
        + "\n".join(power_lines)
        + "\n    )\n"
        "}\n"
    )

    updated = re.sub(
        r"    // essklnam leaves enemy-only IDs as \"a\".*\Z",
        replacement,
        text,
        count=1,
        flags=re.S,
    )
    if "fun techniqueLabel" not in updated:
        updated = updated.replace(
            "        return if (spanish) ENEMY_TECH_ES[skillId] else ENEMY_TECH_EN[skillId]\n    }",
            "        return if (spanish) ENEMY_TECH_ES[skillId] else ENEMY_TECH_EN[skillId]\n    }\n\n"
            "    fun techniqueLabel(skillId: Int, spanish: Boolean): String {\n"
            "        val name = techniqueName(skillId, spanish) ?: if (spanish) \"Técnica $skillId\" else \"Tech $skillId\"\n"
            "        val power = SKILL_POWER[skillId] ?: TechniquePower.powerOf(name)\n"
            "        return if (power != null && power > 0) \"$name ($power)\" else name\n"
            "    }",
        )
    CAT.write_text(updated, encoding="utf-8")
    print(f"wrote {len(unnamed)} names and {len(power_lines)} powers")


if __name__ == "__main__":
    main()
