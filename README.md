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

Minecraft 26.1.2, Fabric Loader 0.18+, Fabric API, Java 25.

## Building

```
./gradlew build
```

The jar ends up in `build/libs/`.

## License

MIT
