package com.openvista.prototype.ambient

import android.content.Context

/** Deployment policy, never writable through preferences or an incoming request. */
object ProviderPolicy {
    // Approved for the free, non-commercial, ad-free OpenVista build. Respect
    // CC BY 4.0 attribution and API limits; paid/commercial distributions need review.
    const val WORLDWIDE_WEATHER_APPROVED = true
    const val OSM_MAP_APPROVED = false

    fun forecast(): ForecastProvider = if (WORLDWIDE_WEATHER_APPROVED) RegionalForecastProvider() else NwsForecastProvider()
    fun cities(language: String): CitySearchProvider? =
        if (WORLDWIDE_WEATHER_APPROVED) OpenMeteoCitySearchProvider(language) else null
    fun map(context: Context): MapBackdropProvider? = if (OSM_MAP_APPROVED) OsmMapProvider(context) else null
}
