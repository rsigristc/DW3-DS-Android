package com.digitaladventure.dw2003.data

data class EquipmentInfo(val name: String, val type: String, val stats: String)

object EquipmentCatalog {
    private val items = mapOf(
        92 to EquipmentInfo("Short Sword", "Arma 1 mano", "+14 FUE · +15 CAR"),
        93 to EquipmentInfo("Zanden Sword", "Arma 1 mano", "+26 FUE · +20 CAR"),
        94 to EquipmentInfo("Crimson Blade", "Arma 1 mano", "+50 FUE · +26 CAR"),
        95 to EquipmentInfo("Mighty Blade", "Arma 1 mano", "+74 FUE · +30 CAR"),
        96 to EquipmentInfo("Shiratorimaru", "Arma 1 mano", "+70 FUE · +41 CAR"),
        97 to EquipmentInfo("Shine Blade", "Arma 1 mano", "+116 FUE · +7 SAB · +51 CAR"),
        98 to EquipmentInfo("Ronin Blade", "Arma 1 mano", "+120 FUE · +55 CAR"),
        99 to EquipmentInfo("Onimaru", "Arma 1 mano", "+130 FUE · +54 CAR"),
        100 to EquipmentInfo("Tenjinmaru", "Arma 1 mano", "+140 FUE · +56 CAR"),
        101 to EquipmentInfo("Fuujin Blade", "Arma 1 mano", "+150 FUE · +5 DEF · +58 CAR"),
        102 to EquipmentInfo("Rusty Katana", "Arma 1 mano", "+20 FUE · +30 CAR"),
        103 to EquipmentInfo("Kotetsu", "Arma 1 mano", "+60 FUE · +40 CAR"),
        104 to EquipmentInfo("Masamune", "Arma 1 mano", "+100 FUE · +50 CAR"),
        105 to EquipmentInfo("Muramasa", "Arma 1 mano", "+160 FUE · +60 CAR"),
        106 to EquipmentInfo("Leather Glove", "Arma 1 mano", "+12 FUE · +15 CAR"),
        107 to EquipmentInfo("Cat Glove", "Arma 1 mano", "+24 FUE · +18 CAR"),
        108 to EquipmentInfo("Iron Glove", "Arma 1 mano", "+46 FUE · -4 VEL · +25 CAR"),
        109 to EquipmentInfo("Needle Glove", "Arma 1 mano", "+68 FUE · +31 CAR"),
        110 to EquipmentInfo("Power Arm", "Arma 1 mano", "+90 FUE · +5 DEF · +40 CAR"),
        111 to EquipmentInfo("Tiger Glove", "Arma 1 mano", "+110 FUE · +52 CAR"),
        112 to EquipmentInfo("Wild Glove", "Arma 1 mano", "+130 FUE · +8 DEF · +55 CAR"),
        113 to EquipmentInfo("Kaiser Fist", "Arma 1 mano", "+150 FUE · +54 CAR"),
        114 to EquipmentInfo("Tempest Arm", "Arma 1 mano", "+160 FUE · +10 DEF · +56 CAR"),
        115 to EquipmentInfo("Raijin Fist", "Arma 1 mano", "+180 FUE · +58 CAR"),
        116 to EquipmentInfo("Rusty Glove", "Arma 1 mano", "+20 FUE · +30 CAR"),
        117 to EquipmentInfo("Zodiac", "Arma 1 mano", "+60 FUE · +40 CAR"),
        118 to EquipmentInfo("Prominence", "Arma 1 mano", "+140 FUE · +50 CAR"),
        119 to EquipmentInfo("Super Nova", "Arma 1 mano", "+200 FUE · +60 CAR"),
        120 to EquipmentInfo("Handgun", "Arma 1 mano", "+100 FUE · +30 CAR"),
        121 to EquipmentInfo("Machinegun", "Arma 1 mano", "+114 FUE · +35 CAR"),
        122 to EquipmentInfo("Shotgun", "Arma 1 mano", "+120 FUE · +39 CAR"),
        123 to EquipmentInfo("Psycho Blaster", "Arma 1 mano", "+140 FUE · -5 ESP · +50 CAR"),
        124 to EquipmentInfo("Sniper Cannon", "Arma 1 mano", "+144 FUE · +55 CAR"),
        125 to EquipmentInfo("Cluster Cannon", "Arma 1 mano", "+150 FUE · +54 CAR"),
        126 to EquipmentInfo("Laser Cannon", "Arma 1 mano", "+160 FUE · +56 CAR"),
        127 to EquipmentInfo("Suijin Cannon", "Arma 1 mano", "+170 FUE · +58 CAR"),
        128 to EquipmentInfo("Rusty Rifle", "Arma 1 mano", "+20 FUE · +30 CAR"),
        129 to EquipmentInfo("Justice", "Arma 1 mano", "+60 FUE · +40 CAR"),
        130 to EquipmentInfo("Judgment", "Arma 1 mano", "+120 FUE · +50 CAR"),
        131 to EquipmentInfo("Punishment", "Arma 1 mano", "+180 FUE · +60 CAR"),
        132 to EquipmentInfo("Wooden Rod", "Arma 1 mano", "+40 FUE · +3 ESP · +30 CAR"),
        133 to EquipmentInfo("Magician Rod", "Arma 1 mano", "+54 FUE · +5 ESP · +37 CAR"),
        134 to EquipmentInfo("Bone Rod", "Arma 1 mano", "+60 FUE · +7 ESP · +39 CAR"),
        135 to EquipmentInfo("Misty Rod", "Arma 1 mano", "+80 FUE · +10 ESP · +51 CAR"),
        136 to EquipmentInfo("Divine Rod", "Arma 1 mano", "+84 FUE · +12 SAB · +55 CAR"),
        137 to EquipmentInfo("Yin Yang Wand", "Arma 1 mano", "+84 FUE · +12 ESP · +55 CAR"),
        138 to EquipmentInfo("Khakkhara", "Arma 1 mano", "+90 FUE · +14 ESP · +54 CAR"),
        139 to EquipmentInfo("Crystal Rod", "Arma 1 mano", "+100 FUE · +16 ESP · +56 CAR"),
        140 to EquipmentInfo("Millenium", "Arma 1 mano", "+110 FUE · +20 ESP · +58 CAR"),
        141 to EquipmentInfo("Old Wand", "Arma 1 mano", "+12 FUE · +6 ESP · +30 CAR"),
        142 to EquipmentInfo("Twinkle", "Arma 1 mano", "+36 FUE · +12 ESP · +40 CAR"),
        143 to EquipmentInfo("Immaculate", "Arma 1 mano", "+74 FUE · +18 ESP · +50 CAR"),
        144 to EquipmentInfo("Eternally", "Arma 1 mano", "+120 FUE · +24 ESP · +60 CAR"),
        145 to EquipmentInfo("Wolf Fang", "Arma 1 mano", "+74 FUE · +30 CAR"),
        146 to EquipmentInfo("Ghost Fang", "Arma 1 mano", "+94 FUE · +34 CAR"),
        147 to EquipmentInfo("Cerberus Fang", "Arma 1 mano", "+100 FUE · +43 CAR"),
        148 to EquipmentInfo("Evil Fang", "Arma 1 mano", "+116 FUE · +50 CAR"),
        149 to EquipmentInfo("Hazard Fang", "Arma 1 mano", "+120 FUE · +55 CAR"),
        150 to EquipmentInfo("Saber Fang", "Arma 1 mano", "+100 FUE · +54 CAR"),
        151 to EquipmentInfo("Venom Fang", "Arma 1 mano", "+140 FUE · +56 CAR"),
        152 to EquipmentInfo("Belial Fang", "Arma 1 mano", "+150 FUE · +58 CAR"),
        153 to EquipmentInfo("Old Claw", "Arma 1 mano", "+20 FUE · +30 CAR"),
        154 to EquipmentInfo("Vanquish", "Arma 1 mano", "+60 FUE · +40 CAR"),
        155 to EquipmentInfo("Destruction", "Arma 1 mano", "+100 FUE · +50 CAR"),
        156 to EquipmentInfo("Invincible", "Arma 1 mano", "+160 FUE · +60 CAR"),
        157 to EquipmentInfo("Dagger", "Arma 1 mano", "+10 FUE · +15 CAR"),
        158 to EquipmentInfo("Shishioumaru", "Arma 1 mano", "+20 FUE · +22 CAR"),
        159 to EquipmentInfo("Long Sword", "Arma 1 mano", "+40 FUE · +25 CAR"),
        160 to EquipmentInfo("Shamshir", "Arma 1 mano", "+60 FUE · +33 CAR"),
        161 to EquipmentInfo("Wing Sword", "Arma 1 mano", "+80 FUE · -5 DEF · +5 VEL · +42 CAR"),
        162 to EquipmentInfo("Kulon Sword", "Arma 1 mano", "+100 FUE · +49 CAR"),
        163 to EquipmentInfo("Brave Sword", "Arma 1 mano", "+110 FUE · +55 CAR"),
        164 to EquipmentInfo("Excalibur", "Arma 1 mano", "+120 FUE · +54 CAR"),
        165 to EquipmentInfo("Grey Sword", "Arma 1 mano", "+130 FUE · +56 CAR"),
        166 to EquipmentInfo("Fenrir Sword", "Arma 1 mano", "+140 FUE · +10 VEL · +58 CAR"),
        167 to EquipmentInfo("Bamboo Spear", "Arma 1 mano", "+8 FUE · +10 CAR"),
        168 to EquipmentInfo("Spear", "Arma 1 mano", "+16 FUE · +16 CAR"),
        169 to EquipmentInfo("Fairy Tale", "Arma 1 mano", "+34 FUE · +23 CAR"),
        170 to EquipmentInfo("Partisan", "Arma 1 mano", "+50 FUE · +25 CAR"),
        171 to EquipmentInfo("Divine Lance", "Arma 1 mano", "+70 FUE · +34 CAR"),
        172 to EquipmentInfo("Trent", "Arma 1 mano", "+90 FUE · +44 CAR"),
        173 to EquipmentInfo("Vampire Lance", "Arma 1 mano", "+100 FUE · +50 CAR"),
        174 to EquipmentInfo("Royal Spear", "Arma 1 mano", "+120 FUE · +52 CAR"),
        175 to EquipmentInfo("Orochi Spear", "Arma 1 mano", "+130 FUE · +54 CAR"),
        176 to EquipmentInfo("Claymore", "Arma 2 manos", "+220 FUE · -4 VEL · +60 CAR"),
        177 to EquipmentInfo("Chain Saw", "Arma 2 manos", "+276 FUE · -6 VEL · +72 CAR"),
        178 to EquipmentInfo("Zweihander", "Arma 2 manos", "+292 FUE · -8 VEL · +90 CAR"),
        179 to EquipmentInfo("Flamberge", "Arma 2 manos", "+352 FUE · -10 VEL · +104 CAR"),
        180 to EquipmentInfo("Rock Breaker", "Arma 2 manos", "+408 FUE · -12 VEL · +108 CAR"),
        181 to EquipmentInfo("Atlas Buster", "Arma 2 manos", "+464 FUE · -14 VEL · +112 CAR"),
        182 to EquipmentInfo("Gulf Sword", "Arma 2 manos", "+520 FUE · -16 VEL · +116 CAR"),
        183 to EquipmentInfo("Halberd", "Arma 2 manos", "+184 FUE · -4 VEL · +50 CAR"),
        184 to EquipmentInfo("Naginata", "Arma 2 manos", "+224 FUE · -6 VEL · +58 CAR"),
        185 to EquipmentInfo("Berdys", "Arma 2 manos", "+240 FUE · -8 VEL · +70 CAR"),
        186 to EquipmentInfo("Soul Chopper", "Arma 2 manos", "+280 FUE · -10 VEL · +96 CAR"),
        187 to EquipmentInfo("Ryuuzanmaru", "Arma 2 manos", "+380 FUE · -12 VEL · +100 CAR"),
        188 to EquipmentInfo("Gaea Halberd", "Arma 2 manos", "+420 FUE · -14 VEL · +104 CAR"),
        189 to EquipmentInfo("Omega Halberd", "Arma 2 manos", "+448 FUE · -16 VEL · +108 CAR"),
        190 to EquipmentInfo("Long Bow", "Arma 2 manos", "+52 FUE · +20 CAR"),
        191 to EquipmentInfo("Shuriken", "Arma 2 manos", "+96 FUE · +38 CAR"),
        192 to EquipmentInfo("Crossbow", "Arma 2 manos", "+160 FUE · +40 CAR"),
        193 to EquipmentInfo("Angel Bow", "Arma 2 manos", "+220 FUE · +52 CAR"),
        194 to EquipmentInfo("Raven Bow", "Arma 2 manos", "+292 FUE · +72 CAR"),
        195 to EquipmentInfo("Lightning Bow", "Arma 2 manos", "+352 FUE · +16 VEL · +90 CAR"),
        196 to EquipmentInfo("Seraphic Bow", "Arma 2 manos", "+408 FUE · +100 CAR"),
        197 to EquipmentInfo("Garuru Cannon", "Arma 2 manos", "+464 FUE · +104 CAR"),
        198 to EquipmentInfo("Positron Cannon", "Arma 2 manos", "+520 FUE · +108 CAR"),
        199 to EquipmentInfo("Mini Guard", "Arma 1 mano", "+46 FUE · +8 DEF · +25 CAR"),
        200 to EquipmentInfo("Pulse Guard", "Arma 1 mano", "+60 FUE · +10 DEF · +30 CAR"),
        201 to EquipmentInfo("Metal Guard", "Arma 1 mano", "+66 FUE · +12 DEF · +35 CAR"),
        202 to EquipmentInfo("Mighty Guard", "Arma 1 mano", "+86 FUE · +14 DEF · +44 CAR"),
        203 to EquipmentInfo("Dramon Guard", "Arma 1 mano", "+92 FUE · +16 DEF · +55 CAR"),
        204 to EquipmentInfo("Mach Guard", "Arma 1 mano", "+100 FUE · +18 DEF · +50 CAR"),
        205 to EquipmentInfo("Mega Guard", "Arma 1 mano", "+110 FUE · +20 DEF · +52 CAR"),
        206 to EquipmentInfo("Deus Guard", "Arma 1 mano", "+120 FUE · +24 DEF · +54 CAR"),
        207 to EquipmentInfo("Metal Horn", "Cabeza", "+50 FUE · +4 DEF · -2 VEL · +30 CAR"),
        208 to EquipmentInfo("Drill Horn", "Cabeza", "+66 FUE · +5 DEF · -1 SAB · +37 CAR"),
        209 to EquipmentInfo("Kabuteri Horn", "Cabeza", "+70 FUE · +6 DEF · -2 SAB · +41 CAR"),
        210 to EquipmentInfo("Shock Horn", "Cabeza", "+90 FUE · +7 DEF · -3 SAB · +49 CAR"),
        211 to EquipmentInfo("Dramon Horn", "Cabeza", "+100 FUE · +8 DEF · +55 CAR"),
        212 to EquipmentInfo("Scissor Horn", "Cabeza", "+110 FUE · +9 DEF · -4 SAB · +54 CAR"),
        213 to EquipmentInfo("Trihorn", "Cabeza", "+120 FUE · +10 DEF · -5 SAB · +49 CAR"),
        214 to EquipmentInfo("Glorious Horn", "Cabeza", "+130 FUE · +12 DEF · +58 CAR"),
        215 to EquipmentInfo("Bandanna", "Cabeza", "+4 DEF · +10 CAR"),
        216 to EquipmentInfo("Sun Visor", "Cabeza", "+7 DEF · +16 CAR"),
        217 to EquipmentInfo("Baseball Cap", "Cabeza", "+10 DEF · +20 CAR"),
        218 to EquipmentInfo("School Cap", "Cabeza", "+13 DEF · +7 SAB · +24 CAR"),
        219 to EquipmentInfo("Wool Cap", "Cabeza", "+16 DEF · +28 CAR · +10 HIELO"),
        220 to EquipmentInfo("Kung Fu Cap", "Cabeza", "+19 DEF · +8 VEL · +34 CAR"),
        221 to EquipmentInfo("Yin Yang Hat", "Cabeza", "+22 DEF · +6 ESP · +50 CAR"),
        222 to EquipmentInfo("Sniper Goggle", "Cabeza", "+23 DEF · +50 CAR"),
        223 to EquipmentInfo("Night Vision", "Cabeza", "+24 DEF · +40 CAR"),
        224 to EquipmentInfo("Beret", "Cabeza", "+24 DEF · +10 SAB · +46 CAR"),
        225 to EquipmentInfo("Red Cap", "Cabeza", "+27 DEF · +8 ESP · +50 CAR"),
        226 to EquipmentInfo("Ribbon", "Cabeza", "+5 DEF · +10 CAR"),
        227 to EquipmentInfo("Cat Ears", "Cabeza", "+8 DEF · +15 CAR"),
        228 to EquipmentInfo("Headband", "Cabeza", "+12 DEF · +5 ESP · +18 CAR"),
        229 to EquipmentInfo("Mythril Crown", "Cabeza", "+14 DEF · +27 CAR"),
        230 to EquipmentInfo("Magical Crown", "Cabeza", "+18 DEF · +30 CAR"),
        231 to EquipmentInfo("Shaman Mask", "Cabeza", "+20 DEF · +36 CAR"),
        232 to EquipmentInfo("Divine Crown", "Cabeza", "+18 DEF · +30 CAR"),
        233 to EquipmentInfo("Angel Ring", "Cabeza", "+26 DEF · +40 CAR"),
        234 to EquipmentInfo("Royal Crown", "Cabeza", "+29 DEF · +46 CAR"),
        235 to EquipmentInfo("Goddess Crown", "Cabeza", "+33 DEF · +50 CAR"),
        236 to EquipmentInfo("Hide Helmet", "Cabeza", "+6 DEF · +15 CAR"),
        237 to EquipmentInfo("Tin Helmet", "Cabeza", "+9 DEF · +20 CAR"),
        238 to EquipmentInfo("Iron Helmet", "Cabeza", "+15 DEF · -3 VEL · +25 CAR"),
        239 to EquipmentInfo("Knight Helmet", "Cabeza", "+18 DEF · +30 CAR"),
        240 to EquipmentInfo("Wing Helmet", "Cabeza", "+19 DEF · +36 CAR · +10 VIENTO"),
        241 to EquipmentInfo("Kulon Helmet", "Cabeza", "+24 DEF · +39 CAR · +5 MÁQ"),
        242 to EquipmentInfo("Wild Helmet", "Cabeza", "+27 DEF · +55 CAR"),
        243 to EquipmentInfo("Ronin Helmet", "Cabeza", "+28 DEF · +55 CAR"),
        244 to EquipmentInfo("Brave Helmet", "Cabeza", "+28 DEF · +55 CAR"),
        245 to EquipmentInfo("Hazard Helmet", "Cabeza", "+27 DEF · +55 CAR"),
        246 to EquipmentInfo("D-Tama Helmet", "Cabeza", "+29 DEF · +45 CAR"),
        247 to EquipmentInfo("Matrix Helmet", "Cabeza", "+34 DEF · +52 CAR"),
        248 to EquipmentInfo("Mugen Helmet", "Cabeza", "+39 DEF · +55 CAR"),
        249 to EquipmentInfo("Leather Coat", "Cuerpo", "+5 DEF · +15 CAR"),
        250 to EquipmentInfo("Gym Suit", "Cuerpo", "+10 DEF · +19 CAR"),
        251 to EquipmentInfo("Priest Robe", "Cuerpo", "+14 DEF · +5 SAB · +26 CAR"),
        252 to EquipmentInfo("Rubber Suit", "Cuerpo", "+18 DEF · +30 CAR · +8 RAYO"),
        253 to EquipmentInfo("Dark Cloak", "Cuerpo", "+21 DEF · +37 CAR · +10 OSC"),
        254 to EquipmentInfo("Down Jacket", "Cuerpo", "+25 DEF · +39 CAR · +10 HIELO"),
        255 to EquipmentInfo("Divine Robe", "Cuerpo", "+27 DEF · +55 CAR"),
        256 to EquipmentInfo("Sniper Suit", "Cuerpo", "+28 DEF · +55 CAR"),
        257 to EquipmentInfo("Yin Yang Suit", "Cuerpo", "+27 DEF · +55 CAR"),
        258 to EquipmentInfo("Wild Suit", "Cuerpo", "+28 DEF · +55 CAR"),
        259 to EquipmentInfo("Body Armor", "Cuerpo", "+30 DEF · +44 CAR · +10 MÁQ"),
        260 to EquipmentInfo("Mirage Robe", "Cuerpo", "+35 DEF · +50 CAR"),
        261 to EquipmentInfo("King’s Mantle", "Cuerpo", "+40 DEF · +55 CAR"),
        262 to EquipmentInfo("Leather Mail", "Cuerpo", "+8 DEF · +10 CAR"),
        263 to EquipmentInfo("Tin Mail", "Cuerpo", "+12 DEF · +15 CAR"),
        264 to EquipmentInfo("Iron Armor", "Cuerpo", "+18 DEF · -5 VEL · +20 CAR"),
        265 to EquipmentInfo("Digitama Mail", "Cuerpo", "+20 DEF · +24 CAR"),
        266 to EquipmentInfo("Dark Armor", "Cuerpo", "+25 DEF · +31 CAR · +7 OSC"),
        267 to EquipmentInfo("Kulon Armor", "Cuerpo", "+30 DEF · +34 CAR · +8 MÁQ"),
        268 to EquipmentInfo("Dramon Armor", "Cuerpo", "+39 DEF · +50 CAR"),
        269 to EquipmentInfo("Ronin Armor", "Cuerpo", "+38 DEF · +50 CAR"),
        270 to EquipmentInfo("Hazard Armor", "Cuerpo", "+37 DEF · +50 CAR"),
        271 to EquipmentInfo("Brave Armor", "Cuerpo", "+38 DEF · +50 CAR"),
        272 to EquipmentInfo("Beam Armor", "Cuerpo", "+10 FUEGO · +34 DEF · +42 CAR"),
        273 to EquipmentInfo("Misty Armor", "Cuerpo", "+44 DEF · +45 CAR"),
        274 to EquipmentInfo("Crimson Mail", "Cuerpo", "+48 DEF · +50 CAR"),
        275 to EquipmentInfo("Buckler", "Escudo", "+7 DEF · +10 CAR"),
        276 to EquipmentInfo("Tin Shield", "Escudo", "+10 DEF · +15 CAR"),
        277 to EquipmentInfo("Iron Shield", "Escudo", "+15 DEF · -3 VEL · +20 CAR"),
        278 to EquipmentInfo("Knight Shield", "Escudo", "+18 DEF · +25 CAR"),
        279 to EquipmentInfo("Guard Barrier", "Escudo", "+22 DEF · +28 CAR"),
        280 to EquipmentInfo("Kulon Shield", "Escudo", "+25 DEF · +34 CAR · +7 MÁQ"),
        281 to EquipmentInfo("Divine Barrier", "Escudo", "+24 DEF · +45 CAR"),
        282 to EquipmentInfo("Sniper Shield", "Arma 1 mano", "+29 DEF · +45 CAR"),
        283 to EquipmentInfo("Yin Yang Ward", "Escudo", "+27 DEF · +10 ESP · +45 CAR"),
        284 to EquipmentInfo("Ronin Shield", "Escudo", "+30 DEF · +45 CAR"),
        285 to EquipmentInfo("Wild Shield", "Escudo", "+29 DEF · +45 CAR"),
        286 to EquipmentInfo("Brave Shield", "Escudo", "+30 DEF · +45 CAR"),
        287 to EquipmentInfo("Hazard Shield", "Escudo", "+28 DEF · +45 CAR"),
        288 to EquipmentInfo("Dramon Shield", "Escudo", "+30 DEF · +45 CAR"),
        289 to EquipmentInfo("Beam Shield", "Escudo", "+28 DEF · +8 FUEGO · +42 CAR"),
        290 to EquipmentInfo("High Security", "Escudo", "+35 DEF · +45 CAR"),
        291 to EquipmentInfo("Apocalypse", "Escudo", "+40 DEF · +50 CAR"),
        292 to EquipmentInfo("Power Gem", "Accesorio", "+10 FUE · +3 CAR"),
        293 to EquipmentInfo("Power Ring", "Accesorio", "+20 FUE · +5 CAR"),
        294 to EquipmentInfo("Guard Gem", "Accesorio", "+10 DEF · +2 CAR"),
        295 to EquipmentInfo("Guard Ring", "Accesorio", "+20 DEF · +5 CAR"),
        296 to EquipmentInfo("Spirit Gem", "Accesorio", "+10 ESP · +2 CAR"),
        297 to EquipmentInfo("Spirit Ring", "Accesorio", "+20 ESP · +5 CAR"),
        298 to EquipmentInfo("Wisdom Gem", "Accesorio", "+10 SAB · +2 CAR"),
        299 to EquipmentInfo("Wisdom Ring", "Accesorio", "+20 SAB · +5 CAR"),
        300 to EquipmentInfo("Boost Gem", "Accesorio", "+10 VEL · +3 CAR"),
        301 to EquipmentInfo("Boost Ring", "Accesorio", "+20 VEL · +5 CAR"),
        302 to EquipmentInfo("Charisma Gem", "Accesorio", "+20 CAR"),
        303 to EquipmentInfo("Charisma Ring", "Accesorio", "+30 CAR"),
        304 to EquipmentInfo("Flame Ring", "Accesorio", "+20 FUEGO · +3 CAR"),
        305 to EquipmentInfo("Water Ring", "Accesorio", "+20 AGUA · +3 CAR"),
        306 to EquipmentInfo("Ice Ring", "Accesorio", "+20 HIELO · +2 CAR"),
        307 to EquipmentInfo("Wind Ring", "Accesorio", "+20 VIENTO · +2 CAR"),
        308 to EquipmentInfo("Thunder Ring", "Accesorio", "+20 RAYO · +3 CAR"),
        309 to EquipmentInfo("Machine Ring", "Accesorio", "+20 MÁQ · +3 CAR"),
        310 to EquipmentInfo("Dark Ring", "Accesorio", "+20 OSC · +3 CAR"),
        311 to EquipmentInfo("Antidote Ring", "Accesorio", "+2 CAR"),
        312 to EquipmentInfo("Revive Ring", "Accesorio", "+2 CAR"),
        313 to EquipmentInfo("Awake Ring", "Accesorio", "+2 CAR"),
        314 to EquipmentInfo("Sober Ring", "Accesorio", "+2 CAR"),
        315 to EquipmentInfo("Prayer Ring", "Accesorio", "+3 CAR"),
        316 to EquipmentInfo("Multi Crest", "Accesorio", "+5 CAR"),
        317 to EquipmentInfo("Search Crest", "Accesorio", "+4 CAR"),
        318 to EquipmentInfo("Counter Crest", "Accesorio", "+3 CAR"),
        319 to EquipmentInfo("Binder Crest", "Accesorio", "+3 CAR"),
        320 to EquipmentInfo("Recover Crest", "Accesorio", "+4 CAR"),
        321 to EquipmentInfo("EXP Adapter", "Accesorio", "+2 CAR"),
        322 to EquipmentInfo("BIT Adapter", "Accesorio", "Sin bonificación directa"),
        323 to EquipmentInfo("MP Proxy", "Accesorio", "Sin bonificación directa"),
        324 to EquipmentInfo("MP Mega Proxy", "Accesorio", "Sin bonificación directa"),
        325 to EquipmentInfo("HP Proxy", "Accesorio", "Sin bonificación directa"),
        326 to EquipmentInfo("HP Mega Proxy", "Accesorio", "Sin bonificación directa"),
        327 to EquipmentInfo("Hack Sticker", "Accesorio", "+2 CAR"),
        328 to EquipmentInfo("Hack System", "Accesorio", "+4 CAR"),
        329 to EquipmentInfo("Over Clocked", "Accesorio", "+3 CAR"),
        330 to EquipmentInfo("Over Load", "Accesorio", "+5 CAR"),
        331 to EquipmentInfo("Glasses", "Accesorio", "+2 CAR"),
        332 to EquipmentInfo("Goggles", "Accesorio", "+5 CAR"),
        333 to EquipmentInfo("Dance Feather", "Accesorio", "Sin bonificación directa"),
        334 to EquipmentInfo("Dance Wing", "Accesorio", "+1 CAR"),
        335 to EquipmentInfo("Runner Sandals", "Accesorio", "Sin bonificación directa"),
        336 to EquipmentInfo("Runner Shoes", "Accesorio", "+1 CAR"),
        337 to EquipmentInfo("Training Book", "Accesorio", "Sin bonificación directa"),
        338 to EquipmentInfo("Power Brace", "Accesorio", "Sin bonificación directa"),
        339 to EquipmentInfo("Fire Power 1", "Accesorio", "+3 CAR"),
        340 to EquipmentInfo("Fire Power 2", "Accesorio", "+5 CAR"),
        341 to EquipmentInfo("Fire Power S", "Accesorio", "+7 CAR"),
        342 to EquipmentInfo("Water Power 1", "Accesorio", "+2 CAR"),
        343 to EquipmentInfo("Water Power 2", "Accesorio", "+4 CAR"),
        344 to EquipmentInfo("Water Power S", "Accesorio", "+6 CAR"),
        345 to EquipmentInfo("Ice Power 1", "Accesorio", "+2 CAR"),
        346 to EquipmentInfo("Ice Power 2", "Accesorio", "+4 CAR"),
        347 to EquipmentInfo("Ice Power S", "Accesorio", "+6 CAR"),
        348 to EquipmentInfo("Wind Power 1", "Accesorio", "+2 CAR"),
        349 to EquipmentInfo("Wind Power 2", "Accesorio", "+4 CAR"),
        350 to EquipmentInfo("Wind Power S", "Accesorio", "+6 CAR"),
        351 to EquipmentInfo("Bolt Power 1", "Accesorio", "+3 CAR"),
        352 to EquipmentInfo("Bolt Power 2", "Accesorio", "+5 CAR"),
        353 to EquipmentInfo("Bolt Power S", "Accesorio", "+7 CAR"),
        354 to EquipmentInfo("Metal Power 1", "Accesorio", "+3 CAR"),
        355 to EquipmentInfo("Metal Power 2", "Accesorio", "+5 CAR"),
        356 to EquipmentInfo("Metal Power S", "Accesorio", "+7 CAR"),
        357 to EquipmentInfo("Dark Power 1", "Accesorio", "+3 CAR"),
        358 to EquipmentInfo("Dark Power 2", "Accesorio", "+5 CAR"),
        359 to EquipmentInfo("Dark Power S", "Accesorio", "+7 CAR")
    )

