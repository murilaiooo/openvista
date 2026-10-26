package com.openvista.prototype.ambient

import android.content.Context

/** Deployment policy, never writable through preferences or an incoming request. */
object ProviderPolicy {
    // Owner acceptance of free-service terms, attribution and aggregate quotas is outstanding.
    const val WORLDWIDE_WEATHER_APPROVED = false
    const val OSM_MAP_APPROVED = false

    fun forecast(): ForecastProvider = if (WORLDWIDE_WEATHER_APPROVED) RegionalForecastProvider() else NwsForecastProvider()
    fun cities(language: String): CitySearchProvider? =
        if (WORLDWIDE_WEATHER_APPROVED) OpenMeteoCitySearchProvider(language) else null
    fun map(context: Context): MapBackdropProvider? = if (OSM_MAP_APPROVED) OsmMapProvider(context) else null
}
