"""Offline NOAA-style solar reference, not bundled in any Android application.

This small Python prototype tests behavior before porting it to Kotlin.
Altitude and events are approximations; terrain and weather are not modeled.
"""
from __future__ import annotations

from datetime import date, datetime, timedelta, timezone
import math


SUNRISE_ELEVATION_DEG = -0.833
GOLDEN_HOUR_MAX_ELEVATION_DEG = 6.0


def _check_coordinates(latitude: float, longitude: float) -> None:
    if not (
        math.isfinite(latitude)
        and math.isfinite(longitude)
        and -90 <= latitude <= 90
        and -180 <= longitude <= 180
    ):
        raise ValueError("Latitude/longitude must be finite valid coordinates")


def solar_elevation_utc(instant: datetime, latitude: float, longitude: float) -> float:
    """Approximate geometric solar altitude, in degrees, at a UTC instant."""
    _check_coordinates(latitude, longitude)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Solar calculation requires a timezone-aware instant")

    utc = instant.astimezone(timezone.utc)
    days_in_year = 366 if (
        utc.year % 4 == 0 and (utc.year % 100 != 0 or utc.year % 400 == 0)
    ) else 365
    minutes = utc.hour * 60 + utc.minute + utc.second / 60 + utc.microsecond / 60e6
    fractional_year = 2 * math.pi / days_in_year * (
        utc.timetuple().tm_yday - 1 + (minutes / 60 - 12) / 24
    )

    gamma = fractional_year
    equation_of_time = 229.18 * (
        0.000075 + 0.001868 * math.cos(gamma)
        - 0.032077 * math.sin(gamma)
        - 0.014615 * math.cos(2 * gamma)
        - 0.040849 * math.sin(2 * gamma)
    )
    declination = (
        0.006918 - 0.399912 * math.cos(gamma)
        + 0.070257 * math.sin(gamma)
        - 0.006758 * math.cos(2 * gamma)
        + 0.000907 * math.sin(2 * gamma)
        - 0.002697 * math.cos(3 * gamma)
        + 0.00148 * math.sin(3 * gamma)
    )

    solar_minutes = (minutes + equation_of_time + 4 * longitude) % 1440
    hour_angle = math.radians(solar_minutes / 4 - 180)
    latitude_rad = math.radians(latitude)
    sin_elevation = (
        math.sin(latitude_rad) * math.sin(declination)
        + math.cos(latitude_rad) * math.cos(declination) * math.cos(hour_angle)
    )
    return math.degrees(math.asin(max(-1.0, min(1.0, sin_elevation))))


def daylight_state(
    instant: datetime, latitude: float | None, longitude: float | None
) -> dict[str, float | str | bool | None]:
    """Classify user-geotagged sources; do not invent coordinates."""
    if latitude is None or longitude is None:
        return {"state": "unknown_location", "altitudeDegrees": None, "goldenHour": False}
    altitude = solar_elevation_utc(instant, latitude, longitude)
    return {
        "state": "daylight" if altitude >= SUNRISE_ELEVATION_DEG else "night",
        "altitudeDegrees": round(altitude, 2),
        "goldenHour": 0 <= altitude <= GOLDEN_HOUR_MAX_ELEVATION_DEG,
    }


def sun_events_utc(day: date, latitude: float, longitude: float) -> dict:
    """Approximate sunrise/sunset transitions within one UTC calendar day.

    A location's local calendar day may span two UTC dates. The Android port
    must use the user's explicit camera timezone for local-date formatting.
    """
    _check_coordinates(latitude, longitude)
    start = datetime(day.year, day.month, day.day, tzinfo=timezone.utc)
    step = timedelta(minutes=30)

    def above_sunrise_threshold(instant: datetime) -> bool:
        return solar_elevation_utc(instant, latitude, longitude) >= SUNRISE_ELEVATION_DEG

    events = []
    previous = start
    prev_daylight = above_sunrise_threshold(previous)

    for offset in range(1, 49):
        current = start + offset * step
        now_daylight = above_sunrise_threshold(current)
        if now_daylight != prev_daylight:
            low, high = previous, current
            for _ in range(18):
                middle = low + (high - low) / 2
                if above_sunrise_threshold(middle) == prev_daylight:
                    low = middle
                else:
                    high = middle
            events.append({
                "type": "sunrise" if now_daylight else "sunset",
                "utc": high.isoformat(),
            })
        previous, prev_daylight = current, now_daylight

    if not events:
        state = "polar_day" if prev_daylight else "polar_night"
    else:
        state = "normal"
    return {"dateUtc": day.isoformat(), "state": state, "events": events}
