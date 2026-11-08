package com.example.i18n

object I18n {
    const val LANG_EN = "en"
    const val LANG_KN = "kn"

    fun t(key: String, lang: String): String {
        val isKn = lang == LANG_KN
        return when (key) {
            // App Title & Tagline
            "app_title" -> if (isKn) "ಕ್ರಿಯೇಟರ್ ಸ್ಟುಡಿಯೋ" else "Creator Studio"
            "app_subtitle" -> if (isKn) "ನಿಮ್ಮ ಎಲ್ಲಾ ಸಾಮಾಜಿಕ ಮಾಧ್ಯಮ ವಿಷಯ ರಚನೆ ಒಂದೇ ಕಡೆ" else "Your Content. One Creator Studio."
            "app_desc" -> if (isKn) "YouTube, Instagram ಮತ್ತು Facebook ವಿಷಯಗಳನ್ನು ಸುಲಭವಾಗಿ ರಚಿಸಿ ಮತ್ತು ನಿರ್ವಹಿಸಿ" else "Create, organize and manage YouTube, Instagram and Facebook content from one place."

            // Social Channels
            "my_main_channel" -> if (isKn) "ನನ್ನ ಮುಖ್ಯ ಚಾನೆಲ್" else "MY MAIN CHANNEL"
            "youtube" -> "YouTube"
            "youtube_studio" -> "YouTube Studio"
            "instagram" -> "Instagram"
            "facebook" -> "Facebook"
            "open_youtube" -> if (isKn) "YouTube ತೆರೆಯಿರಿ" else "Open YouTube"
            "open_youtube_studio" -> if (isKn) "Studio ತೆರೆಯಿರಿ" else "Open Studio"
            "open_instagram" -> if (isKn) "Instagram ತೆರೆಯಿರಿ" else "Open Instagram"
            "open_facebook" -> if (isKn) "Facebook ತೆರೆಯಿರಿ" else "Open Facebook"
            "open_channel" -> if (isKn) "ಚಾನೆಲ್ ತೆರೆಯಿರಿ" else "Open Channel"

            // Navigation Items
            "nav_dashboard" -> if (isKn) "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್" else "Dashboard"
            "nav_cross_platform_hub" -> if (isKn) "ಕ್ರಾಸ್-ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಕೇಂದ್ರ" else "Cross-Platform Hub"
            "nav_ai_prompt_studio" -> if (isKn) "AI ಪ್ರಾಂಪ್ಟ್ ಸ್ಟುಡಿಯೋ" else "AI Prompt Studio"
            "nav_story_to_video" -> if (isKn) "ಕಥೆ-ಟು-ವೀಡಿಯೊ AI" else "Story-to-Video AI"
            "nav_cartoon_studio" -> if (isKn) "ಕಾರ್ಟೂನ್ ಸ್ಟುಡಿಯೋ" else "Cartoon Studio"
            "nav_auto_generator" -> if (isKn) "⚡ 10-ನಿಮಿಷದ ಆಟೋ ವೀಡಿಯೊ" else "⚡ 10-Min Auto Video"
            "nav_create_video" -> if (isKn) "ವೀಡಿಯೊ ರಚಿಸಿ" else "Create Video"
            "nav_shorts_reels" -> if (isKn) "ಶಾರ್ಟ್ಸ್ / ರೀಲ್ಸ್" else "Shorts / Reels"
            "nav_script_gen" -> if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಜನರೇಟರ್" else "Script Generator"
            "nav_thumbnail_creator" -> if (isKn) "ಥಂಬ್‌ನೇಲ್ ಮೇಕರ್" else "Thumbnail Creator"
            "nav_publishing_centre" -> if (isKn) "ಪ್ರಕಾಶನ ಕೇಂದ್ರ" else "Publishing Centre"
            "nav_youtube" -> if (isKn) "YouTube ಟೂಲ್" else "YouTube Tool"
            "nav_instagram" -> if (isKn) "Instagram ಟೂಲ್" else "Instagram Tool"
            "nav_facebook" -> if (isKn) "Facebook ಟೂಲ್" else "Facebook Tool"
            "nav_projects" -> if (isKn) "ಕಂಟೆಂಟ್ ಪ್ರಾಜೆಕ್ಟ್‌ಗಳು" else "Content Projects"
            "nav_calendar" -> if (isKn) "ಕಂಟೆಂಟ್ ಕ್ಯಾಲೆಂಡರ್" else "Content Calendar"
            "nav_media" -> if (isKn) "ಮೀಡಿಯಾ ಲೈಬ್ರರಿ" else "Media Library"
            "nav_analytics" -> if (isKn) "ಅನಾಲಿಟಿಕ್ಸ್" else "Analytics"
            "nav_ai_tools" -> if (isKn) "AI ಪರಿಕರಗಳು" else "AI Tools"
            "nav_social_channels" -> if (isKn) "ಸಾಮಾಜಿಕ ಚಾನೆಲ್‌ಗಳು" else "Social Channels"
            "nav_backup_restore" -> if (isKn) "ಬ್ಯಾಕಪ್ / ಮರುಸ್ಥಾಪನೆ" else "Backup / Restore"
            "nav_video_editor" -> if (isKn) "ವೀಡಿಯೊ ಸಂಪಾದಕ & AI" else "Video Editor & AI"
            "nav_opencreator_hub" -> if (isKn) "ಓಪನ್‌ಕ್ರಿಯೇಟರ್ ಹಬ್" else "OpenCreator Hub & Presets"
            "nav_github_link" -> if (isKn) "ಗಿಟ್‌ಹಬ್ ಡೇಟಾ ಲಿಂಕ್" else "GitHub Data Link"
            "nav_manga_studio" -> if (isKn) "ಮಂಗಾ ಸ್ಟೋರಿ ಸ್ಟುಡಿಯೋ" else "Manga Story Studio"
            "nav_api_settings" -> if (isKn) "API ಕೀ ಸೆಟ್ಟಿಂಗ್ಸ್" else "API Settings"
            "nav_settings" -> if (isKn) "ಸೆಟ್ಟಿಂಗ್ಸ್" else "Settings"

            // Quick Actions Bar
            "quick_actions" -> if (isKn) "ತ್ವರಿತ ಪರಿಕರಗಳು" else "Quick Actions"
            "quick_script" -> if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಜನರೇಟರ್" else "Script Generator"
            "quick_caption" -> if (isKn) "ಕ್ಯಾಪ್ಷನ್ ಜನರೇಟರ್" else "Caption Generator"
            "quick_hashtags" -> if (isKn) "ಹ್ಯಾಶ್‌ಟ್ಯಾಗ್ಸ್" else "Hashtag Generator"

            // Dashboard Cards
            "card_create_video" -> if (isKn) "🎬 ವೀಡಿಯೊ ರಚಿಸಿ" else "🎬 Create Video"
            "card_shorts_reels" -> if (isKn) "📱 ಶಾರ್ಟ್ಸ್ / ರೀಲ್ಸ್" else "📱 Create Short / Reel"
            "card_thumbnail" -> if (isKn) "🖼 ಥಂಬ್‌ನೇಲ್ ಮೇಕರ್" else "🖼 Create Thumbnail"
            "card_script" -> if (isKn) "✍ ಸ್ಕ್ರಿಪ್ಟ್ ಬರೆಯಿರಿ" else "✍ Generate Script"
            "card_caption" -> if (isKn) "📝 ಕ್ಯಾಪ್ಷನ್ ಪಡೆಯಿರಿ" else "📝 Generate Caption"
            "card_hashtags" -> if (isKn) "#️⃣ ಹ್ಯಾಶ್‌ಟ್ಯಾಗ್‌ಗಳು" else "#️⃣ Generate Hashtags"
            "card_calendar" -> if (isKn) "📅 ಕಂಟೆಂಟ್ ಕ್ಯಾಲೆಂಡರ್" else "📅 Content Calendar"
            "card_media" -> if (isKn) "📂 ಮೀಡಿಯಾ ಲೈಬ್ರರಿ" else "📂 Media Library"
            "card_analytics" -> if (isKn) "📊 ಅನಾಲಿಟಿಕ್ಸ್" else "📊 Analytics"
            "card_channels" -> if (isKn) "🔗 ಸಾಮಾಜಿಕ ಚಾನೆಲ್‌ಗಳು" else "🔗 Social Channels"
            "card_publish" -> if (isKn) "📤 ಪ್ರಕಟಿಸಿ / ರಫ್ತು" else "📤 Publish / Export"
            "card_settings" -> if (isKn) "⚙ ಸೆಟ್ಟಿಂಗ್ಸ್" else "⚙ Settings"

            // Calendar & Statuses
            "status_draft" -> if (isKn) "ಕರಡು (Draft)" else "Draft"
            "status_scheduled" -> if (isKn) "ನಿಗದಿಪಡಿಸಲಾಗಿದೆ (Scheduled)" else "Scheduled"
            "status_published" -> if (isKn) "ಪ್ರಕಟಿಸಲಾಗಿದೆ (Published)" else "Published"
            "status_idea" -> if (isKn) "ಕಲ್ಪನೆ (Idea)" else "Idea"
            "status_script_ready" -> if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಸಿದ್ಧ" else "Script Ready"
            "status_production" -> if (isKn) "ನಿರ್ಮಾಣದಲ್ಲಿ" else "In Production"
            "status_thumbnail_ready" -> if (isKn) "ಥಂಬ್‌ನೇಲ್ ಸಿದ್ಧ" else "Thumbnail Ready"
            "status_ready_to_publish" -> if (isKn) "ಪ್ರಕಟಿಸಲು ಸಿದ್ಧ" else "Ready to Publish"
            
            "search_calendar" -> if (isKn) "ಶೀರ್ಷಿಕೆ ಅಥವಾ ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಹುಡುಕಿ..." else "Search by title or platform tags..."
            "export_csv" -> if (isKn) "CSV ಗೆ ರಫ್ತು ಮಾಡಿ" else "Export to CSV"
            "add_calendar_task" -> if (isKn) "+ ಹೊಸ ಕಾರ್ಯ ನಿಗದಿಪಡಿಸಿ" else "+ Schedule Task"
            "task_status_changed" -> if (isKn) "ಕಾರ್ಯದ ಸ್ಥಿತಿ ಬದಲಾಗಿದೆ: " else "Task status updated to: "
            "drag_instruction" -> if (isKn) "ದಿನಾಂಕ ಬದಲಾಯಿಸಲು ಟ್ಯಾಪ್ ಮಾಡಿ ಅಥವಾ ಮರುಹೊಂದಿಸಿ" else "Tap status to advance or reschedule date"

            // Common Actions
            "copy" -> if (isKn) "ನಕಲಿಸಿ" else "Copy"
            "copied" -> if (isKn) "ನಕಲಿಸಲಾಗಿದೆ!" else "Copied!"
            "save" -> if (isKn) "ಉಳಿಸಿ" else "Save"
            "saved" -> if (isKn) "ಉಳಿಸಲಾಗಿದೆ!" else "Saved!"
            "delete" -> if (isKn) "ಅಳಿಸಿ" else "Delete"
            "edit" -> if (isKn) "ಸಂಪಾದಿಸಿ" else "Edit"
            "generate" -> if (isKn) "ರಚಿಸಿ" else "Generate"
            "regenerate" -> if (isKn) "ಮತ್ತೆ ರಚಿಸಿ" else "Regenerate"
            "download" -> if (isKn) "ಡೌನ್‌ಲೋಡ್" else "Download"
            "cancel" -> if (isKn) "ರದ್ದುಮಾಡಿ" else "Cancel"
            "close" -> if (isKn) "ಮುಚ್ಚಿ" else "Close"
            "search" -> if (isKn) "ಹುಡುಕಿ" else "Search"
            "filter" -> if (isKn) "ಫಿಲ್ಟರ್" else "Filter"
            "clear" -> if (isKn) "ತೆರವುಗೊಳಿಸಿ" else "Clear"

            // Setup Wizard
            "wizard_title" -> if (isKn) "ಕ್ರಿಯೇಟರ್ ಸ್ಟುಡಿಯೋಗೆ ಸ್ವಾಗತ!" else "Welcome to Creator Studio!"
            "wizard_subtitle" -> if (isKn) "ನಿಮ್ಮ ಸಾಮಾಜಿಕ ಮಾಧ್ಯಮ ಪ್ರೊಫೈಲ್ ಲಿಂಕ್‌ಗಳನ್ನು ಸೇರಿಸಿ" else "Connect your creator social links to get started"
            "wizard_finish" -> if (isKn) "ಸೆಟಪ್ ಪೂರ್ಣಗೊಳಿಸಿ" else "Finish Setup"
            "wizard_skip" -> if (isKn) "ನಂತರ ಹೊಂದಿಸಿ (Skip)" else "Skip for Now"

            else -> key
        }
    }
}
