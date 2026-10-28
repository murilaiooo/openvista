package com.openvista.prototype.ui

import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Builds the UI context: the saved language plus a density that maps every display onto the
 * same 960 x 540 dp canvas. A small bootstrap preference avoids opening the library twice
 * during Activity attachment.
 */
object AppLocale {
    const val CANVAS_WIDTH_DP = 960
    const val CANVAS_HEIGHT_DP = 540

    fun wrap(context: Context): Context {
        TvDesign.install(context)
        val language = language(context)
        val configuration = Configuration(context.resources.configuration)
        if (language != "system") {
            configuration.setLocale(if (language == "pt-BR") Locale.forLanguageTag("pt-BR") else Locale.ENGLISH)
        }
        val metrics = context.resources.displayMetrics
        val density = canvasDensity(metrics.widthPixels, metrics.heightPixels)
        val rescale = density != null && density != configuration.densityDpi
        if (rescale && density != null) {
            val scale = density / 160f
            configuration.densityDpi = density
            configuration.screenWidthDp = (metrics.widthPixels / scale).roundToInt()
            configuration.screenHeightDp = (metrics.heightPixels / scale).roundToInt()
            configuration.smallestScreenWidthDp = min(configuration.screenWidthDp, configuration.screenHeightDp)
        }
        if (language == "system" && !rescale) return context
        return context.createConfigurationContext(configuration)
    }

    /**
     * A 3840 x 2160 panel with a raised system density otherwise shows every layout smaller
     * than a 1080p TV does; the canvas keeps proportions identical across both.
     */
    internal fun canvasDensity(widthPixels: Int, heightPixels: Int): Int? {
        val long = max(widthPixels, heightPixels)
        val short = min(widthPixels, heightPixels)
        if (long <= 0 || short <= 0) return null
        return (min(long.toFloat() / CANVAS_WIDTH_DP, short.toFloat() / CANVAS_HEIGHT_DP) * 160f)
            .roundToInt().coerceAtLeast(120)
    }

    fun language(context: Context): String = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        .getString("language", "system")?.takeIf { it in setOf("system", "en", "pt-BR") } ?: "system"

    fun save(context: Context, language: String) {
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit()
            .putString("language", language).apply()
    }
}
