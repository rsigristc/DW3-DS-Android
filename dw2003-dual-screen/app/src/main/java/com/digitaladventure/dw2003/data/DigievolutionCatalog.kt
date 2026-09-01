package com.digitaladventure.dw2003.data

data class TechniqueInfo(val name: String, val mp: Int?, val learnLevel: Int)

/** Digievolution and technique data extracted from jeanheck/digivice's DW3 tables. */
object DigievolutionCatalog {
    private val names = mapOf(
        5 to "Greymon", 6 to "Devimon", 12 to "MetalGreymon", 19 to "Kabuterimon",
        20 to "Angemon", 26 to "SkullGreymon", 27 to "MetalMamemon", 56 to "Digitamamon",
        59 to "Phoenixmon", 66 to "Myotismon", 144 to "Rosemon", 148 to "Imperialdramon",
        150 to "Omnimon", 151 to "Diaboromon", 196 to "MetalGarurumon", 211 to "MagnaAngemon",
        213 to "WarGreymon", 214 to "Seraphimon", 230 to "GranKuwagamon", 234 to "Angewomon",
        254 to "Paildramon", 259 to "ExVeemon", 260 to "Stingmon", 267 to "BK WarGreymon",
        359 to "Imperialdramon-F", 367 to "Growlmon", 368 to "WarGrowlmon", 369 to "Gallantmon",
        372 to "MegaGargomon", 374 to "Kyubimon", 375 to "Taomon", 376 to "Sakuyamon",
        377 to "Beelzemon", 378 to "MaloMyotismon", 381 to "Imperialdramon-P", 386 to "Dinohumon",
        387 to "Hookmon", 388 to "Grizzmon", 389 to "Kyukimon", 390 to "Armormon",
        391 to "GrapLeomon", 392 to "Guardiangemon", 393 to "Cannondramon", 394 to "Marsmon"
    )

