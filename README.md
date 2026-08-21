# Playful Watch

Open-source Wear OS watch faces built on a clean-room [Compose O'Clock](../ComposeOClock) fork.

## Modules

- **playful-foundation** — shared color scheme (`PlayfulColorScheme`, `LocalPlayfulColors`)
- **watchfaces** — face implementations (Hourglass, Liquid Vials) + gallery switcher
- **wear** — Wear APK with product flavors (`collection`, `hourglass`, `liquidVials`)
- **phone** — companion gallery for preview and theme swatches

## Build

```bash
./gradlew :wear:assembleCollectionDebug :phone:assembleDebug
```

Requires sibling checkout of [compose-oclock](https://github.com/ahugenb333/compose-oclock):

```bash
git clone https://github.com/ahugenb333/compose-oclock.git ~/ComposeOClock
git clone https://github.com/ahugenb333/playful-watch.git ~/Projects/playful-watch
```

The composite build in `settings.gradle.kts` expects `../../ComposeOClock` relative to this repo (e.g. `~/Projects/playful-watch` + `~/ComposeOClock`).

## License

Apache 2.0
