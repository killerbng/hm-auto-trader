package dev.hm.autotrader;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FavoritesScreen extends Screen {
    private final Screen parent;
    private int page;
    public FavoritesScreen(Screen parent) { super(Component.literal("HM Auto-trader favorites")); this.parent = parent; }

    @Override protected void init() {
        List<String> entries = HMAutoTraderClient.FAVORITES.entries();
        int c = width / 2;
        int y = Math.max(15, height / 2 - 102);
        addRenderableWidget(Button.builder(Component.literal("Global favorites: " + entries.size()), b -> {})
            .bounds(c - 148, y, 296, 20).build()).active = false;
        y += 24;
        int offset = page * 6;
        for (int i = offset; i < Math.min(offset + 6, entries.size()); i++) {
            String entry = entries.get(i);
            String[] parts = entry.split("\\|", -1);
            String a = shortId(parts[0]);
            String b = "-".equals(parts[1]) ? "" : " + " + shortId(parts[1]);
            String result = shortId(parts[2]);
            int rowY = y + (i - offset) * 23;
            addRenderableWidget(Button.builder(Component.literal(a + b + " => " + result), button -> {})
                 .bounds(c - 148, rowY, 238, 20).build()).active = false;
            addRenderableWidget(Button.builder(Component.literal("X"), button -> {
                HMAutoTraderClient.FAVORITES.remove(entry);
                rebuildWidgets();
            }).bounds(c + 94, rowY, 54, 20).build());
        }
        int bottom = y + 6 * 23 + 4;
        addRenderableWidget(Button.builder(Component.literal("< Previous"), b -> {
            page = Math.max(0, page - 1); rebuildWidgets();
        }).bounds(c - 148, bottom, 92, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
            page++; rebuildWidgets();
        }).bounds(c - 50, bottom, 92, 20).build()).active = (page + 1) * 6 < entries.size();
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
           .bounds(c + 48, bottom, 100, 20).build());
    }
    private static String shortId(String id) { return id.replace("minecraft:", ""); }
    @Override public void onClose() { this.minecraft.gui.setScreen(parent); }
}
