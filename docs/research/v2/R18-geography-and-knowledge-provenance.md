# R18 deep investigation — Place knowledge with explicit provenance

**Evidence:** E1 OpenVista source and research assumptions. **Decision:** test user-authored notes and unique geo matches before third-party data enrichment. [Original dossier](../dossiers/R18-place-discovery.md).

## 1. What the app currently knows vs what it does not

[LibraryItem.kt is defined in LibraryModels.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt): city, country, optional latitude/longitude, timezone, attribution. [LocationLookup.kt](../../../player/src/main/java/com/openvista/prototype/ambient/LocationLookup.kt) and [CountryResolver.kt](../../../player/src/main/java/com/openvista/prototype/ambient/CountryResolver.kt) can complete or normalize named locations under guarded conditions. [SourceMapView.kt](../../../player/src/main/java/com/openvista/prototype/ambient/SourceMapView.kt) can plot user sources. **None proves the precise physical camera mounting position, pointing direction or visible landmark**. A geocoded city center could be tens of km from a coastal camera. The weather provider coordinate is not a cultural POI ID.

## 2. Four approaches with their failure surfaces

1. **User notes:** “View from the old harbor, facing west”, stored locally. Accurate to user's knowledge, no third-party terms or calls; best v1 experiment.
2. **Wikidata entity/description by explicit Q-ID or strong coordinate + category match:** standardized identifiers, multilingual labels but very ambiguous in dense cities; require confirmed selection. Provider API availability, request etiquette and attribution need direct verification.
3. **OSM/Overpass nearby POIs:** useful for parks/bridges, but 5 nearest POIs may not be *in the camera frame*. Avoid inferring visibility. Limit distance, request rate and caching; do not scrape standard OSM map tile servers.
4. **Cloud image recognition/vision:** potentially “interesting” but sends private video frames to a service, has rights/privacy/CPU complexity; NO-GO initially.

## 3. Provenance and confidence model

```text
SourceLocation:
  UserConfirmedPoint(lat, lon, optional heading, placeId)
  | ProviderDeclaredPoint(lat, lon, sourceTerms)
  | CityApproximation(lat, lon, geocodeDate)
  | Unknown

PlaceMatch:
  ExplicitUserChosen(id, provider, attribution)
  | Candidate(id, distanceMeters, type, matchReasons)
  | Ambiguous(candidates)
  | NoMatch(reason)
```

**Display rule:** an unverified city coordinate permits “Nearby this location”, not “You are seeing [landmark]”. No automatic “This is the Eiffel Tower” from stream title. Camera's `attribution` and third-party knowledge attribution must remain distinct, including licensing of cached descriptions and source data.

## 4. Explicit failure dataset

| Fixture | Wrong naive result | Acceptable result |
|---|---|---|
| Springfield USA (multiple states) | chooses most popular one | Ambiguous, user chooses |
| Paris Texas vs Paris France | applies French history | city+country scoped |
| webcam titled “Tower Bridge” from another angle | claims visible bridge | nearby only absent user confirmation |
| beach camera coords at city center | nearest museum shown as camera viewpoint | location coarse warning |
| camera moves after installation | stale POI treated as fact | source location revision invalidates cache |
| offshore camera and country missing | nearest mainland attraction | no match |
| 200 geocoded results | request storm | capped candidates and explicit action |
| user changes language | English-only descriptions | PT-BR fallback correctly labeled |
| provider rate 429 / offline | infinite spinner | cached data or no description |
| malicious HTML description | injected interactive UI | escaped/plain text |
| provider returns wrong attribution | unlicensed screenshot | suppress until terms clear |

## 5. Performance and UX

Player OK → Info → About this place; Camera Details → Location → Edit note / Choose known place; normal ambiance has no permanent informational overlay. Cache requests only when user explicitly opens a place, not on every 5-minute camera change. Suggested response budget 2s to first optional card, but not measured. If all data missing, show Add location action, not a fabricated country/timezone. Attribute every external fact where it appears. Source privacy: never send a stream URL, stable token or complete viewing history to a knowledge provider; only explicit coordinates/place ID when permitted.

## 6. Evidence and decision

Need verify Wikimedia/Wikidata and OSM/Overpass **current** request policies from first-party sources, real ambiguous-geolocation precision and cache terms, not just GitHub descriptions. No live API calls performed. E2 pure matching tests with 100 hand-labeled city/POI scenarios can measure precision/abstention; E3 remote-focus and overlay layout on target TV. **HOLD until false-match rate and provider terms are proven**. Do not conflate a geographic map with contextual knowledge.
