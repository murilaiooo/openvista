# R18 — Discover This Place: contextual facts without guessing geography

**Verdict:** HOLD pending licensed provider selection and location match proof. E1 code audit, no provider API transaction or device UX test. [Research #46](https://github.com/murilaiooo/openvista/issues/46).

## Why this could matter
An amazing live view is more engaging if the viewer can discover the place on request: where it is, why it matters, and what nearby points are known. But vague labels like “Springfield” or “Silver City” occur in many countries. **Wrong context is worse than no context**, and the ambient scene must remain the focus.

## Baseline and data trust
[LibraryItem](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) has optional user city, country, lat/long, timezone, attribution. [CountryResolver.kt](../../../player/src/main/java/com/openvista/prototype/ambient/CountryResolver.kt) and [LocationMatch.kt](../../../player/src/main/java/com/openvista/prototype/ambient/LocationMatch.kt) handle camera country/city; [LocationLookup.kt](../../../player/src/main/java/com/openvista/prototype/ambient/LocationLookup.kt) resolves incomplete location. [SourceMapView](../../../player/src/main/java/com/openvista/prototype/ambient/SourceMapView.kt) shows cameras with coordinates on an optional map. None proves the scene's exact landmark or vantage direction.

## Source research
Candidate **Wikidata/Wikipedia** entity summaries: stable structured identifiers, descriptions, names/language variants and coordinates, but entity ambiguity, user-agent/rate/attribution policy and licensing require verification before use. Candidate **OpenStreetMap Overpass** query around a verified coordinate: point-of-interest metadata, not automatically meaningful explanatory text; API load/rate limits and ODbL attribution matter. Candidate **local user-entered place notes:** reliable about what owner intended, inexpensive and offline; should be supported before relying on outside APIs.

**Never:** reverse-geocode from unknown stream URL as if it identifies camera; call provider for every scene switch; show sourced facts without citation; mistake forecast location for actual camera location.

## Match algorithm proposal
1. Require valid coordinates or explicit user-confirmed place ID. Separate “camera location”, “weather forecast point” and “place of interest”.
2. Query on **explicit Info action**, not permanent background; cache by source ID + coordinate revision + provider license/provenance with TTL.
3. Score candidate match by distance, known city/country, type and disambiguation; if ambiguous show “Choose a place” or no description. Do not write external candidate back into user's original source record without confirmation.
4. Allow language-specific labels EN/PT-BR and original local name, with offline “Saved description (may be outdated)” label.

## Test plan
Fixtures: 10 Springfields, duplicate Paris cities, New York city/state, remote cliff camera with nearest place 20km away, coordinates in oceans, source relocation, rate 429, XML/JSON malicious strings, long Unicode names, 720p truncation, airplane-mode offline. Record precision of suggested place matching and refusal rate. Research target: **zero assertions of exact landmark from title alone**; place cards open within 2 user actions of player OK. No empirical precision rate yet.

## User interaction
Player → Info → “About this place” (collapsed by default); Camera detail → Location → optional place note; Map → nearby saved cameras. No extra Home section or permanent third-party logos over scenery; visible attribution only where data is displayed. If no location: “Add a location to see place details”.

## Stop criteria
Provider disallows display/caching without impractical terms, unauthenticated endpoint exceeds rate limits, user location sent implicitly, or high false match rate. This is optional enrichment, not prerequisite for camera playback. Check [OpenStreetMap attribution policy](https://www.openstreetmap.org/copyright) and [Wikidata access guidance](https://www.wikidata.org/wiki/Wikidata:Data_access) before selecting provider; external policies **were not revalidated on GitHub**.