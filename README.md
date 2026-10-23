# HM Auto-trader 0.1.2 (Minecraft Java 26.2, Fabric)

**Standalone client-side mod.** Name and mod ID are not tied to a server or network.
No dependency on MaLiLib, Item Scroller, Autotrade Plus, Cloth Config or Mod Menu.
Runtime requirements: **Fabric Loader + Fabric API** (as with many other Fabric mods).

> STATUS: source implementation prepared; **not compiled or tested in-game** in the authoring environment (which lacks JDK 25 / downloadable Gradle). Updated 0.1.2 addresses the remaining 26.2 API errors: handleContainerInput rather than handleInventoryMouseClick, and a Mixin accessor for inherited leftPos/topPos (which have no public getters). Test on a spare installation / world first. Don't delete your working trading mods before validating this replacement.

## Build on Windows

1. Extract the entire ZIP to a folder on the desktop.
2. Double-click `BUILD-WINDOWS.bat`. It tries installed JDK 25/26 and Gradle 9.5.0 from the cache used by previous projects, otherwise downloads official Gradle.
3. The compiled JAR, if successful, will appear in `build\libs\hm-auto-trader-0.1.2+26.2.jar`.
4. Install the compiled JAR into the **Fabric 26.2 client's** `mods` folder. Fabric API must be present.
5. Temporarily remove **Item Scroller** and **Autotrade Plus** for testing, since both can intercept the same trade inputs. Keep Litematica, MiniHUD, Tweakeroo, and MaLiLib!

If compilation fails, copy the **first error block** (not just the last BUILD FAILED line).

**Alternative build:** The included `.github/workflows/build.yml` builds using GitHub Actions with JDK 25 and uploads the resulting mod JAR as a workflow artifact if you put this source in your own GitHub repository. No MaLiLib or Item Scroller dependencies are introduced by this route.

## Controls

- **Shift + middle-click** on a visible villager *trade row* in the merchant screen: add/remove a **global favorite**. This saves favorites to `config/hm-autotrader-favorites.txt`.
- **O** (outside a GUI): open the native HM Auto-trader settings screen.
- **~** (grave accent): start/stop automatic trading. Off on startup by design.
- All keybinds appear in Minecraft's Controls > Key Binds > HM Auto-trader category.
- In the settings GUI: choose profession (defaults to **cleric**), range, click delay, round rest, full-inventory safety, and view/remove saved favorites.

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
- `config/hm-autotrader-favorites.txt` (global favorite trade identifiers)

## Updating to Minecraft 26.3

See `PORTING-26.3.md`. Java networking and trading code is isolated in `TraderController.java`, screen interception is isolated in `mixin/MerchantScreenMixin.java`, and the Gradle/tool versions are all in `gradle.properties`.
