package com.openvista.prototype.ui

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** A small bootstrap preference avoids opening the library twice during Activity attachment. */
object AppLocale {
    fun wrap(context: Context): Context {
        val language = language(context)
        if (language == "system") return context
        val locale = if (language == "pt-BR") Locale.forLanguageTag("pt-BR") else Locale.ENGLISH
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(configuration)
    }

    fun language(context: Context): String = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        .getString("language", "system")?.takeIf { it in setOf("system", "en", "pt-BR") } ?: "system"

    fun save(context: Context, language: String) {
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit()
            .putString("language", language).apply()
    }
}
