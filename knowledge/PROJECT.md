---
title: Dynamite — project
type: project
layer: store
tags: [minecraft, neoforge, explosives]
---

# Dynamite

A throwable three-stick bundle: lit on the throw, bounces, rests, goes off
on a two-second fuse at half a TNT's power. NeoForge 1.21.1, both sides,
`dynamite`, one of Rusty's self-built mods (public at
github.com/the-rusty-shackleford), in the shared modpack.

## Shape

- `domain` (JDK-only, plain JUnit): `Fuse` (goes off / remaining / hisses on
  a beat), `Bounce` (reflect into a unit-axis normal at 0.35, keep 0.45 of
  the slide, rest on a floor under 0.08), `Blast` (power, breaksBlocks,
  reach = 1.3 × power / 0.225 × 0.3).
- `main`: `Dynamite` (entry: config, registries, dispenser projectile
  behaviour, Combat tab), `DynamiteConfig` (SERVER: fuseTicks 40, power 2,
  breakBlocks, cooldownTicks 10), `DynamiteItem` (use → throw at 1.5 with a
  cooldown; `ProjectileItem` for dispensers), `ThrownDynamite`
  (`ThrowableItemProjectile` that bounces instead of breaking; hiss every
  10 ticks; explode with `ExplosionInteraction.TNT` or `NONE`; smoke and a
  spark client-side), `ModEntities`, `ModItems`, `ModSounds`,
  `client/DynamiteClient` (`ThrownItemRenderer`).
- Data: shapeless recipe 3 `c:gunpowders` + 2 paper + 1 `c:strings`, with a
  recipe-book unlock on gunpowder.
- `gametest`: seven tests in batches by config (blast / gentle / flight);
  the booth throws through `gameMode.useItem` and photographs.
- `devtools/art/build.py`: the 16×16 icon as a character map; the three
  sounds cut with ffmpeg from CC0 recordings in `devtools/art/sounds/src`.

## How it is verified

`./gradlew check`: 12 JUnit tests; the gametest server's seven; the booth's
four checks and five photographs.

## Decisions

D-0001: a timer fuse, bounce not break, half a TNT, the game's own
explosion.

## Next

Asked by Rusty 2026-09-09 ("throwable dynamite ... functionally like weaker
TNT that you can throw like a projectile"; "Don't forget sound effects!"),
delivered as 1.0.0 the same day.
