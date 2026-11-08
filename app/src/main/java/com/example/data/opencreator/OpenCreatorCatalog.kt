package com.example.data.opencreator

/**
 * Data structures and catalog parsed directly from the OpenCreator source repository
 * Attachment Path: public/assets/aistudio/OpenCreator_All_Source_Code_8b88ab46.txt
 */
data class OpenCreatorVideoPreset(
    val id: String,
    val titleEn: String,
    val titleZh: String,
    val descriptionEn: String,
    val descriptionZh: String,
    val tags: List<String>,
    val isVertical: Boolean,
    val defaultPrompt: String,
    val cameraMovement: String
)

data class OpenCreatorCoverStyle(
    val id: String,
    val name: String,
    val description: String,
    val colorPalette: List<String>,
    val typographyHint: String
)

data class OpenCreatorTtsVoice(
    val id: String,
    val name: String,
    val provider: String,
    val language: String,
    val gender: String,
    val scenario: String,
    val isRecommended: Boolean = false
)

data class OpenCreatorPlatformSource(
    val id: String,
    val name: String,
    val iconName: String,
    val supportedTypes: List<String>,
    val description: String
)

data class OpenCreatorImagePreset(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val promptTemplate: String
)

data class OpenCreatorWritingTemplate(
    val id: String,
    val nameEn: String,
    val nameZh: String,
    val descriptionEn: String,
    val descriptionZh: String,
    val categoryId: String,
    val tags: List<String>,
    val structure: List<String>,
    val instructions: String,
    val author: String,
    val stagePromptTopics: String,
    val stagePromptOutline: String,
    val stagePromptReview: String
)

data class OpenCreatorPipelineSpec(
    val id: String,
    val title: String,
    val targetAspect: String,
    val engine: String,
    val description: String,
    val stages: List<String>,
    val outputFormat: String
)

object OpenCreatorCatalog {
    const val SOURCE_FILE_PATH = "public/assets/aistudio/OpenCreator_All_Source_Code_8b88ab46.txt"
    const val TOTAL_SOURCE_LINES = 621254
    const val TOTAL_SOURCE_BYTES = 32576982L
    const val REPOSITORY_NAME = "OpenCreator-master"
    const val REPOSITORY_DESCRIPTION = "Complete OpenCreator repository consolidating daemon services, video & image presets, TTS voice catalogs, and creator tooling."

