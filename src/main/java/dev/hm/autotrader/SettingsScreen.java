package dev.hm.autotrader;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Built-in menu; never requires Mod Menu, Cloth Config, or MaLiLib. */
public final class SettingsScreen extends Screen {
    private static final String[] PROFESSIONS = {
            "minecraft:cleric", "all", "minecraft:farmer", "minecraft:fletcher", "minecraft:librarian",
            "minecraft:armorer", "minecraft:toolsmith", "minecraft:weaponsmith", "minecraft:mason",
            "minecraft:butcher", "minecraft:shepherd", "minecraft:cartographer", "minecraft:fisherman", "minecraft:leatherworker"
    };
    private final Screen parent;
    private final Settings cfg = HMAutoTraderClient.SETTINGS;

    public SettingsScreen(Screen parent) {
        super(Component.literal("HM Auto-trader"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int c = width / 2;
        int y = Math.max(20, height / 2 - 110);
        addRenderableWidget(Button.builder(Component.literal("HM Auto-trader  •  26.2"), b -> {})
                .bounds(c - 115, y, 230, 20).build()).active = false;
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Trading: " + (cfg.enabled ? "ON - click to STOP" : "OFF - click to START")), b -> {
            cfg.enabled = !cfg.enabled;
            HMAutoTraderClient.TRADER.reset();
            saveRefresh();
        }).bounds(c - 115, y, 230, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Profession: " + cfg.profession.replace("minecraft:", "")), b -> {
            int next = 0;
            for (int i = 0; i < PROFESSIONS.length; i++) if (PROFESSIONS[i].equals(cfg.profession)) { next = (i + 1) % PROFESSIONS.length; break; }
            cfg.profession = PROFESSIONS[next];
            saveRefresh();
        }).bounds(c - 115, y, 230, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Range: " + cfg.range + " blocks (-)"), b -> {
            cfg.range = Math.max(2, cfg.range - .5); saveRefresh();
        }).bounds(c - 115, y, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Range (+)"), b -> {
            cfg.range = Math.min(6, cfg.range + .5); saveRefresh();
        }).bounds(c + 3, y, 112, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Click delay: " + cfg.clickDelayTicks + "t (-)"), b -> {
            cfg.clickDelayTicks = Math.max(2, cfg.clickDelayTicks - 1); saveRefresh();
        }).bounds(c - 115, y, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Click delay (+)"), b -> {
            cfg.clickDelayTicks = Math.min(20, cfg.clickDelayTicks + 1); saveRefresh();
        }).bounds(c + 3, y, 112, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Rest after round: " + cfg.roundDelayTicks / 20 + "s (-)"), b -> {
            cfg.roundDelayTicks = Math.max(40, cfg.roundDelayTicks - 20); saveRefresh();
        }).bounds(c - 115, y, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Round rest (+)"), b -> {
            cfg.roundDelayTicks = Math.min(2400, cfg.roundDelayTicks + 20); saveRefresh();
        }).bounds(c + 3, y, 112, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Stop if inventory full: " + (cfg.stopIfFull ? "YES" : "NO")), b -> {
            cfg.stopIfFull = !cfg.stopIfFull; saveRefresh();
        }).bounds(c - 115, y, 230, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Auto-close trade screens: " + (cfg.closeAfterVillager ? "YES" : "NO")), b -> {
            cfg.closeAfterVillager = !cfg.closeAfterVillager; saveRefresh();
        }).bounds(c - 115, y, 230, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Favorites (" + HMAutoTraderClient.FAVORITES.size() + ")"), b ->
            this.minecraft.gui.setScreen(new FavoritesScreen(this)))
                .bounds(c - 115, y, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(c + 3, y, 112, 20).build());
    }
    private void saveRefresh() { cfg.save(); rebuildWidgets(); }
    @Override public void onClose() { cfg.save(); this.minecraft.gui.setScreen(parent); }
}
