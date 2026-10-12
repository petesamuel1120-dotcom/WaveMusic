package com.petedev.wavemusic.viewModel

internal fun shouldOfferLyricsTranslation(text: String, configuredLanguage: String): Boolean {
    if (text.isBlank()) return true
    val language = when (configuredLanguage.trim().lowercase().substringBefore('-')) {
        "english" -> "en"
        "german", "deutsch" -> "de"
        "arabic" -> "ar"
        "spanish", "espanol", "español" -> "es"
        "french", "français" -> "fr"
        "italian", "italiano" -> "it"
        "portuguese", "português" -> "pt"
        "dutch", "nederlands" -> "nl"
        "russian", "русский" -> "ru"
        "ukrainian", "українська" -> "uk"
        "japanese", "日本語" -> "ja"
        "korean", "한국어" -> "ko"
        "chinese", "中文" -> "zh"
        "hindi", "हिन्दी" -> "hi"
        "thai", "ไทย" -> "th"
        "polish", "polski" -> "pl"
        "turkish", "türkçe" -> "tr"
        "swedish", "svenska" -> "sv"
        "danish", "dansk" -> "da"
        "norwegian", "norsk" -> "no"
        else -> configuredLanguage.trim().lowercase().substringBefore('-').take(2)
    }
    val scriptRanges = when (language) {
        "ar", "fa", "ur" -> listOf(0x0600..0x08FF)
        "he", "yi" -> listOf(0x0590..0x05FF)
        "ru", "uk", "bg", "sr" -> listOf(0x0400..0x052F)
        "el" -> listOf(0x0370..0x03FF)
        "ja" -> listOf(0x3040..0x30FF, 0x4E00..0x9FFF)
        "zh" -> listOf(0x3400..0x9FFF)
        "ko" -> listOf(0xAC00..0xD7AF)
        "hi", "mr", "ne" -> listOf(0x0900..0x097F)
        "th" -> listOf(0x0E00..0x0E7F)
        "bn" -> listOf(0x0980..0x09FF)
        "ta" -> listOf(0x0B80..0x0BFF)
        else -> emptyList()
    }
    if (scriptRanges.isNotEmpty()) {
        val letters = text.count { it.isLetter() }
        val targetLetters = text.count { character -> scriptRanges.any { character.code in it } }
        if (targetLetters >= 4 && letters > 0) {
            return targetLetters.toFloat() / letters <= 0.65f
        }
    }

    val dictionaries = mapOf(
        "en" to setOf("i", "you", "the", "a", "an", "and", "or", "of", "to", "in", "is", "it", "me", "my", "we", "he", "she", "they", "this", "that", "for", "with", "was", "are", "be", "have", "do", "not", "don't", "can't", "will", "can", "what", "when", "where", "how", "but", "as", "your", "our", "on", "at", "from", "if", "so"),
        "de" to setOf("ich", "du", "er", "sie", "wir", "ihr", "der", "die", "das", "den", "dem", "ein", "eine", "einer", "und", "oder", "nicht", "ist", "bist", "bin", "sind", "war", "für", "mit", "mein", "meine", "mich", "mir", "dich", "dir", "was", "wie", "wo", "wenn", "dass", "auf", "an", "im", "in", "es", "so", "doch", "bei", "von", "zu"),
        "es" to setOf("yo", "tú", "él", "ella", "nosotros", "el", "la", "los", "las", "un", "una", "y", "o", "de", "del", "que", "no", "es", "soy", "eres", "está", "mi", "me", "te", "con", "por", "para", "como", "en", "si", "pero", "tu", "se"),
        "fr" to setOf("je", "tu", "il", "elle", "nous", "vous", "ils", "le", "la", "les", "un", "une", "et", "ou", "de", "du", "que", "ne", "pas", "est", "suis", "sont", "mon", "ma", "moi", "toi", "avec", "pour", "dans", "sur", "si", "mais", "ce", "ça"),
        "it" to setOf("io", "tu", "lui", "lei", "noi", "voi", "il", "lo", "la", "gli", "un", "una", "e", "o", "di", "che", "non", "è", "sono", "sei", "mio", "mia", "con", "per", "in", "su", "ma", "se", "come"),
        "pt" to setOf("eu", "tu", "ele", "ela", "nós", "vocês", "o", "a", "os", "as", "um", "uma", "e", "ou", "de", "que", "não", "é", "sou", "está", "meu", "minha", "com", "para", "por", "em", "mas", "se", "como"),
        "nl" to setOf("ik", "jij", "je", "hij", "zij", "wij", "we", "de", "het", "een", "en", "of", "van", "dat", "niet", "is", "ben", "zijn", "mijn", "met", "voor", "in", "op", "maar", "als", "hoe", "wat"),
        "pl" to setOf("ja", "ty", "on", "ona", "my", "wy", "i", "a", "ale", "że", "nie", "jest", "są", "to", "co", "z", "do", "w", "na", "się", "mój", "moja", "dla", "jak"),
        "tr" to setOf("ben", "sen", "o", "biz", "siz", "ve", "veya", "de", "da", "bir", "bu", "şu", "ne", "değil", "var", "yok", "için", "ile", "çok", "gibi", "ama"),
        "sv" to setOf("jag", "du", "han", "hon", "vi", "ni", "de", "det", "den", "en", "ett", "och", "eller", "av", "att", "inte", "är", "var", "min", "mitt", "med", "för", "på", "som", "men"),
        "da" to setOf("jeg", "du", "han", "hun", "vi", "I", "de", "det", "den", "en", "et", "og", "eller", "af", "at", "ikke", "er", "var", "min", "mit", "med", "for", "på", "som", "men"),
        "no" to setOf("jeg", "du", "han", "hun", "vi", "dere", "de", "det", "den", "en", "et", "og", "eller", "av", "at", "ikke", "er", "var", "min", "mitt", "med", "for", "på", "som", "men"),
    )
    val targetWords = dictionaries[language] ?: return true
    val words = buildList {
        val word = StringBuilder()
        for (character in text.lowercase()) {
            if (character.isLetter()) {
                word.append(character)
            } else if (word.isNotEmpty()) {
                add(word.toString())
                word.clear()
            }
        }
        if (word.isNotEmpty()) add(word.toString())
    }
    val otherWords = dictionaries.filterKeys { it != language }.values.flatten().toSet()
    val targetMarkers = words.count { it in targetWords }
    val foreignMarkers = words.count { it !in targetWords && it in otherWords }
    val identifiedMarkers = targetMarkers + foreignMarkers
    // Once there are enough target-language markers to identify the dominant language, use
    // their share even when there are few foreign markers. Otherwise short, clearly English
    // lyrics were incorrectly treated as uncertain and still offered translation.
    if (targetMarkers < 4 || identifiedMarkers == 0) return true
    return targetMarkers.toFloat() / identifiedMarkers <= 0.65f
}