    val videoPresets: List<OpenCreatorVideoPreset> = listOf(
        OpenCreatorVideoPreset(
            id = "aerial-pullback-rise-reveal",
            titleEn = "Aerial Pullback and Rise Reveal",
            titleZh = "航拍后拉升高揭示",
            descriptionEn = "Fly backward and upward along the subject axis to reveal the full vehicle or environment in one continuous move.",
            descriptionZh = "让摄像机沿主体轴线同时后退和升高，从近景主体逐步揭示完整载具或大场景。",
            tags = listOf("camera-motion", "aerial", "horizontal", "image-to-video", "reveal"),
            isVertical = false,
            defaultPrompt = "Aerial Pullback and Rise Reveal. Fly backward and upward along the subject axis to reveal the full vehicle or environment in one continuous move.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "anime-skateboard-chase-nyc",
            titleEn = "Anime Skateboard Chase in NYC",
            titleZh = "纽约街头的动漫滑板追逐",
            descriptionEn = "Use a character reference to create a semi-realistic anime skateboard chase with a girl and kitten through New York streets.",
            descriptionZh = "根据角色参考图生成女孩与小猫在纽约街头高速滑板追逐的半写实动漫短片。",
            tags = listOf("animation", "anime-style", "city-street", "skateboard", "horizontal", "image-to-video"),
            isVertical = false,
            defaultPrompt = "Anime Skateboard Chase in NYC. Use a character reference to create a semi-realistic anime skateboard chase with a girl and kitten through New York streets.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "beach-stone-stop-motion",
            titleEn = "Beach Stone Stop-Motion",
            titleZh = "海滩石像定格动画",
            descriptionEn = "Create a vertical stop-motion scene in which a small figure made of beach stones dances, falls, and scatters back into stones.",
            descriptionZh = "生成由天然石块组成的小型人形在潮汐池中起舞、跌倒并重新散落成石块的定格动画。",
            tags = listOf("animation", "stop-motion", "vertical", "text-to-video"),
            isVertical = true,
            defaultPrompt = "Beach Stone Stop-Motion. Create a vertical stop-motion scene in which a small figure made of beach stones dances, falls, and scatters back into stones.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "bedroom-falling-book-catch",
            titleEn = "Bedroom Falling Book Catch",
            titleZh = "卧室接住落书互动",
            descriptionEn = "Animate a warm bedroom interaction in which the subject catches a falling book while preserving the first-frame identity and setting.",
            descriptionZh = "基于卧室人物首帧生成书籍滑落、人物及时接住并作出自然反应的连续物体交互。",
            tags = listOf("character-interaction", "architecture-interior", "vertical", "image-to-video"),
            isVertical = true,
            defaultPrompt = "Bedroom Falling Book Catch. Animate a warm bedroom interaction in which the subject catches a falling book while preserving the first-frame identity and setting.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "bioluminescent-lighthouse-keeper",
            titleEn = "Bioluminescent Lighthouse Keeper",
            titleZh = "生物发光灯塔守望者",
            descriptionEn = "Create a continuous cinematic night shot of a lighthouse keeper watching luminous orbs rise from the sea.",
            descriptionZh = "生成老灯塔守望者在雾夜悬崖目睹发光球体从海中升起的连续电影镜头。",
            tags = listOf("cinematic", "fantasy", "horizontal", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Bioluminescent Lighthouse Keeper. Create a continuous cinematic night shot of a lighthouse keeper watching luminous orbs rise from the sea.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "brutalist-courtyard-martial-arts",
            titleEn = "Brutalist Courtyard Martial Arts",
            titleZh = "粗野主义庭院武术动作",
            descriptionEn = "Create a fast-paced martial-arts confrontation in a brutalist concrete courtyard with tense cinematic lighting.",
            descriptionZh = "生成发生在粗野主义混凝土庭院中的多人武术对抗，强调快节奏摄影和高张力布光。",
            tags = listOf("action", "martial-arts", "architecture-interior", "horizontal", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Brutalist Courtyard Martial Arts. Create a fast-paced martial-arts confrontation in a brutalist concrete courtyard with tense cinematic lighting.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "cinematic-roller-skating-action",
            titleEn = "Cinematic Roller-Skating Action",
            titleZh = "电影级动作轮滑序列",
            descriptionEn = "Create a cinematic urban chase with a roller-skating protagonist, consistent identity, and physically grounded movement.",
            descriptionZh = "生成一名轮滑女性穿梭城市车流并躲避警车追逐的电影级动作序列，强调角色一致性和真实运动物理。",
            tags = listOf("action", "cinematic", "horizontal", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Cinematic Roller-Skating Action. Create a cinematic urban chase with a roller-skating protagonist, consistent identity, and physically grounded movement.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "cinematic-shaolin-action",
            titleEn = "Cinematic Shaolin Action",
            titleZh = "电影级少林武僧动作大片",
            descriptionEn = "Create a wuxia fantasy battle between a Shaolin monk and original stone-armored creatures with choreographed action and speed changes.",
            descriptionZh = "生成少林武僧与石甲生物对决的武侠奇幻动作序列，包含分段动作编排、变速和电光效果。",
            tags = listOf("action", "cinematic", "fantasy", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Cinematic Shaolin Action. Create a wuxia fantasy battle between a Shaolin monk and original stone-armored creatures with choreographed action and speed changes.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "cliffside-roller-skating",
            titleEn = "Cliffside Roller-Skating Action",
            titleZh = "山崖轮滑动作",
            descriptionEn = "Create a photorealistic roller-skating action sequence on a dangerous mountain path with controlled body and wheel physics.",
            descriptionZh = "生成一名女性在危险山崖道路轮滑的超写实动作序列，重点控制环境尺度、身体平衡和轮滑物理。",
            tags = listOf("action", "cinematic", "horizontal", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Cliffside Roller-Skating Action. Create a photorealistic roller-skating action sequence on a dangerous mountain path with controlled body and wheel physics.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "dolly-zoom-space-warp",
            titleEn = "Dolly Zoom Space Warp",
            titleZh = "推拉变焦空间扭曲",
            descriptionEn = "Synchronize a forward dolly with a wider field of view while keeping the subject size fixed and stretching background depth.",
            descriptionZh = "同步执行摄像机前移和镜头变广，在保持主体尺寸不变的同时拉伸背景空间。",
            tags = listOf("camera-motion", "dolly-zoom", "horizontal", "image-to-video"),
            isVertical = false,
            defaultPrompt = "Dolly Zoom Space Warp. Synchronize a forward dolly with a wider field of view while keeping the subject size fixed and stretching background depth.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "drone-orbit-subject",
            titleEn = "Drone Orbit Around Subject",
            titleZh = "无人机环绕主体",
            descriptionEn = "Orbit a subject at constant radius, height, and speed to create continuous horizon rotation and parallax.",
            descriptionZh = "让无人机以固定半径和高度匀速环绕主体，利用地平线旋转和视差建立空间感。",
            tags = listOf("camera-motion", "drone-orbit", "horizontal", "image-to-video"),
            isVertical = false,
            defaultPrompt = "Drone Orbit Around Subject. Orbit a subject at constant radius, height, and speed to create continuous horizon rotation and parallax.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "first-person-dragon-rider",
            titleEn = "First-Person Dragon Rider",
            titleZh = "第一人称视角：龙骑士电影级画面",
            descriptionEn = "Create a vertical first-person fantasy flight as a dragon launches from coastal cliffs, skims the sea, dives underwater, and emerges.",
            descriptionZh = "以第一人称骑手视角生成巨龙从海岸悬崖起飞、贴海飞行、入水并再次冲出水面的奇幻镜头。",
            tags = listOf("cinematic", "fantasy", "vertical", "first-person"),
            isVertical = true,
            defaultPrompt = "First-Person Dragon Rider. Create a vertical first-person fantasy flight as a dragon launches from coastal cliffs, skims the sea, dives underwater, and emerges.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "fixed-point-pan-right-reveal",
            titleEn = "Fixed-Point Pan-Right Reveal",
            titleZh = "固定点向右横摇揭示",
            descriptionEn = "Rotate right from a fixed camera position and settle on the revealed subject without translating the camera.",
            descriptionZh = "让摄像机保持原地不位移，只向右水平旋转并在结尾主体上稳定停留。",
            tags = listOf("camera-motion", "pan-right", "horizontal", "image-to-video", "reveal"),
            isVertical = false,
            defaultPrompt = "Fixed-Point Pan-Right Reveal. Rotate right from a fixed camera position and settle on the revealed subject without translating the camera.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "japanese-influencer-get-ready-ugc",
            titleEn = "Japanese Influencer Get-Ready UGC",
            titleZh = "日本网红出门准备 UGC",
            descriptionEn = "Use character and room references to create a natural vertical phone video about an influencer fixing her bangs before leaving home.",
            descriptionZh = "使用角色图和房间首帧生成一段日本美妆网红出门前整理刘海的自然手机口播视频。",
            tags = listOf("ugc", "vertical", "multi-reference", "image-to-video"),
            isVertical = true,
            defaultPrompt = "Japanese Influencer Get-Ready UGC. Use character and room references to create a natural vertical phone video about an influencer fixing her bangs before leaving home.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "khachapuri-overhead-cooking",
            titleEn = "Overhead Khachapuri Cooking",
            titleZh = "哈恰普里俯拍烹饪",
            descriptionEn = "Generate a 10-second horizontal overhead cooking sequence with continuous food states. Source preview; output and rights unverified.",
            descriptionZh = "生成 10 秒横屏俯拍哈恰普里料理片段，按提示词衔接食物状态。来源预览；效果及授权待核实。",
            tags = listOf("cooking", "horizontal", "text-to-video", "reference-template"),
            isVertical = false,
            defaultPrompt = "Overhead Khachapuri Cooking. Generate a 10-second horizontal overhead cooking sequence with continuous food states. Source preview; output and rights unverified.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "luxury-jewelry-unboxing",
            titleEn = "Luxury Jewelry Unboxing Ad",
            titleZh = "奢华珠宝开箱广告",
            descriptionEn = "Create a vertical luxury jewelry commercial with unboxing, gemstone macro details, styling, and a final product display.",
            descriptionZh = "生成包含礼盒开箱、宝石微距、人物佩戴和产品陈列的竖屏珠宝商业广告。",
            tags = listOf("advertising", "jewelry", "vertical", "text-to-video"),
            isVertical = true,
            defaultPrompt = "Luxury Jewelry Unboxing Ad. Create a vertical luxury jewelry commercial with unboxing, gemstone macro details, styling, and a final product display.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "nyc-parkour-web-swing",
            titleEn = "NYC Parkour and Web-Swing Action",
            titleZh = "纽约跑酷与蛛丝摆荡动作",
            descriptionEn = "Use a character reference for a continuous drone follow shot combining parkour, wall runs, and web-assisted swings through New York.",
            descriptionZh = "生成一段15秒连续无人机跟拍，让角色在纽约街区完成跑酷、蹬墙和蛛丝摆荡。",
            tags = listOf("action", "parkour", "city-street", "horizontal", "image-to-video"),
            isVertical = false,
            defaultPrompt = "NYC Parkour and Web-Swing Action. Use a character reference for a continuous drone follow shot combining parkour, wall runs, and web-assisted swings through New York.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "rack-focus-foreground-background",
            titleEn = "Rack Focus Between Foreground and Background",
            titleZh = "前后景转移焦点",
            descriptionEn = "Lock the composition and shift focus once from a distant anchor to a near subject.",
            descriptionZh = "保持机位和构图完全固定，只在远景锚点与近景主体之间进行一次平滑转焦。",
            tags = listOf("camera-motion", "rack-focus", "horizontal", "image-to-video"),
            isVertical = false,
            defaultPrompt = "Rack Focus Between Foreground and Background. Lock the composition and shift focus once from a distant anchor to a near subject.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "rainforest-mysterious-light",
            titleEn = "Rainforest Mysterious Light",
            titleZh = "雨林神秘光源短片",
            descriptionEn = "Animate a rainy-forest first frame as a woman notices a distant warm light, approaches it, and watches it disappear.",
            descriptionZh = "基于雨林人物首帧生成女性察觉远处暖光、靠近后光芒消失的氛围悬念短片。",
            tags = listOf("cinematic", "mystery", "nature-landscape", "vertical", "image-to-video"),
            isVertical = true,
            defaultPrompt = "Rainforest Mysterious Light. Animate a rainy-forest first frame as a woman notices a distant warm light, approaches it, and watches it disappear.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "real-person-sticker-poster-motion",
            titleEn = "Real-Person Sticker Poster Motion",
            titleZh = "真人贴纸海报动效",
            descriptionEn = "Animate only the real person; freeze the camera and sticker background. 4 seconds if supported. Preview: source tutorial excerpt, not an OpenCreator result.",
            descriptionZh = "上传真人贴纸海报，固定镜头与背景，只让真实人物自然活动。默认 4 秒，具体支持跟随所选服务。预览取自原教程，非本工具生成结果。",
            tags = listOf("animation", "image-to-video", "真人贴纸", "固定镜头", "reference-template"),
            isVertical = false,
            defaultPrompt = "Real-Person Sticker Poster Motion. Animate only the real person; freeze the camera and sticker background. 4 seconds if supported. Preview: source tutorial excerpt, not an OpenCreator result.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "recipe-infographic-cooking-sequence",
            titleEn = "Recipe Infographic to Cooking Sequence (Part One)",
            titleZh = "食谱信息图转烹饪短片（前半段）",
            descriptionEn = "Draft adaptation of the first 10-second stage of a two-stage vertical cooking workflow; continuation is not supported by the current runtime.",
            descriptionZh = "参考多步骤食谱信息图，生成前 10 秒的竖屏烹饪短片；当前模板不支持自动续写后半段。",
            tags = listOf("cooking", "vertical", "reference-image"),
            isVertical = true,
            defaultPrompt = "Recipe Infographic to Cooking Sequence (Part One). Draft adaptation of the first 10-second stage of a two-stage vertical cooking workflow; continuation is not supported by the current runtime.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "retro-illustration-micro-motion",
            titleEn = "Retro Illustration Micro-Motion",
            titleZh = "复古插画微动效",
            descriptionEn = "Animate an uploaded illustration with playful local motion and a fixed camera. The preview comes from the source tutorial, not an OpenCreator generation.",
            descriptionZh = "上传完成的插画或海报，以固定镜头生成轻快的局部微动效，保持主体、文字与布局稳定。预览取自原教程，非本工具生成结果。",
            tags = listOf("animation", "image-to-video", "nostalgic", "手绘插画", "reference-template"),
            isVertical = false,
            defaultPrompt = "Retro Illustration Micro-Motion. Animate an uploaded illustration with playful local motion and a fixed camera. The preview comes from the source tutorial, not an OpenCreator generation.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "sci-fi-motorcycle-action",
            titleEn = "Sci-Fi Motorcycle Action",
            titleZh = "科幻摩托车动作场面",
            descriptionEn = "Create a high-energy cinematic sequence combining a futuristic motorcycle chase, an alien mothership explosion, and close combat.",
            descriptionZh = "生成未来摩托车追逐、外星母舰爆炸和近身格斗组成的高能电影级科幻动作场面。",
            tags = listOf("action", "cinematic", "sci-fi", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Sci-Fi Motorcycle Action. Create a high-energy cinematic sequence combining a futuristic motorcycle chase, an alien mothership explosion, and close combat.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "seoul-sunday-dv-home-video",
            titleEn = "Seoul Sunday DV Home Video",
            titleZh = "首尔周日 DV 家庭录像",
            descriptionEn = "Create an early-2000s-style Seoul home video with natural daily interaction and continuous object state.",
            descriptionZh = "生成一段具有 21 世纪初手持 DV 质感、自然日常互动和连续物体状态的首尔生活记录，适合怀旧 Vlog、城市日常和人物短片。",
            tags = listOf("dv", "nostalgic", "city-street", "horizontal", "text-to-video"),
            isVertical = false,
            defaultPrompt = "Seoul Sunday DV Home Video. Create an early-2000s-style Seoul home video with natural daily interaction and continuous object state.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "sketch-to-real-stop-motion-cooking",
            titleEn = "Sketch-to-Real Stop-Motion Cooking",
            titleZh = "手绘变实物定格料理",
            descriptionEn = "Replace the food and utensil variables to create a 10-second horizontal sketch-to-real sequence. Source preview; output and rights unverified.",
            descriptionZh = "替换提示词中的食材与餐具变量，生成 10 秒横屏手绘转实物料理片段。来源预览；效果及授权待核实。",
            tags = listOf("cooking", "stop-motion", "horizontal", "text-to-video", "reference-template"),
            isVertical = false,
            defaultPrompt = "Sketch-to-Real Stop-Motion Cooking. Replace the food and utensil variables to create a 10-second horizontal sketch-to-real sequence. Source preview; output and rights unverified.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "vertical-crane-reveal",
            titleEn = "Vertical Crane Reveal",
            titleZh = "垂直升降揭示",
            descriptionEn = "Upload a low-angle first frame, then raise the camera smoothly with a continuous downward tilt to reveal the scale of the environment.",
            descriptionZh = "上传低机位起始关键帧，让镜头平滑垂直升高并持续向下俯仰，逐步揭示主体周围的环境规模。",
            tags = listOf("camera-motion", "crane-up", "horizontal", "image-to-video", "reveal"),
            isVertical = false,
            defaultPrompt = "Vertical Crane Reveal. Upload a low-angle first frame, then raise the camera smoothly with a continuous downward tilt to reveal the scale of the environment.",
            cameraMovement = "Camera Motion"
        ),
        OpenCreatorVideoPreset(
            id = "watercolor-sketch-parkour-chase",
            titleEn = "Watercolor Sketch Parkour Chase",
            titleZh = "水彩素描跑酷追逐",
            descriptionEn = "Use multiple character and location references for a parkour chase redrawn with unstable pencil lines and watercolor on every frame.",
            descriptionZh = "使用多张角色与场景参考图生成三人跑酷追逐，并把每一帧呈现为不稳定线条和水彩重新绘制的动画。",
            tags = listOf("animation", "watercolor", "horizontal", "multi-reference"),
            isVertical = false,
            defaultPrompt = "Watercolor Sketch Parkour Chase. Use multiple character and location references for a parkour chase redrawn with unstable pencil lines and watercolor on every frame.",
            cameraMovement = "Default Camera"
        ),
        OpenCreatorVideoPreset(
            id = "wildflower-seasons-macro-timelapse",
            titleEn = "Wildflower Seasons Macro Time-Lapse",
            titleZh = "野花四季微距延时",
            descriptionEn = "Create a 10-second horizontal wildflower time-lapse; text and audio need output verification. Source preview; output and rights unverified.",
            descriptionZh = "生成 10 秒横屏野花四季微距延时片段；指定文字和声音均需效果验收。来源预览；效果及授权待核实。",
            tags = listOf("nature-landscape", "horizontal", "text-to-video", "reference-template"),
            isVertical = false,
            defaultPrompt = "Wildflower Seasons Macro Time-Lapse. Create a 10-second horizontal wildflower time-lapse; text and audio need output verification. Source preview; output and rights unverified.",
            cameraMovement = "Default Camera"
        ),
    )

    val coverStyles: List<OpenCreatorCoverStyle> = listOf(
        OpenCreatorCoverStyle(
            id = "personal-growth",
            name = "Personal Growth Editorial",
            description = "Deep navy, warm gold, and bright white with confident contrast and forward momentum. Bold, optimistic, action-oriented typography.",
            colorPalette = listOf("#0B192C", "#FFD700", "#FFFFFF", "#1E3E62"),
            typographyHint = "Bold sans-serif with key action words highlighted in warm gold."
        ),
        OpenCreatorCoverStyle(
            id = "psychology",
            name = "Psychology & Cognition",
            description = "Restrained deep forest green, blue-gray, white, with coral accent for calm, intelligent contrast. Thoughtful, authoritative structure.",
            colorPalette = listOf("#1A3636", "#40534C", "#D6C0B3", "#E78895"),
            typographyHint = "Clean editorial serif or geometric sans with calm spacing."
        ),
        OpenCreatorCoverStyle(
            id = "wealth-platinum-red",
            name = "Wealth & Finance Platinum",
            description = "Clean white, platinum, charcoal, and strong red accents with polished commercial lighting. Decisive financial styling.",
            colorPalette = listOf("#F5F5F7", "#1D1D1F", "#E50914", "#A1A1A6"),
            typographyHint = "Decisive numbers and financial terms emphasized in vivid red."
        ),
        OpenCreatorCoverStyle(
            id = "bilibili-red-blue-white",
            name = "Bilibili High Energy",
            description = "High-energy Chinese video-platform thumbnail style using red, electric blue, crisp white, and dark outlines for maximum click-through.",
            colorPalette = listOf("#00A1D6", "#FB7299", "#FFFFFF", "#222222"),
            typographyHint = "Dynamic display typography with heavy borders and high impact."
        ),
        OpenCreatorCoverStyle(
            id = "images-go-hard-thumbnail",
            name = "Ultra Punchy Viral",
            description = "Maximum contrast with neon accents, dynamic subject rim lighting, and instant curiosity-gap visual hierarchy.",
            colorPalette = listOf("#000000", "#FF0055", "#00FFA3", "#FFFF00"),
            typographyHint = "Ultra bold angled headline with neon drop shadow."
        )
    )

    val ttsVoices: List<OpenCreatorTtsVoice> = listOf(
        // OpenAI Voices
        OpenCreatorTtsVoice("alloy", "Alloy", "openai", "multi", "neutral", "Balanced & Versatile", isRecommended = true),
        OpenCreatorTtsVoice("ash", "Ash", "openai", "multi", "male", "Calm & Steady"),
        OpenCreatorTtsVoice("ballad", "Ballad", "openai", "multi", "male", "Expressive & Warm"),
        OpenCreatorTtsVoice("cedar", "Cedar", "openai", "multi", "female", "Natural & Engaging", isRecommended = true),
        OpenCreatorTtsVoice("coral", "Coral", "openai", "multi", "female", "Warm & Friendly"),
        OpenCreatorTtsVoice("echo", "Echo", "openai", "multi", "male", "Clear & Crisp"),
        OpenCreatorTtsVoice("fable", "Fable", "openai", "multi", "male", "Narrative Storytelling"),
        OpenCreatorTtsVoice("marin", "Marin", "openai", "multi", "female", "Natural & Dynamic", isRecommended = true),
        OpenCreatorTtsVoice("nova", "Nova", "openai", "multi", "female", "Energetic & Bright"),
        OpenCreatorTtsVoice("onyx", "Onyx", "openai", "multi", "male", "Deep & Authoritative"),
        OpenCreatorTtsVoice("sage", "Sage", "openai", "multi", "female", "Neutral & Thoughtful"),
        OpenCreatorTtsVoice("shimmer", "Shimmer", "openai", "multi", "female", "Soft & Soothing"),
        OpenCreatorTtsVoice("verse", "Verse", "openai", "multi", "male", "Expressive Poetry"),

        // Qwen3 / Aliyun Voices
        OpenCreatorTtsVoice("Cherry", "Qian Yue (芊悦)", "aliyun", "zh-CN", "female", "Sunlit & Positive", isRecommended = true),
        OpenCreatorTtsVoice("Serena", "Su Yao (苏瑶)", "aliyun", "zh-CN", "female", "Gentle Narrative"),
        OpenCreatorTtsVoice("Ethan", "Chen Xu (晨煦)", "aliyun", "zh-CN", "male", "Bright Young Male"),
        OpenCreatorTtsVoice("Chelsie", "Qian Xue (千雪)", "aliyun", "zh-CN", "female", "Anime & ACG"),
        OpenCreatorTtsVoice("Momo", "Mo Tu (茉兔)", "aliyun", "multi", "female", "Playful & Quirky"),
        OpenCreatorTtsVoice("Vivian", "Shi San (十三)", "aliyun", "multi", "female", "Cute & Sassy"),
        OpenCreatorTtsVoice("Moon", "Yue Bai (月白)", "aliyun", "multi", "male", "Dashing & Confident"),
        OpenCreatorTtsVoice("Maia", "Si Yue (四月)", "aliyun", "multi", "female", "Intellectual & Warm"),
        OpenCreatorTtsVoice("Kai", "Kai (凯)", "aliyun", "multi", "male", "Relaxed & Natural"),
        OpenCreatorTtsVoice("Jennifer", "Jennifer", "aliyun", "multi", "female", "Cinematic American Accent", isRecommended = true),
        OpenCreatorTtsVoice("Ryan", "Tian Cha (甜茶)", "aliyun", "multi", "male", "Dramatic Tension"),
        OpenCreatorTtsVoice("Katerina", "Katerina", "aliyun", "multi", "female", "Mature Executive"),
        OpenCreatorTtsVoice("Neil", "A Wen (阿闻)", "aliyun", "multi", "male", "News Broadcaster"),
        OpenCreatorTtsVoice("Elias", "Lecturer Mo (墨讲师)", "aliyun", "multi", "female", "Educational Explainer"),
        OpenCreatorTtsVoice("Vincent", "Uncle Tian (田叔)", "aliyun", "multi", "male", "Gritty & Resonant")
    )

    val platformSources: List<OpenCreatorPlatformSource> = listOf(
        OpenCreatorPlatformSource(
            id = "youtube",
            name = "YouTube & Shorts",
            iconName = "youtube",
            supportedTypes = listOf("Video", "Shorts", "Audio stream", "Auto-captions"),
            description = "Direct extraction of high-res video (up to 4K), audio streams, and timeline markers via yt-dlp runtime."
        ),
        OpenCreatorPlatformSource(
            id = "bilibili",
            name = "Bilibili (哔哩哔哩)",
            iconName = "bilibili",
            supportedTypes = listOf("Multi-part Video", "Danmaku", "Audio", "AI Subtitles"),
            description = "Specialized OpenCreator Bilibili extractor plugin with multi-page P-selector and bilingual dubbing workflow."
        ),
        OpenCreatorPlatformSource(
            id = "douyin",
            name = "Douyin (抖音)",
            iconName = "douyin",
            supportedTypes = listOf("Short Video", "Soundtrack", "Watermark-Free HD"),
            description = "Native Douyin plugin parsing share links and downloading watermark-free source clips for remixing."
        ),
        OpenCreatorPlatformSource(
            id = "tiktok",
            name = "TikTok",
            iconName = "tiktok",
            supportedTypes = listOf("Vertical Video", "Original Audio", "Creator Metadata"),
            description = "Seamless vertical video asset import for repurposing into Shorts and Reels."
        ),
        OpenCreatorPlatformSource(
            id = "wechat",
            name = "WeChat Articles (微信公众号)",
            iconName = "wechat",
            supportedTypes = listOf("Article Text", "Embedded Images", "Video Feeds"),
            description = "Extracts full article copy and visual assets, converting long-form articles into dynamic video scripts."
        ),
        OpenCreatorPlatformSource(
            id = "xiaohongshu",
            name = "Xiaohongshu (小红书)",
            iconName = "xiaohongshu",
            supportedTypes = listOf("Photo Carousel", "Video Note", "Tags & Comments"),
            description = "Captures trending visual cards and lifestyle video notes for content ideation and repackaging."
        )
    )

    val imagePresets: List<OpenCreatorImagePreset> = listOf(
        OpenCreatorImagePreset(
            id = "ecommerce-product",
            title = "E-Commerce Hero Product Ad",
            category = "Commercial",
            description = "Clean studio lighting with soft floating shadows, crisp reflections, and luxury pedestal framing.",
            promptTemplate = "Commercial product photography of {product}, studio spotlight, soft gradient background, 8k resolution, minimalist elegance."
        ),
        OpenCreatorImagePreset(
            id = "vintage-botanical",
            title = "Vintage Scientific Botanical Poster",
            category = "Editorial",
            description = "Intricate 19th-century scientific illustration with aged parchment texture, Latin calligraphy, and exploded floral anatomy.",
            promptTemplate = "Vintage botanical engraving of {subject}, aged parchment background, detailed scientific annotations, muted archival inks."
        ),
        OpenCreatorImagePreset(
            id = "isometric-kitchen",
            title = "Isometric Miniature World",
            category = "3D & Clay",
            description = "Charming diorama cutaway in claymation style with soft ambient occlusion and cozy interior lighting.",
            promptTemplate = "3D isometric miniature diorama of {scene}, cute clay aesthetic, tilt-shift photography, warm morning sunlight, pastel color palette."
        ),
        OpenCreatorImagePreset(
            id = "retro-halftone",
            title = "Retro Halftone Comic Illustration",
            category = "Graphic Art",
            description = "Vintage comic book pop art with visible Ben-Day dots, bold black ink outlines, and 1960s newsprint grain.",
            promptTemplate = "Retro pop-art illustration of {subject}, screen-printed halftone dots, bold ink contours, primary color palette, vintage comic style."
        ),
        OpenCreatorImagePreset(
            id = "watercolor-postcard",
            title = "Watercolor Travel Postcard",
            category = "Artistic",
            description = "Lively hand-painted watercolor architectural sketch with soft wet-on-wet color bleeds and fountain pen line details.",
            promptTemplate = "Watercolor and ink illustration of {landmark}, soft color washes, loose architectural sketch, cold-press paper texture, travel diary."
        ),
        OpenCreatorImagePreset(
            id = "cyberpunk-neon",
            title = "Cyberpunk Neon Streetscape",
            category = "Sci-Fi",
            description = "Atmospheric rain-slicked metropolis bathed in cyan and magenta neon glows, holographic billboards, and cinematic mist.",
            promptTemplate = "Cinematic wide shot of {subject} in futuristic cyberpunk city, rain reflections on pavement, neon magenta and cyan signs, volumetric fog."
        )
    )

    val writingTemplates: List<OpenCreatorWritingTemplate> = listOf(
        OpenCreatorWritingTemplate(
            id = "community.sun-style-writing",
            nameEn = "Sun-Style Narrative Skill",
            nameZh = "孙割写作.skill",
            descriptionEn = "Nonfiction storytelling shaped by short sentences, objects, numbers, and emotional restraint.",
            descriptionZh = "用短句、物件、数字和留白承载情绪的非虚构叙事。",
            categoryId = "story",
            tags = listOf("Plain description", "Short-sentence rhythm", "Object narrative", "Nonfiction"),
            structure = listOf("Contrast or suspense opening", "Scene and time anchor", "Relationship or conflict", "Object callback"),
            instructions = "Move the narrative through observable actions, concrete objects, and real numbers while limiting direct explanations of inner states. Maintain a short-sentence rhythm, give key scenes room to unfold, and create callbacks with details that genuinely exist in the sources.",
            author = "数字生命卡兹克 / OpenCreator adapted",
            stagePromptTopics = "Prioritize finding directions in sources with clear characters, relationships, time spans, concrete objects, or action conflicts. Do not fabricate experiences, numbers, or details not present in the sources.",
            stagePromptOutline = "Establish contrast, suspense, or relationship shifts in the first three sentences. Use time, place, characters, or objects as anchors. End with a callback to an action, object, or question introduced at the beginning.",
            stagePromptReview = "Verify if opening rapidly establishes authentic contrast. Ensure no fabricated inner psychologizing exists. Confirm recurring objects return naturally."
        ),
        OpenCreatorWritingTemplate(
            id = "community.khazix-writer",
            nameEn = "Khazix Conversational Long-form",
            nameZh = "卡兹克公众号长文写作",
            descriptionEn = "Conversational long-form writing built on real experience, HKR inspection, and progressive argument.",
            descriptionZh = "用聊天感、真实体验和层层推进写出有判断力的公众号长文。",
            categoryId = "story",
            tags = listOf("Long-form article", "Conversational rhythm", "Real experience", "Opinion narrative"),
            structure = listOf("Concrete event opening", "Conversational context", "Ideas and examples", "Callback or resonant close"),
            instructions = "Begin with a source-supported event, experience, or genuine question. Develop the argument in a conversational rhythm, using real examples, clear judgments, and natural transitions to sustain reading flow. Never invent personal experiences.",
            author = "数字生命卡兹克",
            stagePromptTopics = "HKR Quality Inspection: Happy (interesting, contrast, suspense), Knowledge (new information, actionable technique), Resonance (connects with genuine situation).",
            stagePromptOutline = "Prototype types: Investigation experiment, product hands-on, phenomenon breakdown, tool share, methodology. Conversational tone, avoid textbook empty cliches.",
            stagePromptReview = "Four-Tier QA: L1 Hard rules (no report cliches), L2 Style consistency (conversational pacing), L3 Content quality (grounded evidence), L4 Authenticity review."
        ),
        OpenCreatorWritingTemplate(
            id = "community.research-paper-writing",
            nameEn = "Research Paper Writing & Breakdown",
            nameZh = "科研论文写作与深度拆解",
            descriptionEn = "Structure research topics with clear sections, rigorous arguments, and reviewer-grade scrutiny.",
            descriptionZh = "用清晰章节、严密论证和审稿人视角组织科研论文与学术内容。",
            categoryId = "analysis",
            tags = listOf("Academic paper", "Section structure", "Claim and evidence", "Pre-submission review"),
            structure = listOf("Research task and problem", "Related work and challenge", "Method and contribution", "Experiments and evidence", "Conclusion & future work"),
            instructions = "Clarify the research story, technical problem, and core contribution before organizing the argument by section. Give each paragraph one message, state its role in opening sentence, and keep terminology consistent.",
            author = "Master-cai / Peng Sida / OpenCreator",
            stagePromptTopics = "Define the research task, technical problem, and target audience clearly. Identify key limitations of existing approaches and cite concrete empirical foundations.",
            stagePromptOutline = "Abstract -> Introduction -> Related Work -> Method -> Experiments -> Conclusion. Maintain strict claim-to-evidence parity.",
            stagePromptReview = "Five Review Dimensions: 1. Contribution, 2. Expression, 3. Experimental Strength, 4. Evaluation Completeness, 5. Method Soundness."
        ),
        OpenCreatorWritingTemplate(
            id = "official.insight",
            nameEn = "Deep Insight & Industry Observation",
            nameZh = "深度洞察",
            descriptionEn = "Clear points of view for industry observations and trend analysis.",
            descriptionZh = "观点鲜明，适合行业观察与趋势分析。",
            categoryId = "analysis",
            tags = listOf("Thesis first", "Industry insight", "Layered argument"),
            structure = listOf("Core judgment", "Context and problem", "Layered argument", "Actionable advice"),
            instructions = "Open with a clearly defined problem and state the conclusion before developing the argument. Use 2-4 well-structured section headings, grounding each point in facts, data, and examples from the sources.",
            author = "OpenCreator Official",
            stagePromptTopics = "Topic must contain verifiable problem, explicit audience, and information gain. Avoid generic corporate buzzwords.",
            stagePromptOutline = "Outline states conclusion first, then argues hierarchically with comparative data or cases.",
            stagePromptReview = "Check if author judgments are clearly differentiated from verified source facts."
        ),
        OpenCreatorWritingTemplate(
            id = "official.story",
            nameEn = "Narrative Storytelling",
            nameZh = "故事叙事",
            descriptionEn = "Lead with a person or scene and build an evocative, rhythmic reading flow.",
            descriptionZh = "从人物或场景切入，强调阅读节奏与情绪推进。",
            categoryId = "story",
            tags = listOf("Scene opening", "Character story", "Emotional rhythm"),
            structure = listOf("Person or scene", "Rising conflict", "Key turn", "Thematic callback"),
            instructions = "Open with a specific person, scene, or conflict, then carry the central idea through the story progression. Keep paragraphs concise and rhythmic, use headings at meaningful turns.",
            author = "OpenCreator Official",
            stagePromptTopics = "Topic initiates from a person with concrete actions or conflicts, not an abstract moral.",
            stagePromptOutline = "Organize by scene progression where each scene alters character stakes or reveals new dimensions.",
            stagePromptReview = "Ensure the ending echoes the opening scene without preachy slogans."
        ),
        OpenCreatorWritingTemplate(
            id = "official.tutorial",
            nameEn = "Practical Tutorial Guide",
            nameZh = "实用教程",
            descriptionEn = "Clear executable steps for workflows, methodologies, and hands-on guides.",
            descriptionZh = "步骤清楚，适合方法论与操作指南。",
            categoryId = "practical",
            tags = listOf("Clear steps", "Method breakdown", "Checklist"),
            structure = listOf("Problem and outcome", "Action steps", "Cautions & Pitfalls", "Completion check"),
            instructions = "Start by explaining the reader problem and the outcome they will achieve, then proceed through executable steps. For each step, state purpose, action, and cautions.",
            author = "OpenCreator Official",
            stagePromptTopics = "Clarify target reader pain point, expected tangible result, and operational scope.",
            stagePromptOutline = "Sequential steps with inputs, outputs, and failure prevention tips.",
            stagePromptReview = "Verify every step can be reproduced and has an explicit verification check."
        ),
        OpenCreatorWritingTemplate(
            id = "official.news-analysis",
            nameEn = "News Analysis & Event Brief",
            nameZh = "热点解读",
            descriptionEn = "Explain what happened quickly, then assess its broader impact and market signals.",
            descriptionZh = "快速交代事件，再解释影响与判断。",
            categoryId = "analysis",
            tags = listOf("Fact summary", "Impact analysis", "Trend outlook"),
            structure = listOf("Event summary", "Crucial context", "Affected stakeholders", "Future signals"),
            instructions = "Open with the minimal summary of what occurred, then answer why it matters, who it affects, and what signals to monitor next. Keep source facts separate from opinions.",
            author = "OpenCreator Official",
            stagePromptTopics = "Pinpoint the event date, primary entity, and immediate reaction.",
            stagePromptOutline = "Facts first (0-15s), context and historical precedent, market implication, upcoming milestone to watch.",
            stagePromptReview = "Ensure no exaggerated claims or speculative numbers exist."
        )
    )

    val pipelineSpecs: List<OpenCreatorPipelineSpec> = listOf(
        OpenCreatorPipelineSpec(
            id = "krillinai-vertical",
            title = "KrillinAI Vertical Video Pipeline (9:16)",
            targetAspect = "9:16 (1080x1920)",
            engine = "Remotion + FunASR + Aliyun/OpenAI TTS",
            description = "Optimized short-form pipeline for TikTok, YouTube Shorts, and WeChat Channels with dynamic safe-zone padding (350px bottom margin) and sentence-level karaoke subtitle animation.",
            stages = listOf(
                "Script parsing & timing breakdown",
                "TTS voice synthesis with pcm inspection",
                "FunASR phonetic subtitle timestamp alignment",
                "Visual scene composition with motion graphics",
                "Audio ducking (dialogue vs background music)",
                "Remotion multi-threaded MP4 render"
            ),
            outputFormat = "H.264 MP4, 1080x1920, 30/60 FPS, AAC 192kbps"
        ),
        OpenCreatorPipelineSpec(
            id = "krillinai-horizontal",
            title = "KrillinAI Horizontal Cinema Pipeline (16:9)",
            targetAspect = "16:9 (1920x1080)",
            engine = "Remotion + Whisper + Multi-Track B-Roll",
            description = "Long-form landscape pipeline with dual-speaker layout, picture-in-picture video layering, chapter markers, and cinematic lower-third titles.",
            stages = listOf(
                "Narrative chapter segmentation",
                "Voice synthesis & multi-track dialogue mastering",
                "B-roll asset synchronization & Ken Burns pan/zoom",
                "Lower-third title rendering & logo watermark",
                "Color grading LUT application",
                "Full HD 1080p final render"
            ),
            outputFormat = "H.264 MP4, 1920x1080, 60 FPS, AAC 320kbps"
        ),
        OpenCreatorPipelineSpec(
            id = "stickman-whiteboard",
            title = "Stickman Remotion Whiteboard Pipeline",
            targetAspect = "Universal (16:9 & 9:16)",
            engine = "Stickman-Remotion Vector Engine",
            description = "Expressive hand-drawn animated stickman explainer pipeline with algorithmic skeletal rigs, emotion faces, and dynamic prop interactions.",
            stages = listOf(
                "Dialogue sentiment mapping",
                "Keyframe pose sequence generation",
                "Vector bone kinematics & expression tweening",
                "Sound effect sync (whoosh, pop, ding)",
                "Canvas rasterization & video export"
            ),
            outputFormat = "H.264 MP4 / WebM with alpha channel"
        ),
        OpenCreatorPipelineSpec(
            id = "funasr-subtitle-engine",
            title = "FunASR Bilingual Subtitle Synchronizer",
            targetAspect = "Overlay Track",
            engine = "FunASR / Whisper AI",
            description = "High-accuracy phonetic speech-to-text aligner generating millisecond-precise SRT/VTT caption tracks with automatic punctuation and filler word elimination.",
            stages = listOf(
                "WAV audio extraction & 16kHz resample",
                "Acoustic model phoneme alignment",
                "VAD (Voice Activity Detection) silence trimming",
                "Sentence boundary segmentation",
                "Word-level timing markup"
            ),
            outputFormat = "SRT, VTT, Remotion Subtitle JSON"
        )
    )
}