    private val techniques = mapOf(
        5 to listOf(t("Flame Ball",20,5),t("Triple Fire",25,15),t("Double Power",42,30),t("Flame Lance",48,45),t("Nova Blast",60,60)),
        6 to listOf(t("Dark Matter",20,10),t("Evil Poison",18,20),t("Armor Break",39,35),t("Stun Shock",18,55),t("Touch of Evil",40,70)),
        12 to listOf(t("Flame Ball",20,5),t("Flame Lance",48,10),t("Triple Fire",25,15),t("Magic Missile",56,35),t("Rising Fire",60,60),t("Giga Destroyer",90,70)),
        19 to listOf(t("Energy Leech",18,5),t("Venom Stab",36,15),t("Hammer Rush",30,35),t("Energy Drain",72,60),t("Electro Shocker",90,70)),
        20 to listOf(t("Air Blast",20,5),t("Small Heal",16,10),t("Double Guard",42,15),t("Mega Tornado",30,45),t("Hand of Fate",42,60)),
        26 to listOf(t("Flame Ball",20,5),t("Dark Matter",20,15),t("Triple Fire",25,35),t("Black Thorn",60,45),t("Dark Shot",90,70)),
        27 to listOf(t("Metal Attack",26,5),t("Magic Missile",56,15),t("Mechanical Bash",48,35),t("Slow Down",41,45),t("Twin Missile",64,60),t("Energetic Bomb",78,70)),
        56 to listOf(t("Hypno Gas",40,10),t("Soul Snatcher",50,30),t("Hypno Nebula",60,55),t("NM Syndromer",90,85)),
        59 to listOf(t("Full Heal",120,10),t("Final Heal",240,40),t("Erase Magic",37,60),t("Auto Recover",82,75),t("Crimson Flame",200,90)),
        66 to listOf(t("Dark Fear",48,10),t("Black Dart",70,25),t("Armor Off",78,40),t("Confuse Gas",40,65),t("Soul Snatcher",50,80),t("Grisly Wing",90,85)),
        144 to listOf(t("Grand Wave",72,10),t("Erase Poison",12,20),t("Erase Paralysis",12,25),t("Water Field",100,50),t("Anti-Magic",20,70),t("Thorn Whipping",130,85)),
        148 to listOf(t("Mega Heal",74,5),t("Mechanical Bash",48,25),t("Showstorm",60,50),t("Sylph Storm",72,70),t("Mega Crusher",150,85)),
        150 to listOf(t("Flame Breath",72,10),t("Flame Sphere",180,30),t("Giga Fire",90,35),t("Inferno",200,60),t("T-Sword",250,99)),
        151 to listOf(t("Deadly Poison",90,10),t("Paralyze Shock",90,20),t("Confuse Nebula",90,40),t("Hypno Nebula",60,55),t("Cable Crusher",270,99)),
        196 to listOf(t("Mechanical Bash",48,10),t("Twin Missile",64,15),t("Magical Cannon",72,45),t("Metal Field",100,70),t("Metal Wolf Claw",150,85)),
        211 to listOf(t("Air Blast",20,5),t("Small Heal",16,10),t("Mega Tornado",30,25),t("Mega Heal",74,40),t("Mega Protection",84,50),t("Gate of Destiny",112,70)),
        213 to listOf(t("Flame Lance",48,5),t("Flame Breath",72,35),t("Rising Fire",60,45),t("Twin Missile",64,60),t("Giga Fire",90,80),t("Terra Force",140,85)),
        214 to listOf(t("Mega Tornado",30,5),t("Mega Heal",74,20),t("Sylph Storm",72,35),t("Giga Heal",188,55),t("Wind Field",100,75),t("Seven Heavens",140,85)),
        230 to listOf(t("Venom Stab",36,10),t("Energy Drain",72,30),t("Confuse Stab",64,50),t("Impact Rush",60,65),t("Dimension Scissors",200,90)),
        234 to listOf(t("Antidote",8,15),t("Divine Rain",30,20),t("Anti-Paralysis",8,30),t("Celestial Arrow",44,70)),
        254 to listOf(t("Metal Attack",26,5),t("Ice Shower",48,10),t("Mega Tornado",30,25),t("Speed Up",48,55),t("Anti-Paralysis",8,65),t("Desperado Blaster",84,70)),
        259 to listOf(t("Air Blast",20,10),t("Ice Blow",20,20),t("Small Heal",16,40),t("Antidote",8,50),t("Veelaser",60,60)),
        260 to listOf(t("Poison Bites",18,10),t("Energy Leech",18,15),t("Panic Bites",18,35),t("Venom Stab",36,55),t("Spiking Strike",36,60)),
        267 to listOf(t("Flame Breath",72,10),t("Dark Fear",48,20),t("Giga Fire",90,35),t("Black Dart",70,50),t("Fire Field",100,70),t("Terra Destroyer",140,85)),
        359 to listOf(t("Heaven Hit",32,10),t("Mega Boost",96,20),t("Full Heal",120,45),t("Counter Strike",96,60),t("Giga Heal",188,75),t("Giga Crusher",180,90)),
        367 to listOf(t("Double Power",42,5),t("Double Guard",42,10),t("Picking Claw",18,25),t("Counter Alert",48,45),t("Plasma Blade",40,60)),
        368 to listOf(t("Picking Claw",18,5),t("Counter Alert",48,10),t("Mega Protection",84,25),t("Mega Strength",84,45),t("Speed Up",48,60),t("Atomic Blaster",90,70)),
        369 to listOf(t("Speed Up",48,15),t("Counter Strike",96,25),t("Snapping Claw",54,50),t("Mega Boost",96,75),t("Final Purification",140,85)),
        372 to listOf(t("Mechanical Bash",48,5),t("Magical Cannon",72,20),t("God Bombard",120,50),t("Mega Break",82,75),t("Giant Missile",200,90)),
        374 to listOf(t("Thunder Bolt",20,5),t("Ice Blow",20,15),t("Thunder Gemini",30,25),t("Ice Shower",48,55),t("Dragon Wheel",60,60)),
        375 to listOf(t("Thunder Gemini",30,5),t("Ice Shower",48,10),t("Electro Bolt",48,25),t("Showstorm",60,45),t("Thunder Field",100,55),t("Bonhitsusen",80,70)),
        376 to listOf(t("Electro Bolt",48,5),t("Showstorm",60,15),t("Lightning Bolt",72,40),t("Giga Freeze",72,50),t("Ice Field",100,65),t("KongouKaimandra",150,85)),
        377 to listOf(t("Dark Elemental",120,5),t("Darkness Chaos",180,20),t("Black Scewer",200,35),t("Confuse Nebula",90,50),t("Soul Plunder",160,60),t("Blast Mode",250,99)),
        378 to listOf(t("Dark Elemental",120,10),t("Crimson Cloud",150,20),t("Deadly Poison",90,30),t("Paralyze Shock",90,45),t("Dark Field",100,75),t("Melting Blood",210,90)),
        381 to listOf(t("Auto Recover",82,10),t("Impact Rush",60,20),t("Erase Magic",37,30),t("Soul Plunder",160,50),t("Final Heal",240,65),t("Omega Blade",280,99)),
        386 to listOf(t("Heat Cutter",18,5),t("Frost Cutter",18,10),t("Small Heal",16,25),t("Double Power",42,40),t("Lizard Dance",40,60)),
        387 to listOf(t("Wing Buster",18,5),t("Bug Buster",24,10),t("Big Shot",20,25),t("Fish Buster",30,40),t("Captain Cannon",38,60)),
        388 to listOf(t("Lightning Slash",18,5),t("Whirlwind",18,10),t("Hammer Rush",30,25),t("Spinal Tap",18,40),t("Maul Attack",40,60)),
        389 to listOf(t("Heat Cutter",18,5),t("Frost Cutter",18,10),t("Burn Slash",32,25),t("Cold Slash",32,40),t("Big Shot",20,60),t("Blade Twister",80,70)),
        390 to listOf(t("Bug Buster",24,5),t("Big Shot",20,10),t("Fish Buster",30,25),t("Dino Buster",40,45),t("Dramon Buster",48,60),t("Justice Strike",78,70)),
        391 to listOf(t("Lightning Slash",18,5),t("Whirlwind",18,10),t("Soul Charge",19,25),t("Speed Up",48,40),t("Cyclone Turbine",80,70)),
        392 to listOf(t("Burn Slash",32,15),t("Cold Slash",32,30),t("Mega Heal",74,40),t("Hammer Rush",30,50),t("Pinpoint Shot",54,70),t("Golden Ripper",140,85)),
        393 to listOf(t("Fish Buster",30,15),t("Dino Buster",40,25),t("Dramon Buster",48,40),t("Pinpoint Shot",54,60),t("Devil Shot",56,80),t("DynamoCannon",136,85)),
        394 to listOf(t("Heaven Hit",32,15),t("Vacuum Cannon",48,25),t("Brain Freeze",52,40),t("Misshukikou",49,65),t("Mugenhadou",148,85))
    )

    fun name(id: Int): String? = names[id]

    fun techniques(id: Int, skillLevel: Int): List<TechniqueInfo> =
        techniques[id].orEmpty().filter { it.learnLevel <= skillLevel }.takeLast(3)

    private fun t(name: String, mp: Int, level: Int) = TechniqueInfo(name, mp, level)
}
