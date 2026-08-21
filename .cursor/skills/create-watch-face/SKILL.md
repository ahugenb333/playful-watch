---
name: create-watch-face
description: Create a new Playful Watch face using Compose O'Clock. Use when adding watch faces to playful-watch, designing layers, or wiring WatchFaceService entries.
---

# Create a Playful Watch Face

## Prerequisites

- `compose-oclock` fork builds (`oclock-core`, `oclock-watchface-renderer`)
- `playful-watch` modules: `playful-foundation`, `watchfaces`, `wear`, `phone`

## Face design spec

1. Split rendering into **layers** with separate `OClockCanvas { }` blocks (background, hands, accents).
2. Read time from `LocalTime.current` and ambient mode from `LocalIsAmbient.current`.
3. Pull colors from `LocalPlayfulColors.current` — never hard-code theme colors in face logic.
4. In ambient mode, reduce motion (hide seconds), prefer strokes over fills, lower alpha.
5. Register tap targets only on dedicated edge/control layers; return `false` when not handled.

## Template

```kotlin
@Composable
fun MyFace() {
    val colors = LocalPlayfulColors.current
    val time = LocalTime.current
    val isAmbient by LocalIsAmbient.current

    OClockCanvas {
        drawCircle(colors.background, radius = size.minDimension / 2f)
    }
    OClockCanvas {
        // animated layer — skip heavy work when isAmbient
    }
}
```

## Register the face

1. Add composable in `watchfaces/src/main/kotlin/.../MyFace.kt`
2. Register in `PlayfulWatchFaces.all` with `id`, `displayName`, `defaultColors`
3. Add `WatchFaceService` in `wear/.../WatchFaceServices.kt`
4. Add flavor manifest under `wear/src/<flavor>/AndroidManifest.xml` if shipping standalone
5. Phone gallery picks up faces automatically via `PlayfulWatchFaces.all`

## Preview

Use `OClockRootCanvas` in the phone app or `@Preview` with `PlayfulTheme(defaultColors) { MyFace() }`.

## Distribution flavors

| Flavor | applicationId | Use |
|--------|---------------|-----|
| `collection` | `com.playfulwatch.wear.collection` | All faces, gallery switcher |
| `hourglass` | `com.playfulwatch.wear.hourglass` | Single face APK |
| `liquidVials` | `com.playfulwatch.wear.liquidvials` | Single face APK |

Build: `./gradlew :wear:assembleCollectionDebug :wear:assembleHourglassDebug`
