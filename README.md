# HM Auto-trader 0.1.3 (Minecraft Java 26.2, Fabric)

**Standalone client-side mod.** Name and mod ID are not tied to a server or network.
No dependency on MaLiLib, Item Scroller, Autotrade Plus, Cloth Config or Mod Menu.
Runtime requirements: **Fabric Loader + Fabric API** (as with many other Fabric mods).

> STATUS: v0.1.2 was confirmed working by a user on Minecraft 26.2; **v0.1.3 changes are not yet compiled or game-tested** in the authoring environment (which lacks JDK 25 / downloadable Gradle). Test on a spare installation / world first. Keep a backup of the working v0.1.2 JAR.

## Build on Windows

1. Extract the entire ZIP to a folder on the desktop.
2. Double-click `BUILD-WINDOWS.bat`. It tries installed JDK 25/26 and Gradle 9.5.0 from the cache used by previous projects, otherwise downloads official Gradle.
3. The compiled JAR, if successful, will appear in `build\libs\hm-auto-trader-0.1.3+26.2.jar`.
4. Install the compiled JAR into the **Fabric 26.2 client's** `mods` folder. Fabric API must be present.
5. Temporarily remove **Item Scroller** and **Autotrade Plus** for testing, since both can intercept the same trade inputs. Keep Litematica, MiniHUD, Tweakeroo, and MaLiLib!

If compilation fails, copy the **first error block** (not just the last BUILD FAILED line).

**Alternative build:** The included `.github/workflows/build.yml` builds using GitHub Actions with JDK 25 and uploads the resulting mod JAR as a workflow artifact if you put this source in your own GitHub repository. No MaLiLib or Item Scroller dependencies are introduced by this route.

## Controls

- **Shift + middle-click** on a visible villager *trade row* in the merchant screen: add/remove a **global favorite**. Favorites now save to `config/hm-autotrader-favorites.json` (automatic migration from the existing `.txt`, which is kept as a backup).
- **O** (outside a GUI): open the native HM Auto-trader settings screen.
- **~** (grave accent): start/stop automatic trading. Off on startup by design.
- All keybinds appear in Minecraft's Controls > Key Binds > HM Auto-trader category.
- In the settings GUI: choose profession (defaults to **cleric**), scan radius (2-32 blocks), click delay, round rest, full-inventory safety, and view/remove saved favorites.

## Suggested first test

1. Use a single cleric and a small amount of rotten flesh.
2. Shift + middle-click **the rotten-flesh-to-emerald row** and **the emerald-to-lapis row**. The action bar will report saved/removed.
3. Press O and ensure Profession = cleric, Trade click delay = 4 ticks (increase if laggy).
4. Exit the menu, press ~ to enable. It should open the nearby cleric, execute saved trades, and close the screen.
5. Watch your inventory and the server logs. If any issue, immediately toggle off.

### Safety / limits

- The mod never drops items deliberately. Stops when the output can't fit in the player inventory (default).
- Trades use standard villager GUI and inventory-click packets. Server-side checks, stocks, restocking, latency, and anti-cheat still apply.
- Favorites match **item IDs for both inputs and output**, not specific villagers or trade positions. Custom potion/component variations are **not** distinguished in v0.1.
- No direct import of Item Scroller favorites; re-favorite the two desired trades manually.
- No background trading while logged out or GUI closed; client must be running.
- The first version is aimed at vanilla single-output villager trades. It is not tested with modded/custom recipe trades or servers with exotic trade plugins.

## Config files

- `config/hm-autotrader.properties` (normal settings)
- `config/hm-autotrader-favorites.json` (global favorite trades, editable JSON array)
- `config/hm-autotrader-favorites.txt` (old v0.1.2 file, retained unchanged after automatic first-launch import)

## Changes in v0.1.3

- Scan-radius setting now allows **2–32 blocks** (up to 6 in 0.5 increments; after 6 in 2-block increments). **This is NOT extended player reach.** The mod skips villagers outside your actual, server-enforced entity interaction range (normally 3 blocks in Survival, 5 Creative). The server can change that reach attribute legitimately; the client mod does not bypass it.
- Start/stop chat messages use **white brackets**, **pink HM Auto-trader**, **green Enabled**, **red Stopped**. Both the `~` key and the in-menu toggle use the same function.
- New favorites JSON format, easy to edit. Example:

  ```json
  [
    {"sell": "minecraft:rotten_flesh", "buy": "minecraft:emerald"},
    {"sell": "minecraft:emerald", "buy": "minecraft:lapis_lazuli"}
  ]
  ```

  For trades needing two inputs, include an optional `"sell_extra": "minecraft:second_item"`. This is still *item-ID matching* only and does not preserve the exact counts, discounts, or NBT/components of each offer. JSON is read on startup and re-saved when you change favorites in game.

## Updating to Minecraft 26.3

See `PORTING-26.3.md`. Java networking and trading code is isolated in `TraderController.java`, screen interception is isolated in `mixin/MerchantScreenMixin.java`, and the Gradle/tool versions are all in `gradle.properties`.