    fun get(id: Int): EquipmentInfo? = items[id]
}

data class EquipmentBonuses(
    val strength: Int = 0,
    val defense: Int = 0,
    val spirit: Int = 0,
    val wisdom: Int = 0,
    val speed: Int = 0,
    val charisma: Int = 0,
    val fire: Int = 0,
    val water: Int = 0,
    val ice: Int = 0,
    val wind: Int = 0,
    val thunder: Int = 0,
    val machine: Int = 0,
    val dark: Int = 0
) {
    operator fun plus(other: EquipmentBonuses) = EquipmentBonuses(
        strength + other.strength,
        defense + other.defense,
        spirit + other.spirit,
        wisdom + other.wisdom,
        speed + other.speed,
        charisma + other.charisma,
        fire + other.fire,
        water + other.water,
        ice + other.ice,
        wind + other.wind,
        thunder + other.thunder,
        machine + other.machine,
        dark + other.dark
    )

    fun parameter(index: Int): Int = when (index) {
        0 -> strength
        1 -> defense
        2 -> spirit
        3 -> wisdom
        4 -> speed
        5 -> charisma
        else -> 0
    }

    fun resistance(index: Int): Int = when (index) {
        0 -> fire
        1 -> water
        2 -> ice
        3 -> wind
        4 -> thunder
        5 -> machine
        6 -> dark
        else -> 0
    }

    companion object {
        private val TOKEN_PATTERN = Regex(
            """([+-]?\d+)\s+(FUEGO|AGUA|HIELO|VIENTO|RAYO|MÁQUINA|OSCURIDAD|MÁQ|OSC|FUE|DEF|ESP|SAB|VEL|CAR)"""
        )

        fun parse(stats: String): EquipmentBonuses {
            var bonus = EquipmentBonuses()
            TOKEN_PATTERN.findAll(stats).forEach { match ->
                val amount = match.groupValues[1].toInt()
                bonus = when (match.groupValues[2]) {
                    "FUE" -> bonus.copy(strength = bonus.strength + amount)
                    "DEF" -> bonus.copy(defense = bonus.defense + amount)
                    "ESP" -> bonus.copy(spirit = bonus.spirit + amount)
                    "SAB" -> bonus.copy(wisdom = bonus.wisdom + amount)
                    "VEL" -> bonus.copy(speed = bonus.speed + amount)
                    "CAR" -> bonus.copy(charisma = bonus.charisma + amount)
                    "FUEGO" -> bonus.copy(fire = bonus.fire + amount)
                    "AGUA" -> bonus.copy(water = bonus.water + amount)
                    "HIELO" -> bonus.copy(ice = bonus.ice + amount)
                    "VIENTO" -> bonus.copy(wind = bonus.wind + amount)
                    "RAYO" -> bonus.copy(thunder = bonus.thunder + amount)
                    "MÁQUINA", "MÁQ" -> bonus.copy(machine = bonus.machine + amount)
                    "OSCURIDAD", "OSC" -> bonus.copy(dark = bonus.dark + amount)
                    else -> bonus
                }
            }
            return bonus
        }

        fun of(items: List<EquipmentInfo?>): EquipmentBonuses =
            items.mapNotNull { item -> item?.let { parse(it.stats) } }
                .fold(EquipmentBonuses()) { total, bonus -> total + bonus }
    }
}
