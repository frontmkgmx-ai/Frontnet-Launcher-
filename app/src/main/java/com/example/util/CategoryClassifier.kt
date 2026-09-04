package com.example.util

import android.content.pm.ApplicationInfo
import android.os.Build
import com.example.model.AppCategory

object CategoryClassifier {

    fun classify(packageName: String, label: String, appInfo: ApplicationInfo? = null): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        // 1. Check system category if available (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
                ApplicationInfo.CATEGORY_MAPS -> return AppCategory.TRAVEL
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
            }
        }

        // 2. Heuristic package and label matching
        return when {
            // Communication & Social
            matchesAny(lowerPkg, lowerLabel,
                "whatsapp", "telegram", "instagram", "facebook", "twitter", "x.corp", "messenger",
                "discord", "signal", "snapchat", "wechat", "viber", "teams", "slack", "contatos",
                "contacts", "telefone", "phone", "dialer", "mensagens", "messages", "sms", "chat"
            ) -> AppCategory.COMMUNICATION

            // Media & Streaming
            matchesAny(lowerPkg, lowerLabel,
                "youtube", "spotify", "netflix", "prime", "twitch", "tiktok", "deezer",
                "musica", "music", "video", "player", "fotos", "photos", "galeria", "gallery",
                "cinema", "streaming", "radio", "podcast", "vlc", "sound"
            ) -> AppCategory.MEDIA

            // Productivity & Work
            matchesAny(lowerPkg, lowerLabel,
                "gmail", "email", "outlook", "mail", "calendar", "calendario", "agenda",
                "drive", "docs", "sheets", "slides", "notion", "trello", "keep", "notes",
                "notas", "todo", "task", "pdf", "word", "excel", "office", "workspace"
            ) -> AppCategory.PRODUCTIVITY

            // Finance & Shopping
            matchesAny(lowerPkg, lowerLabel,
                "bank", "banco", "nubank", "inter", "itau", "santander", "bradesco", "caixa",
                "wallet", "pagbank", "mercadopago", "picpay", "paypal", "crypto", "binance",
                "invest", "finance", "cartao", "amazon", "shopee", "mercadolivre", "magalu",
                "aliexpress", "shein", "compras"
            ) -> AppCategory.FINANCE

            // Travel & Navigation
            matchesAny(lowerPkg, lowerLabel,
                "maps", "mapa", "waze", "uber", "99", "cabify", "transport", "viagem",
                "travel", "booking", "airbnb", "flight", "voo", "onibus", "metro", "transit"
            ) -> AppCategory.TRAVEL

            // Games
            matchesAny(lowerPkg, lowerLabel,
                "game", "jogos", "jogo", "play.games", "roblox", "minecraft", "freefire",
                "pubg", "candy", "clash", "subway", "fifa", "arcade", "puzzle", "rpg"
            ) -> AppCategory.GAMES

            // Tools & System
            matchesAny(lowerPkg, lowerLabel,
                "settings", "config", "camera", "câmera", "relogio", "clock", "alarme",
                "calculator", "calculadora", "arquivos", "files", "explorer", "store",
                "vending", "google.android.gms", "security", "seguranca", "cleaner",
                "browser", "chrome", "firefox", "edge", "navegador", "gravador", "recorder"
            ) -> AppCategory.TOOLS

            else -> AppCategory.OTHER
        }
    }

    private fun matchesAny(pkg: String, label: String, vararg keywords: String): Boolean {
        return keywords.any { kw -> pkg.contains(kw) || label.contains(kw) }
    }
}
