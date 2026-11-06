# Hypixel SkyBlock Coin Hider

<img src="icon.png" width="128" alt="Icon">

Hides coin drops on the ground in Hypixel SkyBlock, like the coins from the **Coin Talisman**.

- **Coins are not drawn** on the floor.
- **No pickup animation** for coins (the coin flying into you).
- **Only active in SkyBlock.** Detected from the sidebar scoreboard, so it does nothing anywhere else.
- **Client-side and visual only.** The coins still exist and you still pick them up. Nothing is sent to the server.
- **No config, no commands, no extra libraries.** Install it and it works.

## How coins are detected

Hypixel's coin drops are plain player heads with no custom name and no SkyBlock item ID. Every real SkyBlock item has an ID, so pets, accessories, skins and other loot are never hidden.

Something hidden that shouldn't be, or a coin that isn't? [Open an issue](https://github.com/evolveperformance/hypixel-skyblock-coin-hider/issues).

## Requirements

Minecraft 26.1.x, 26.2 or 26.3 (pick the matching jar), Fabric Loader 0.18+, Fabric API, Java 25.

## Building

```
sh build-all.sh
```

Builds one jar per supported Minecraft version into `build/libs/`. Versions are listed at the bottom of `build-all.sh`. `./gradlew build` alone builds the default from `gradle.properties` (26.1.2).

## License

MIT
