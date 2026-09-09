# Dynamite

A three-stick bundle of dynamite you can throw. A server-and-client mod for
NeoForge 1.21.1.

TNT is a demolition charge: a block to lay, light and run from, and a crater
to match. Sometimes you just want to clear a little area with blasting.
Dynamite is a grenade: right-click throws the bundle lit, it flies, bounces,
comes to rest hissing and smoking, and goes off two seconds after the throw
wherever it is -- in the air, over a wall, down a hole -- with half the
power of a block of TNT.

- **Crafted** shapeless from 3 gunpowder, 2 paper and 1 string (the fuse);
  one bundle. It is in the Combat tab, and any recipe viewer shows it.
- **Thrown** like a snowball, a little over a block a tick, with a short
  cooldown between throws. A dispenser throws it lit too.
- **The fuse is a timer, not a trigger.** The bundle does not go off on
  impact; it bounces off what it hits -- a third of its speed back out of a
  surface, under half of its slide kept -- and lies still on a floor once it
  is barely moving. Two seconds after the throw, it goes off.
- **The blast** is the game's own explosion at power 2 (TNT is 4): about a
  three-block reach through air, a shallow crater in dirt, a block or so
  into stone, blocks dropping at TNT's rate, no fire, damage to whatever
  stands near it including the thrower. Under water it hurts and breaks
  nothing, as explosions do.
- **Sounds:** a whoosh on the throw, the fuse hissing while it is lit, a
  thud where it lands, and the explosion everyone knows. All three of the
  mod's own are cut from CC0 recordings (`devtools/art/sounds/SOURCES.md`).

Needed on the server and on every client: the bundle in flight is an entity.

## Config

`serverconfig/dynamite-server.toml` in the world:

| Key | Default | Meaning |
|---|---|---|
| `dynamite.fuseTicks` | 40 | ticks from the throw to the blast |
| `dynamite.power` | 2.0 | the blast's power; a block of TNT is 4 |
| `dynamite.breakBlocks` | true | whether the blast breaks blocks at all |
| `dynamite.cooldownTicks` | 10 | ticks a player waits between throws |

## How it works

`ThrownDynamite` is a `ThrowableItemProjectile` that does not break on
impact: its block and entity hits go through the domain's `Bounce`, which
reflects the speed into the surface at a third and keeps under half of the
slide, and declares the bundle at rest on a floor below a small speed. Each
server tick the domain's `Fuse` says whether it hisses (every ten ticks, the
sound being a little longer, so the hisses run on) and whether it is due;
when it is, the level explodes at the bundle with the configured power and
the TNT interaction, or none when block breaking is off, and the bundle is
gone. The client draws the entity as its item, spinning, and puts smoke and
a spark off the fuse every tick.

## Building and checking

Java 21. `./gradlew build` produces `build/libs/dynamite-<version>.jar`.

`./gradlew check` runs the plain-JUnit tests against the pure layer, the
gametest server (seven tests in a fifteen-block dirt arena: the fuse's
timing and the crater it leaves, the reach nothing beyond is touched
within, a bounce off a wall, coming to rest, the dispenser, the stack and
the cooldown, and block breaking off), and the photo booth (a client that
throws a bundle through the real use path and photographs it in the hand,
in flight, lying lit, the blast and the crater into
`run/booth/screenshots/`). The booth needs a display; `-PskipBooth` leaves
it out, `-PskipGameTests` both. Headless: `DISPLAY=:1 Xephyr :7 -screen
1280x720 -ac -br -noreset` then `DISPLAY=:7 __GLX_VENDOR_LIBRARY_NAME=mesa
LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe ./gradlew check`.

Layout: `src/domain` is the pure layer, compiled against nothing but the JDK
-- `Fuse`, `Bounce`, `Blast`. `src/main` is the mod: the item, the entity,
the sounds, the config, the client's renderer. `src/gametest` is the
gametests and the booth, a mod of its own, never shipped.
`devtools/art/build.py` draws the icon and cuts the sounds.

## License

AGPL-3.0-or-later. Copyright (C) 2026 Rusty Shackleford and nfx.
