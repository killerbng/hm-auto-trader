# Porting HM Auto-trader 26.2 -> 26.3

This is a maintenance checklist, **not** a claim that a 26.2 JAR works on 26.3.

1. Clone/copy the project into a `26.3` working tree. Back up `config/hm-autotrader-favorites.txt` before testing.
2. In `gradle.properties`, change `minecraft_version=26.3`, update Loader/Fabric API and Loom to 26.3-compatible versions, change `mod_version=0.2.0+26.3`. Fabric for 26.3 documents Loom 1.18.x and Fabric API 0.161.0+26.3 or newer.
3. In `BUILD-WINDOWS.ps1`, change Gradle 9.5.0 to a version supported by Loom 1.18.x (typically Gradle 9.7.1), or use a generated Gradle wrapper.
4. Review 26.3 changes in `net.minecraft.client.input.MouseButtonEvent` for `mixin/MerchantScreenMixin.java`; `MerchantScreen.mouseClicked` signature and row layout; `net.minecraft.client.Minecraft.gui.screen()` and new UI behavior.
5. Review merchant selection packet and inventory clicks in `TraderController.java`: `ServerboundSelectTradePacket`, `MerchantMenu.tryMoveItems`, `handleInventoryMouseClick`, and server transaction timing.
6. Recompile; test first on a single villager with a few items, then on a multiplayer test server before using it around storage/builds.
7. When upgrading, retain the **mod ID `hm_autotrader`** and config filenames to preserve settings and favorites.

No MaLiLib, Item Scroller, Autotrade Plus or external config UI libraries need version updates for this mod. It only needs updated Fabric Loader/Fabric API and the Minecraft API adjustments above.

## 0.1.2 Minecraft 26.2 API notes

- Use `net.minecraft.world.inventory.ContainerInput`, **not** `ClickType`, for 26.x inventory actions.
- MerchantScreen GUI coordinates: `getLeftPos()` / `getTopPos()`, not `getGuiLeft()` / `getGuiTop()`.

### 0.1.2 API compatibility fix

Minecraft 26.2 uses `MultiPlayerGameMode.handleContainerInput` with `ContainerInput.QUICK_MOVE`; the GUI origin comes from `AbstractContainerScreenAccessor` for `leftPos` / `topPos`. Check both when porting to 26.3.
