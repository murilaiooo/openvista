package com.openvista.prototype.ambient

import android.content.Context
import com.openvista.prototype.R
import java.util.Locale

/** Translate only known provider vocabulary; preserve unfamiliar wording rather than inventing conditions. */
object WeatherText {
    fun description(context: Context, current: CurrentWeather): String {
        val label = when (current.weatherCode) {
            0 -> R.string.condition_clear
            1 -> R.string.condition_mainly_clear
            2 -> R.string.condition_partly_cloudy
            3 -> R.string.condition_overcast
            45 -> R.string.condition_fog
            48 -> R.string.condition_rime_fog
            51 -> R.string.condition_light_drizzle
            53 -> R.string.condition_drizzle
            55 -> R.string.condition_heavy_drizzle
            56, 57 -> R.string.condition_freezing_drizzle
            61 -> R.string.condition_light_rain
            63 -> R.string.condition_rain
            65 -> R.string.condition_heavy_rain
            66, 67 -> R.string.condition_freezing_rain
            71 -> R.string.condition_light_snow
            73 -> R.string.condition_snow
            75 -> R.string.condition_heavy_snow
            77 -> R.string.condition_snow_grains
            80 -> R.string.condition_light_showers
            81 -> R.string.condition_showers
            82 -> R.string.condition_heavy_showers
            85, 86 -> R.string.condition_snow_showers
            95 -> R.string.condition_thunderstorms
            96, 99 -> R.string.condition_thunderstorms_hail
            else -> null
        }
        if (label != null) return context.getString(label)
        val raw = current.description?.trim().orEmpty()
        if (context.resources.configuration.locales[0].language != "pt") return raw
        fun phrase(value: String): String? {
            val normalized = value.trim().lowercase(Locale.US)
            nws[normalized]?.let { return it }
            val prefixes = listOf("slight chance " to "Pequena chance de ", "chance " to "Chance de ",
                "isolated " to "Isoladas: ", "scattered " to "Esparsas: ", "patchy " to "Áreas de ",
                "areas of " to "Áreas de ", "widespread " to "Generalizado: ")
            prefixes.firstOrNull { normalized.startsWith(it.first) }?.let { (prefix, translated) ->
                phrase(normalized.removePrefix(prefix))?.let { return translated + it.lowercase(Locale.forLanguageTag("pt-BR")) }
            }
            if (normalized.endsWith(" likely")) phrase(normalized.removeSuffix(" likely"))?.let { return "$it provável" }
            return null
        }
        phrase(raw)?.let { return it }
        val parts = Regex("\\s+(then|and|with)\\s+", RegexOption.IGNORE_CASE).split(raw)
        val joins = Regex("\\s+(then|and|with)\\s+", RegexOption.IGNORE_CASE).findAll(raw).map { it.groupValues[1].lowercase(Locale.US) }.toList()
        val translated = parts.map { phrase(it) }
        if (translated.any { it == null } || parts.size != joins.size + 1) return raw
        return buildString {
            translated.forEachIndexed { index, text ->
                if (index > 0) append(when (joins[index - 1]) { "then" -> ", depois "; "with" -> " com "; else -> " e " })
                append(text)
            }
        }
    }

    private val nws = mapOf(
        "sunny" to "Ensolarado", "clear" to "Céu limpo", "fair" to "Tempo bom",
        "mostly sunny" to "Predomínio de sol", "mostly clear" to "Predomínio de céu limpo",
        "partly sunny" to "Parcialmente ensolarado", "partly cloudy" to "Parcialmente nublado",
        "mostly cloudy" to "Predomínio de nuvens", "cloudy" to "Nublado", "overcast" to "Encoberto",
        "rain" to "Chuva", "light rain" to "Chuva fraca", "heavy rain" to "Chuva forte",
        "rain showers" to "Pancadas de chuva", "showers" to "Pancadas de chuva",
        "thunderstorms" to "Trovoadas", "showers and thunderstorms" to "Pancadas de chuva e trovoadas",
        "snow" to "Neve", "light snow" to "Neve fraca", "heavy snow" to "Neve forte",
        "snow showers" to "Pancadas de neve", "rain and snow" to "Chuva e neve",
        "freezing rain" to "Chuva congelante", "freezing drizzle" to "Garoa congelante",
        "drizzle" to "Garoa", "fog" to "Nevoeiro", "freezing fog" to "Nevoeiro congelante",
        "haze" to "Névoa seca", "smoke" to "Fumaça", "blowing dust" to "Poeira levantada pelo vento",
        "windy" to "Ventos fortes", "breezy" to "Ventando", "blizzard" to "Nevasca",
        "sleet" to "Chuva congelada", "ice pellets" to "Grãos de gelo", "frost" to "Geada",
    )
}
