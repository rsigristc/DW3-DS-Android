package com.digitaladventure.dw2003.data

/** Map IDs are hexadecimal stage identifiers, not consecutive list positions. */
object AreaCatalog {
    private val names = mapOf(
        0x1000 to "Pantalla de menú", 0x1300 to "Tienda de cartas",
        0x1400 to "Pantalla de recompensa", 0x0500 to "Selección de paquete inicial",
        0x0C01 to "Pantalla de guardado", 0x0A00 to "Entrenamiento",
        0x0D00 to "Pantalla de Piximon", 0x0E03 to "Secuencia de historia",
        0x0F00 to "Tienda", 0x0600 to "Batalla",
        0x0200 to "Ciudad Asuka", 0x0201 to "Ciudad Asuka 2", 0x0202 to "Puente Asuka",
        0x0203 to "Salón principal", 0x0204 to "Salón principal", 0x0206 to "Laboratorio Digimon",
        0x0207 to "Sala de Registro", 0x0208 to "Recepción de la Arena",
        0x0209 to "Estadio Digimon",
        0x020A to "Posada Asuka 1P", 0x020B to "Ruta subterránea", 0x020C to "Posada Asuka 2P",
        0x020D to "Herrería", 0x020E to "Chatarrería", 0x020F to "Chuleta de Cordero",
        0x0210 to "Torre de Carga", 0x0211 to "Crucero Amarillo", 0x0212 to "Túnel Acuático",
        0x0213 to "El Dorado", 0x0214 to "Centro Administrativo 1P",
        0x0215 to "Escaleras del sótano", 0x0216 to "Torre de Prisión",
        0x0217 to "Centro Administrativo 2P", 0x0218 to "Sala del Maestro",
        0x0219 to "Cuartel General A.o.A", 0x021A to "Centro Administrativo B1P",
        0x021B to "Alcantarillas de Asuka", 0x021C to "Sala de Control",
        0x021D to "Central Park", 0x021E to "Entrada del Bosque Alambre", 0x021F to "Playa de Conchas",
        0x0220 to "Cabo Conexión", 0x0221 to "Bosque Alambre Oeste", 0x0222 to "Bosque Alambre Este",
        0x0223 to "Posada del Bosque", 0x0224 to "Sótano de la Posada del Bosque",
        0x0225 to "Bosque Protocolo", 0x0226 to "Ruinas Protocolo", 0x0227 to "Lago de Divermon",
        0x0228 to "Isla del Duelo", 0x0229 to "Pradera del Viento", 0x022A to "Bosque de Patadas",
        0x022B to "Valle de Tyranno", 0x022C to "Estación Este", 0x022D to "Grieta Profunda",
        0x022E to "Ciudad Seiryu", 0x022F to "Torre Céfiro", 0x0230 to "Torre Seiryu",
        0x0231 to "Torre Gale", 0x0232 to "Estación Sur", 0x0233 to "Pantano Yermo",
        0x0234 to "Puente Yermo", 0x0235 to "Pantano Bios", 0x0236 to "Punto de Confiabilidad",
        0x0237 to "Pantano Tranquilo", 0x0238 to "Posada del Pantano", 0x0239 to "Casa del Chamán",
        0x023A to "Tumba de la Selva", 0x023B to "Bahía Fénix", 0x023C to "Jungla de Éter",
        0x023D to "Cabo Sur", 0x023E to "Ciudad Suzaku", 0x023F to "Posada Suzaku",
        0x0240 to "Sala Suzaku", 0x0241 to "Lago Subterráneo Suzaku", 0x0242 to "Santuario de la Jungla",
        0x0243 to "Catacumba", 0x0244 to "Catacumba",
        0x0245 to "Laberinto de Bichos", 0x0246 to "Agujero del Laberinto de Bichos",
        0x0247 to "Desierto Sur", 0x0248 to "Desierto del Ruido",
        0x0249 to "Oasis Pelche", 0x024A to "Desierto Norte Oeste", 0x024B to "Desierto Norte Este",
        0x024C to "Valle de las Balas", 0x024D to "Fábrica Dum Dum", 0x024E to "Sala de Conductos 01",
        0x024F to "Sala de Conductos 02", 0x0250 to "Sala de Conductos 03", 0x0251 to "Sala de Conductos 04",
        0x0252 to "Sala de Operaciones", 0x0253 to "Escaleras Secretas", 0x0254 to "Alcantarillas",
        0x0255 to "Sala Secreta", 0x0256 to "Sala de Bombeo",
        0x0257 to "Desierto del Ruido Sur", 0x0258 to "Desierto Mobius",
        0x0259 to "Desierto Mobius 2", 0x025A to "Torre de los Espejismos",
        0x025B to "Salón de los Espejismos", 0x025C to "Sala de los Espejismos",
        0x025D to "Ciudad Byakko",
        0x025E to "Cúpula Byakko", 0x025F to "Sala de Almacén", 0x0260 to "Cueva Subterránea",
        0x0261 to "Montaña de Bota", 0x0262 to "Montaña de Nieve", 0x0263 to "Posada de la Montaña",
        0x0264 to "Montaña de Hielo", 0x0265 to "Mina Kulon", 0x0266 to "Lago de Hielo",
        0x0267 to "Gimnasio Legendario", 0x0268 to "Agujero Kulon",
        0x0269 to "Armas Kulon", 0x026A to "Mazmorra de Hielo",
        0x026B to "Mazmorra de Fuego", 0x026C to "Mazmorra Oscura",
        0x026D to "Cámara", 0x026E to "Puerta de Batalla",
        0x026F to "Ciudad Genbu",
        0x02D7 to "Esquina de la Calle", 0x02D8 to "Centro Online", 0x02D9 to "Sala de Cámara",
        0x02DA to "Magasta B1P", 0x02DB to "Magasta B2P", 0x02DC to "Magasta 1P",
        0x02DD to "Gunslinger 1P", 0x02DE to "Gunslinger 2P", 0x02DF to "Sala de Control",
        0x02E0 to "Fondo Marino 0", 0x02E1 to "Fondo Marino 1", 0x02E2 to "Fondo Marino 2",
        0x02E3 to "Fondo Marino 3", 0x02E4 to "Fondo Marino 4", 0x02E5 to "Fondo Marino 5",
        0x02E6 to "Fondo Marino 6", 0x02E7 to "Fondo Marino 7",
        0x0780 to "Ciudad Amaterasu", 0x0785 to "Puente Amaterasu",
        0x0790 to "Posada Amaterasu 1P", 0x0795 to "Posada Amaterasu 2P",
        0x0800 to "Tienda de Wedge", 0x0805 to "Alcantarillas de Amaterasu",
        0x0810 to "Ciudad Qing Long", 0x0820 to "Torre Qing Long",
        0x0825 to "Ciudad Zhu Que", 0x0830 to "Posada Zhu Que",
        0x0835 to "Sala Zhu Que", 0x0840 to "Lago Subterráneo Zhu Que",
        0x0845 to "Ciudad Bai Hu", 0x0850 to "Cúpula Bai Hu", 0x0855 to "Ciudad Xuan Wu"
    )

    private val fishingSpots = setOf(0x0202, 0x021D, 0x021F, 0x0220)
    private val overlayIds = setOf(
        0x0500, 0x0600, 0x0A00, 0x0C01, 0x0D00, 0x0E03, 0x0F00, 0x1000, 0x1300, 0x1400
    )
    private val blockingEvents = setOf(0x0500, 0x0600, 0x0A00, 0x0C01, 0x0D00, 0x0E03, 0x1400)

    fun knownName(areaId: Int): String? = names[areaId]
    fun name(areaId: Int): String = knownName(areaId) ?: "Área 0x${hex(areaId)}"
    fun supportsFishing(areaId: Int): Boolean = areaId in fishingSpots
    fun isOverlay(areaId: Int): Boolean = areaId in overlayIds
    fun isBlockingEvent(areaId: Int): Boolean = areaId in blockingEvents
    fun isField(areaId: Int): Boolean = areaId != 0 && !isOverlay(areaId)
    fun knownFieldIds(): List<Int> = names.keys.filter(::isField).sorted()
    fun hex(areaId: Int): String = areaId.toString(16).uppercase().padStart(4, '0')
}
