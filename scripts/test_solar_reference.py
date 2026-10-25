"""Offline reference checks for approximate daylight filtering."""
from datetime import date, datetime, timezone
import unittest

from solar_reference import daylight_state, solar_elevation_utc, sun_events_utc


class SolarReferenceTests(unittest.TestCase):
    def test_equator_equinox_midday_and_midnight(self):
        noon = datetime(2026, 3, 20, 12, tzinfo=timezone.utc)
        midnight = datetime(2026, 3, 20, 0, tzinfo=timezone.utc)
        self.assertGreater(solar_elevation_utc(noon, 0, 0), 85)
        self.assertLess(solar_elevation_utc(midnight, 0, 0), -85)

    def test_equinox_greenwich_sun_events(self):
        events = sun_events_utc(date(2026, 3, 20), 0, 0)
        self.assertEqual(events["state"], "normal")
        self.assertEqual([x["type"] for x in events["events"]], ["sunrise", "sunset"])
        self.assertEqual(len(events["events"]), 2)
        sunrise = datetime.fromisoformat(events["events"][0]["utc"])
        sunset = datetime.fromisoformat(events["events"][1]["utc"])
        self.assertTrue(5 <= sunrise.hour <= 7)
        self.assertTrue(17 <= sunset.hour <= 19)

    def test_polar_day_and_night_have_no_false_events(self):
        summer = sun_events_utc(date(2026, 6, 21), 78.2, 15.6)
        winter = sun_events_utc(date(2026, 12, 21), 78.2, 15.6)
        self.assertEqual(summer["state"], "polar_day")
        self.assertEqual(winter["state"], "polar_night")
        self.assertEqual(summer["events"], [])
        self.assertEqual(winter["events"], [])

    def test_unknown_coordinates_are_not_called_night(self):
        value = daylight_state(datetime(2026, 10, 25, tzinfo=timezone.utc), None, None)
        self.assertEqual(value["state"], "unknown_location")
        self.assertFalse(value["goldenHour"])

    def test_invalid_coordinates_and_naive_datetime_rejected(self):
        now = datetime(2026, 10, 25, tzinfo=timezone.utc)
        with self.assertRaises(ValueError):
            solar_elevation_utc(now, 91, 0)
        with self.assertRaises(ValueError):
            solar_elevation_utc(now, 20, float("nan"))
        with self.assertRaises(ValueError):
            solar_elevation_utc(datetime(2026, 10, 25), 20, 0)

    def test_daylight_filter_does_not_assume_weather(self):
        noon = datetime(2026, 3, 20, 12, tzinfo=timezone.utc)
        state = daylight_state(noon, 0, 0)
        self.assertEqual(state["state"], "daylight")
        self.assertFalse(state["goldenHour"])


if __name__ == "__main__":
    unittest.main()
