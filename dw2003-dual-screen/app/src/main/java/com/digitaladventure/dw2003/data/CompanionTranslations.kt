package com.digitaladventure.dw2003.data

/** PT-BR and Simplified Chinese translations of the panel's English labels. */
internal object CompanionTranslations {
    fun pick(language: CompanionLanguage, spanish: String, english: String): String =
        when (language) {
            CompanionLanguage.SPANISH -> spanish
            CompanionLanguage.PORTUGUESE -> labels[english]?.first ?: english
            CompanionLanguage.CHINESE -> labels[english]?.second ?: english
            else -> english
        }

    private val labels = """
        Digimon World 2003 companion panel|Painel complementar de Digimon World 2003|数码宝贝世界 2003 辅助面板
        Exploration|Exploração|探索
        Battle|Batalha|战斗
        Management|Gerenciamento|管理
        Routes|Rotas|进化路线
        Field|Campo|地图场景
        Menu|Menu|菜单
        Map|Mapa|地图
        Fast travel|Viagem rápida|快速旅行
        Status|Estado|状态
        Asuka Server|Servidor Asuka|飞鸟服务器
        Amaterasu Server|Servidor Amaterasu|天照服务器
        Unknown server|Servidor desconhecido|未知服务器
        Central Sector|Setor Central|中央区域
        East Sector|Setor Leste|东部区域
        South Sector|Setor Sul|南部区域
        West Sector|Setor Oeste|西部区域
        North Sector|Setor Norte|北部区域
        Unknown sector|Setor desconhecido|未知区域
        Unknown area|Área desconhecida|未知地点
        Automatic|Automático|自动
        SAVE|SALVAR|保存
        NO STATE|SEM SAVE RÁPIDO|无即时存档
        LOAD|CARREGAR|读取
        2× ON|2× ATIVO|2× 已开启
        SPEED 1×|VELOCIDADE 1×|速度 1×
        SOUND OFF|SOM DESLIGADO|声音关闭
        SOUND ON|SOM LIGADO|声音开启
        NATIVE|NATIVO|原生
        2× BAT|2× EM BATALHA|战斗 2×
        Off|Desativado|关闭
        OFF|DESLIGADO|关闭
        ON|LIGADO|开启
        Dim pane|Escurecer painel|调暗面板
        Pixel shift|Deslocar pixels|像素位移
        Turn pane off|Desligar painel|关闭面板
        30 seconds|30 segundos|30 秒
        60 seconds|60 segundos|60 秒
        120 seconds|120 segundos|120 秒
        Native|Nativo|原生
        2× in battle|2× em batalha|战斗时 2×
        Always 2×|Sempre 2×|始终 2×
        Sharp (pixel)|Nítido (pixel)|锐利（像素）
        3D 2× (battle only)|3D 2× (só em batalha)|3D 2×（仅战斗）
        None|Nenhum|无
        Automatic (low-end)|Automático (aparelhos básicos)|自动（低性能设备）
        Quality|Qualidade|画质优先
        No encounters read|Nenhum encontro detectado|未检测到遭遇战
        Game top · panel bottom|Jogo em cima · painel embaixo|游戏在上 · 面板在下
        Panel top · game bottom|Painel em cima · jogo embaixo|面板在上 · 游戏在下
        Game left · panel right|Jogo à esquerda · painel à direita|游戏在左 · 面板在右
        Panel left · game right|Painel à esquerda · jogo à direita|面板在左 · 游戏在右
        Max bits|Bits ao máximo|Bits 最大值
        No random battles|Sem batalhas aleatórias|关闭随机遇敌
        Infinite HP in battle|HP infinito em batalha|战斗中 HP 无限
        Infinite MP in battle|MP infinito em batalha|战斗中 MP 无限
        Keeps 99,999,968 Bits in the Tamer RAM.|Mantém 99.999.968 Bits na RAM do domador.|在驯兽师内存中保持 99,999,968 Bits。
        Prevents field encounters. Bosses and events are unchanged.|Evita encontros no mapa. Chefes e eventos continuam disponíveis.|阻止野外遇敌，首领和剧情战斗照常触发。
        Only while the battle overlay is active.|Somente durante a batalha.|仅在战斗中生效。
        STORY|HISTÓRIA|剧情
        GAME SESSION|SESSÃO DE JOGO|游戏会话
        GAME NOT STARTED YET|JOGO AINDA NÃO INICIADO|游戏尚未开始
        Finish the intro or load a save. Area, objective and party appear once RAM holds a valid session.|Conclua a introdução ou carregue um save para ver a área, o objetivo e a equipe.|完成序章或读取存档后，即可查看地点、目标和队伍。
        REGIONAL RADAR|RADAR REGIONAL|区域雷达
        TAP THE MAP FOR FAST TRAVEL|TOQUE NO MAPA PARA VIAJAR|点击地图快速旅行
        CURRENT OBJECTIVE|OBJETIVO ATUAL|当前目标
        AUCTION · EL DORADO|LEILÃO · EL DORADO|拍卖 · EL DORADO
        DRI · AGENTS|DRI · AGENTES|DRI · 特工
        TAMER STATUS|ESTADO DO DOMADOR|驯兽师状态
        FISHING|PESCA|钓鱼
        FISHING PREVIEW|PRÉVIA DE PESCA|钓鱼预览
        EXPLORING|EXPLORANDO|探索中
        ACTIVITY|ATIVIDADE|活动
        FISHING NOW|PESCANDO|正在钓鱼
        FISHING AVAILABLE · TAP TO PREVIEW|PESCA DISPONÍVEL · TOQUE PARA VER|可以钓鱼 · 点击预览
        NO FISHING SPOT IN THIS AREA|SEM PONTO DE PESCA NESTA ÁREA|此区域没有钓鱼点
        PARTY|EQUIPE|队伍
        ENEMIES IN BATTLE|INIMIGOS EM BATALHA|战斗中的敌人
        No party in RAM yet. Load a save; on USA the panel reads the NTSC save block.|Nenhuma equipe encontrada na RAM. Carregue um save. Na versão dos EUA, o painel lê o bloco NTSC.|尚未在内存中找到队伍。请读取存档；美版读取 NTSC 存档区。
        MAX|MÁX.|最大
        MAX HP|HP MÁX.|最大 HP
        Max HP|HP máx.|最大 HP
        PARAMETERS|ATRIBUTOS|属性
        STR|FOR|力量
        DEF|DEF|防御
        SPR|ESP|精神
        WIS|SAB|智力
        SPD|VEL|速度
        ELEMENTAL|ELEMENTAIS|元素
        STATUS|ESTADOS|状态
        No catalogued techniques|Nenhuma técnica catalogada|暂无已收录的招式
        LOOT|ESPÓLIOS|掉落
        ENEMIES|INIMIGOS|敌人
        FIR|FOG|火
        WAT|ÁGU|水
        ICE|GELO|冰
        WND|VEN|风
        THD|RAI|雷
        MCH|MÁQ|机械
        DRK|ESC|暗
        TAP ▲▼ TO CHANGE BATTLE ORDER|TOQUE EM ▲▼ PARA REORDENAR A EQUIPE|点击 ▲▼ 调整出战顺序
        REORDER ONLY OUTSIDE BATTLE OR EVENTS|REORDENE FORA DE BATALHAS OU EVENTOS|只能在战斗或事件之外调整顺序
        PARTNER / STATUS|PARCEIRO / ESTADO|搭档状态
        CHA|CAR|魅力
        MAX LEVEL|NÍVEL MÁXIMO|等级已满
        LEFT|FALTAM|还需
        LEVEL|NÍVEL|等级
        DIGIVOLUTIONS · VIEW ROUTES ›|DIGIEVOLUÇÕES · VER ROTAS ›|数码进化 · 查看路线 ›
        DIGIVOLUTIONS|DIGIEVOLUÇÕES|数码进化
        NONE EQUIPPED|NENHUMA EQUIPADA|未装备进化形态
        LV|NV|等级
        PSN|VEN|毒
        PAR|PAR|麻
        CNF|CNF|乱
        SLP|SON|眠
        STRENGTH|FORÇA|力量
        DEFENSE|DEFESA|防御
        SPIRIT|ESPÍRITO|精神
        WISDOM|SABEDORIA|智力
        SPEED|VELOCIDADE|速度
        CHARISMA|CARISMA|魅力
        ELEMENTAL RESISTS|RESISTÊNCIAS ELEMENTAIS|元素抗性
        FIRE|FOGO|火
        WATER|ÁGUA|水
        WIND|VENTO|风
        THUNDER|RAIO|雷
        MACHINE|MÁQUINA|机械
        DARKNESS|ESCURIDÃO|暗
        STATUS RESISTS|RESISTÊNCIAS A ESTADOS|异常状态抗性
        POISON|VENENO|中毒
        PARALYSIS|PARALISIA|麻痹
        CONFUSION|CONFUSÃO|混乱
        SLEEP|SONO|睡眠
        ACTIVE SKILLS|TÉCNICAS ATIVAS|当前招式
        No techniques identified|Nenhuma técnica identificada|未识别到招式
        TAP TO VIEW ALL ›|TOQUE PARA VER TODAS ›|点击查看全部 ›
        POWER|PODER|威力
        EQUIPMENT AND BONUSES|EQUIPAMENTOS E BÔNUS|装备与加成
        HEAD|CABEÇA|头部
        BODY|CORPO|身体
        RIGHT HAND|MÃO DIREITA|右手
        LEFT HAND|MÃO ESQUERDA|左手
        ACCESSORY 1|ACESSÓRIO 1|饰品 1
        ACCESSORY 2|ACESSÓRIO 2|饰品 2
        — EMPTY —|— VAZIO —|— 空 —
        PARTNER|PARCEIRO|搭档
        STAGE|ETAPA|阶段
        Active|Ativa|已启用
        Locked|Bloqueada|未解锁
        RAM PROBE|INSPEÇÃO DA RAM|内存检测
        CHANGES|ALTERAÇÕES|变更
        CAPTURES|CAPTURAS|快照
        FLAWE / MAP|FLAWE / MAPA|FLAWE / 地图
        FAST TRAVEL · VISITED DESTINATIONS|VIAGEM RÁPIDA · DESTINOS VISITADOS|快速旅行 · 已访问地点
        VISITED DESTINATIONS|DESTINOS VISITADOS|已访问地点
        CLOSE|FECHAR|关闭
        OPEN MAP TAB|ABRIR MAPA|打开地图页
        Flawe fast travel is not available on Digimon World 3 USA.|A viagem rápida de Flawe não está disponível na versão americana de Digimon World 3.|美版 Digimon World 3 不支持 Flawe 快速旅行。
        Fast travel is unavailable during battles or events.|A viagem rápida fica indisponível durante batalhas ou eventos.|战斗或事件期间无法快速旅行。
        No visited destinations yet. Explore the field to add locations, or open the in-game map for Flawe's Mod travel.|Nenhum destino visitado. Explore a região ou abra o mapa do jogo para usar a viagem rápida de Flawe.|暂无已访问地点。探索区域，或打开游戏地图以使用 Flawe 快速旅行。
        MODS AND PAL CODES|MODS E CÓDIGOS PAL|模组与 PAL 代码
        Codes are applied to the emulated SLES-03936 / Flawe's Mod RAM. Use them outside critical sequences.|Os códigos alteram a RAM emulada do SLES-03936 / mod de Flawe. Use-os fora de sequências críticas.|代码会修改 SLES-03936 或 Flawe 模组的模拟内存。请避开关键剧情。
        ADD CUSTOM MOD|ADICIONAR MOD PERSONALIZADO|添加自定义模组
        Game|Jogo|游戏
        App|Aplicativo|应用
        Update DS Companion|Atualizar DS Companion|更新 DS Companion
        Install or update Harmony|Instalar ou atualizar Harmony|安装或更新 Harmony
        Return to the game|Voltar ao jogo|返回游戏
        NATIVE COMPANION FOR AYN THOR + GALAXY Z FOLD|COMPANION PARA AYN THOR E GALAXY Z FOLD|适用于 AYN THOR 和 GALAXY Z FOLD
        USER FILES|ARQUIVOS DO USUÁRIO|用户文件
        INSTALLED|INSTALADO|已安装
        NOT IMPORTED|NÃO IMPORTADO|未导入
        NO SAVE|SEM SAVE|无存档
        Return to the start screen|Voltar à tela inicial|返回开始界面
        Select BIN|Selecionar BIN|选择 BIN 文件
        Explore interface|Explorar a interface|体验界面
        Import European BIOS|Importar BIOS europeu|导入欧版 BIOS
        Import Memory Card|Importar Memory Card|导入记忆卡
        Export Memory Card|Exportar Memory Card|导出记忆卡
        Version|Versão|版本
        APP OPTIONS|OPÇÕES DO APLICATIVO|应用选项
        Fast Forward Memory Card Access|Acelerar acesso ao Memory Card|加速记忆卡读写
        Mods tab: on|Aba Mods ativada|模组页已开启
        Mods tab: hidden|Aba Mods oculta|模组页已隐藏
        When enabled, the second screen shows a Mods tab with optional PAL codes.|Quando ativada, a aba Mods exibe códigos PAL opcionais na segunda tela.|启用后，第二屏幕显示包含可选 PAL 代码的模组页。
        Screen layout|Disposição das telas|屏幕布局
        Companion language|Idioma do painel|面板语言
        Automatic follows the game's language when available.|O modo automático segue o idioma do jogo quando disponível.|自动模式会在可用时跟随游戏语言。
        Hide or show Save / Load / 2× / Sound over the game. Those buttons live on the companion pane.|Mostra ou oculta Salvar, Carregar, 2× e Som sobre o jogo. Esses botões ficam no painel complementar.|在游戏画面上显示或隐藏保存、读取、2× 和声音按钮。也可在辅助面板使用这些按钮。
        Image|Imagem|画面
        Performance|Desempenho|性能
        OLED protection|Proteção OLED|OLED 防护
        OLED delay|Tempo de espera OLED|OLED 等待时间
        Cancel|Cancelar|取消
        Continue|Continuar|继续
        Close|Fechar|关闭
        Save|Salvar|保存
        Clear|Limpar|清除
        Share|Compartilhar|分享
        Update|Atualizar|更新
        Later|Mais tarde|稍后
        Harmony update available|Atualização do Harmony disponível|Harmony 有可用更新
        Public Harmony release available|Versão pública do Harmony disponível|Harmony 有可用的公开版本
        Installed|Instalada|已安装
        On GitHub|No GitHub|GitHub 版本
        Harmony is up to date.|O Harmony está atualizado.|Harmony 已是最新版本。
        Select your original PAL DW2003 BIN. A patched copy will be saved.|Selecione seu BIN original de DW2003 PAL. Uma cópia com o patch será salva.|请选择原版 DW2003 PAL BIN。应用补丁后的副本将被保存。
        Download and install|Baixar e instalar|下载并安装
        Play Harmony|Jogar Harmony|游玩 Harmony
        Install or update Harmony|Instalar ou atualizar Harmony|安装或更新 Harmony
        Update available|Atualização disponível|有可用更新
        WHAT'S NEW|NOVIDADES|更新内容
        No release notes were provided for this version.|Esta versão não tem notas de lançamento.|此版本没有提供更新说明。
        Verifying the disc image…|Verificando a imagem do disco…|正在验证光盘镜像…
        BIN access was lost. Select it again.|O acesso ao BIN foi perdido. Selecione o arquivo novamente.|无法访问 BIN 文件。请重新选择。
        Saving quick state…|Salvando estado rápido…|正在保存即时存档…
        Loading quick state…|Carregando estado rápido…|正在读取即时存档…
        State and Memory Card loaded|Estado e Memory Card carregados|已读取即时存档和记忆卡
        Start or load a game to activate the companion panel.|Inicie ou carregue um jogo para ativar o painel.|开始游戏或读取存档以启用辅助面板。
        Open the game menu to sync Flawe's integrated walkthrough.|Abra o menu do jogo para sincronizar o guia integrado de Flawe.|打开游戏菜单以同步 Flawe 内置攻略。
        This USA version has no Flawe walkthrough. The companion does not show the mod's objectives.|A versão americana não inclui o guia de Flawe. Os objetivos do mod não ficam disponíveis no painel.|美版不包含 Flawe 攻略，辅助面板无法显示该模组的目标。
        Digimon Lab|Laboratório Digimon|数码宝贝实验室
        Asuka City|Cidade Asuka|飞鸟城
        Amaterasu City|Cidade Amaterasu|天照城
        Central Park|Parque Central|中央公园
        Seiryu City|Cidade Seiryu|青龙城
        Qing Long City|Cidade Qing Long|青龙城
        Zhu Que City|Cidade Zhu Que|朱雀城
        Bai Hu City|Cidade Bai Hu|白虎城
        Xuan Wu City|Cidade Xuan Wu|玄武城
        1H Weapon|Arma de uma mão|单手武器
        2H Weapon|Arma de duas mãos|双手武器
        Head|Cabeça|头部
        Body|Corpo|身体
        Shield|Escudo|盾牌
        Accessory|Acessório|饰品
        REQUIREMENTS|REQUISITOS|进化条件
        Story progress required|É necessário avançar na história|需要推进剧情
        TECHNIQUES (MP without accessories)|TÉCNICAS (MP sem acessórios)|招式（未计入饰品的 MP）
        LEARNED TECHNIQUES (current equipment)|TÉCNICAS APRENDIDAS (equipamento atual)|已学招式（当前装备）
        Power|Poder|威力
        Mastery DV|Domínio DV|精通进化等级
        Signature|Própria|专属招式
        Amaterasu Sewers|Esgotos de Amaterasu|天照城下水道
        Asuka City 2|Cidade Asuka 2|飞鸟城 2
        Card Shop|Loja de cartas|卡片商店
        Deep Crevice|Fenda Profunda|深裂谷
        Forest Inn Basement|Porão da Pousada da Floresta|森林旅馆地下室
        Menu Screen|Tela do menu|菜单界面
        Mobius Desert 2|Deserto de Mobius 2|莫比乌斯沙漠 2
        Piximon Screen|Tela do Piximon|妖精兽界面
        Registration Room|Sala de Registro|登记室
        Reward Screen|Tela de recompensas|奖励界面
        Save Screen|Tela de salvamento|存档界面
        Shop|Loja|商店
        South Noise Desert|Sul do Deserto Noise|噪音沙漠南部
        Starter Pack Selection|Seleção do pacote inicial|选择初始组合
        Story Sequence|Cena da história|剧情场景
        Suzaku Underground Lake|Lago Subterrâneo de Suzaku|朱雀地下湖
        Training|Treinamento|训练
        Undersea 0|Fundo do mar 0|海底 0
        Undersea 1|Fundo do mar 1|海底 1
        Undersea 2|Fundo do mar 2|海底 2
        Undersea 3|Fundo do mar 3|海底 3
        Undersea 4|Fundo do mar 4|海底 4
        Undersea 5|Fundo do mar 5|海底 5
        Undersea 6|Fundo do mar 6|海底 6
        Undersea 7|Fundo do mar 7|海底 7
        Underwater Tunnel|Túnel Submarino|水下隧道
        Zhu Que Underground Lake|Lago Subterrâneo de Zhu Que|朱雀地下湖
        A.o.A Headquarters|A.O.A. Quartel-General|A.O.A 总部
        Admin Center 1F|Centro de Administração 1F|行政中心 1F
        Admin Center 2F|Centro de Administração 2F.|行政中心 2F
        Admin Center B1F|Centro de Administração B1F|行政中心 B1F
        Amaterasu Bridge|Ponte Amaterasu|阿马泰拉苏桥
        Amaterasu Inn 1F|Amaterasu Inn 1F|阿马泰拉苏客栈1F
        Amaterasu Inn 2F|Amaterasu Inn 2F|阿马泰拉苏客栈2F
        Arena Front Desk|Recepção da Arena.|竞技场前台
        Asuka Bridge|Ponte Asuka.|阿苏卡桥
        Asuka Inn 1F|Asuka Inn 1F|阿苏卡客栈1F
        Asuka Inn 2F|Asuka Inn 2F|阿苏卡客栈2F
        Asuka Sewers|Asuka Esgotos|Asuka 污水处理器
        Bai Hu Dome|Bai Hu Dome|白虎穹顶
        Basement Stairs|Escadas de porão|地下室楼梯
        Battle Gate|Portão de batalha.|战斗门
        Bios Swamp|Pântano de Bios|沼泽
        Boot Mountain|Montanha Boot|靴子山
        Bug Maze|Labirinto de Insetos|臭虫迷宫
        Bug Maze Pit|Pit Bug Maze.|臭虫磁带坑
        Bulk Bridge|Ponte Bulk.|大桥
        Bulk Swamp|Pântano em massa|散装沼泽
        Bullet Valley|Vale da Bala|弹谷
        Byakko City|Cidade de Byakko|比亚科市
        Byakko Dome|Byakko Dome|Byakko 穹顶
        Cargo Tower|Torre de Carga|货物塔
        Catacomb|Catacumba|目录
        Chamber Room|Sala de Câmara.|会议室
        Control Room|Sala de controle.|控制室
        Dark Dungeon|Masmorra Escura|深渊
        Digimon Arena|Digimon Arena|迪基蒙竞技场
        Divermon's Lake|Lago de Divermon.|迪蒙湖(英语:Divermon's Lake)
        Duct Room 01|Quarto Duct 01.|01室
        Duct Room 02|Quarto Duct 02.|第02号Duct室
        Duct Room 03|Quarto Duct 03.|03号房间
        Duct Room 04|Quarto Duct 04.|04号DUCT房间
        Duel Island|Ilha Duel|决斗岛
        Dum Dum Factory|Fábrica Dum Dum|Dum Dum工厂
        East Station|Estação Leste.|东站
        East Wire Forest|Floresta de Fios Orientais|东线森林
        El Dorado|El Dorado.|埃尔多拉多
        Ether Jungle|Ether Jungle|以太丛林
        Fire Dungeon|Masmorra de fogo.|开火 Dungeon
        Forest Inn|Pousada Florestal|森林旅馆
        Freeze Mountain|Parada, montanha.|冻结山
        Gale Tower|Torre Gale.|盖尔塔
        Genbu City|Cidade de Genbu|源布市
        Gunslinger 1F|Gunslinger 1F|1F型炮架
        Gunslinger 2F|Gunslinger 2F|枪炮2F
        Ice Dungeon|Masmorra de Gelo|冰块敦煌
        Jungle Grave|Selva|丛林坟墓
        Jungle Shrine|Santuário da Selva|丛林神社
        Junk Shop|Loja de Lixo|烂摊子
        Kicking Forest|Chutando Floresta|踢树林
        Kulon Mine|Kulon Mine.|库隆矿
        Kulon Pit|Kulon Pit.|库伦坑
        Kulon Weapons|Armas Kulon|库伦武器
        Lake of Ice|Lago de Gelo|冰湖区
        Lamb Chop|Lamb Chop|羊肉切开
        Legendary Gym|Ginásio Lendário|传说中的健身房
        Magasta 1F|Magasta 1F|马加斯塔1F
        Magasta B1F|Magasta B1F|马加斯塔B1F
        Magasta B2F|Magasta B2F|马加斯塔B2F
        Main Lobby|Lobby Principal|主要前厅
        Master Room|Sala Mestre.|客厅
        Mirage Hall|Mirage Hall.|幻影厅
        Mirage Room|Sala Mirage.|幻影室
        Mirage Tower|Torre Mirage.|幻影塔
        Mobius Desert|Deserto de Mobius|莫比乌斯沙漠
        Mountain Inn|Pousada da Montanha|山地客栈
        Noise Desert|Deserto de Ruído|噪声沙漠
        North Badland E|Norte de Badland E|北巴德兰 E
        North Badland W|North Badland W|北巴德兰W
        Online Center|Centro Online|在线中心
        Operation Room|Sala de Operação|手术室
        Pelche Oasis|Pelche Oasis|佩尔切绿洲
        Phoenix Bay|Baía de Phoenix.|凤凰湾
        Plug Cape|Plug Cape.|插头角
        Prison Tower|Torre da Prisão|监狱塔
        Protocol Forest|Protocolo Floresta|议定书森林
        Protocol Ruins|Ruínas do Protocolo|议定书
        Pump Room|Sala de bombas.|泵房
        Qing Long Tower|Qing Long Tower|青龙塔
        Reliability Spot|Ponto de Confiabilidade|可靠性点
        Secret Room|Quarto secreto.|密室
        Secret Stairs|Escadas Secretas|秘密楼梯
        Seiryu Tower|Torre Seiryu|锡里乌塔
        Sewers|Esgotos|污水
        Shaman House|Shaman House.|沙门之家
        Shell Beach|Shell Beach.|贝壳海滩
        Smith's Shop|Smith's Shop|史密斯商店
        Snow Mountain|Montanha da Neve|雪山镇
        South Badland|Terra Bad do Sul.|南巴德兰州
        South Cape|Cabo Sul|南开普
        South Station|Estação Sul.|南车站
        Storage Room|Sala de armazenamento.|存储室
        Street Corner|Canto da Rua|街角
        Suzaku City|Cidade de Suzaku|铃木市
        Suzaku Hall|Salão Suzaku|铃木厅
        Suzaku Inn|Pousada Suzaku|铃木客栈
        Swamp Inn|Pântano Inn|沼泽旅馆
        Tranquil Swamp|Tranquilo Pântano|龙卷风沼泽
        Tyranno Valley|Vale de Tyranno|暴风谷
        Underground Cave|Caverna subterrânea|地下洞穴
        Underground Path|Caminho Subterrâneo|地下路径
        Wedge's Shop|Wedge's Shop|韦奇商店
        West Wire Forest|Floresta de Fio Oeste|西线森林
        Wind Prairie|Pradaria do Vento|风草
        Wire Forest Entrance|Entrada Florestal de Fios|森林电线入口
        Yellow Cruiser|Cruzador Amarelo.|黄色巡洋舰
        Zephyr Tower|Torre Zephyr.|峨眉塔
        Zhu Que Hall|Zhu Que Hall|朱贵堂
        Zhu Que Inn|Zhu Que Inn|朱贵客栈
    """.trimIndent().lineSequence().associate { row ->
        val (english, portuguese, chinese) = row.split('|')
        english to (portuguese to chinese)
    }
}
