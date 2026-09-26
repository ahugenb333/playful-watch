# Playful Watch

Open-source Wear OS watch faces (Hourglass, Liquid Vials, and a small gallery) built on a
[Compose O'Clock](https://github.com/ahugenb333/compose-oclock) fork.

These are **real watch faces**, not a clock you keep open as an app. Each Wear flavor registers a
`WatchFaceService` so you can set Hourglass (or the collection) as the face on a Pixel Watch.

## Two repositories

| Repo | Role |
|------|------|
| [compose-oclock](https://github.com/ahugenb333/compose-oclock) | Engine: Compose canvas runtime, `ComposeWatchFaceService`, `OClockCanvas`, time/ambient locals, tap handling, complications helpers, and the library’s own sample faces |
| **playful-watch** (this repo) | Product: original faces, theming, Wear packaging, and a phone preview app |

Compose O'Clock is the fastest way to *draw* a Wear OS face with Compose. It is not a catalog of
Playful Watch designs. This repo consumes it as a **composite Gradle build** (`includeBuild`) and
ships faces you can sideload.

Upstream Compose O'Clock is dual-licensed (Apache 2.0 for debug; commercial for release apps). This
project currently depends on a fork at `ahugenb333/compose-oclock` so the engine can be built from
source with Gradle `includeBuild` — no Maven/JitPack publish step.

### What this repo adds (not in Compose O'Clock)

- **Hourglass** and **Liquid Vials** face implementations (`watchfaces`)
- **Playful theming** — `PlayfulColorScheme`, `LocalPlayfulColors`, per-face palettes
- **Wear distribution** — product flavors (`collection`, `hourglass`, `liquidVials`),
  `WatchFaceService` wiring, and wallpaper/`WATCH_FACE` manifests
- **On-watch gallery** — `GalleryActivity` plus `PlayfulFaceGallery` for switching faces in the
  collection APK
- **Phone companion** — preview gallery and color swatches (`:phone`), not required to run a face

Shared drawing APIs (`OClockCanvas`, `LocalTime`, `LocalIsAmbient`, `ComposeWatchFaceService`) live
in Compose O'Clock. Face art, brands, APKs, and gallery UX live here.

## Layout on disk

Keep the two repos **side by side**. Gradle `includeBuild`s `../compose-oclock` — no Maven/JitPack
publish, and the engine stays its own git checkout.

```
some-dir/
├── compose-oclock/
└── playful-watch/
```

```bash
git clone https://github.com/ahugenb333/compose-oclock.git ../compose-oclock
```

If the engine already lives elsewhere, symlink it into that sibling path:

```bash
ln -s /path/to/compose-oclock ../compose-oclock
```

## Modules

- **playful-foundation** — shared color scheme (`PlayfulColorScheme`, `LocalPlayfulColors`)
- **watchfaces** — face implementations (Hourglass, Liquid Vials) + gallery switcher
- **wear** — Wear APK with product flavors (`collection`, `hourglass`, `liquidVials`)
- **phone** — companion gallery for preview and theme swatches

## Build

```bash
./gradlew :wear:assembleCollectionDebug :phone:assembleDebug
```

| Flavor | applicationId | What you get |
|--------|---------------|--------------|
| `collection` | `com.playfulwatch.wear.collection` | All faces + on-watch gallery |
| `hourglass` | `com.playfulwatch.wear.hourglass` | Hourglass only |
| `liquidVials` | `com.playfulwatch.wear.liquidvials` | Liquid Vials only |

The phone module is preview only. It does **not** install a face on the watch.

## Use as a watch face (Pixel Watch 3)

Sideload the Wear APK **onto the watch**, then pick it in the face picker. Wireless debugging is the
usual path on Pixel Watch.

### 1. Enable ADB on the watch

1. **Settings → System → About** → tap **Build number** until developer options unlock.
2. **Settings → Developer options** → enable **ADB debugging** and **Wireless debugging**.
3. Pair from the machine running adb (`adb pair <watch-ip>:<pair-port>` plus the pairing code), then
   `adb connect <watch-ip>:<connect-port>`.
4. Confirm with `adb devices` — the watch must be listed (not only the phone).

### 2. Install

Hourglass only:

```bash
./gradlew :wear:installHourglassDebug
```

All faces + gallery:

```bash
./gradlew :wear:installCollectionDebug
```

If Gradle does not see the watch, assemble then install:

```bash
./gradlew :wear:assembleHourglassDebug
adb install -r wear/build/outputs/apk/hourglass/debug/wear-hourglass-debug.apk
```

### 3. Set it as the clock

On the watch: long-press the clock → swipe to **Hourglass** (or **Playful Watch**) → tap to apply.

On the phone: **Google Pixel Watch** app → **Watch faces**. Sideloaded faces often appear after a
short delay.

### If it does not show up

- Confirm the package is on the **watch**: `adb shell pm path com.playfulwatch.wear.hourglass`
- `collection` and `hourglass` use different application ids; uninstall the old flavor if you switch.
- Play Protect may prompt on sideload; allow the install if asked.

After that the face runs as the system clock (including ambient), not as an activity you leave open.

## License

Apache 2.0 (this repository). Compose O'Clock’s license still applies to that dependency.
