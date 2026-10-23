# HM Auto-trader 0.1.5 (Minecraft Java 26.2, Fabric)

**Standalone client-side mod.** Name and mod ID are not tied to a server or network.
No dependency on MaLiLib, Item Scroller, Autotrade Plus, Cloth Config or Mod Menu.
Runtime requirements: **Fabric Loader + Fabric API** (as with many other Fabric mods).

> STATUS: v0.1.2 was confirmed working by a user on Minecraft 26.2; **v0.1.5 changes are not yet compiled or game-tested** in the authoring environment (which lacks JDK 25 / downloadable Gradle). Test on a spare installation / world first. Keep a backup of the working v0.1.2 JAR.

## Build on Windows

1. Extract the entire ZIP to a folder on the desktop.
2. Double-click `BUILD-WINDOWS.bat`. It tries installed JDK 25/26 and Gradle 9.5.0 from the cache used by previous projects, otherwise downloads official Gradle.
3. The compiled JAR, if successful, will appear in `build\libs\hm-auto-trader-0.1.5+26.2.jar`.
4. Install the compiled JAR into the **Fabric 26.2 client's** `mods` folder. Fabric API must be present.
5. Temporarily remove **Item Scroller** and **Autotrade Plus** for testing, since both can intercept the same trade inputs. Keep Litematica, MiniHUD, Tweakeroo, and MaLiLib!

If compilation fails, copy the **first error block** (not just the last BUILD FAILED line).

**Alternative build:** GitHub Actions builds on `main` / `master` pushes or manual dispatch, and stores the JAR as a downloadable artifact. Pushing a tag matching the current version (e.g., `v0.1.5`) additionally publishes a **GitHub Release with the installable JAR attached**. Version `0.x` releases are initially marked prerelease. The tag must match `mod_version` without the `+26.2` suffix.

A separate `.gitea/workflows/build.yml` is **build-only** and stores the JAR as an artifact. It does NOT publish Gitea releases. Gitea still requires a configured runner to execute; this project does not install or configure one.

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
- `config/hm-autotrader-colors.json` (editable HEX colors for toggle chat messages; created automatically on first launch)
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

## Changes in v0.1.4 (uncompiled source update)

The start/stop chat message now uses editable RGB `#RRGGBB` colors. The mod creates `config/hm-autotrader-colors.json` automatically on startup:

```json
{
  "brackets": "#FFFFFF",
  "name": "#FF55FF",
  "nameBold": true,
  "enabled": "#55FF55",
  "stopped": "#FF5555"
}
```

Edit that file with Minecraft running if desired: colors reload the next time you **toggle trading**. Invalid hex entries use the previous hardcoded default for that field; malformed JSON is logged and not overwritten. No alpha channel, no traditional color-code limitations. Existing trade settings and favorites are unchanged.

**GitHub:** version tags matching `gradle.properties` (for example, `v0.1.5`) trigger automated GitHub Releases with only the installable JAR. Regular pushes still upload a downloadable Actions artifact.

**Gitea:** separate build-only workflow for future use; no Gitea release, no runner changes.

## Changes in v0.1.5 (source update)

- The chat prefix's name can now be **bold** independently of its HEX color.
- `config/hm-autotrader-colors.json` contains `"nameBold": true` by default; set to `false` for normal-weight text. The setting is reloaded before each on/off chat message.
- Existing v0.1.4 color files are upgraded in place to include the new flag without resetting custom HEX values. Keep a backup if you prefer to retain your original JSON formatting.
- The bracket and status text formatting remains unchanged; this setting only changes `HM Auto-trader`.
- GitHub tag-to-release and Gitea build-only automation remain unchanged.
