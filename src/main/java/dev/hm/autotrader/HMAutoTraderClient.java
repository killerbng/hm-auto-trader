package dev.hm.autotrader;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HMAutoTraderClient implements ClientModInitializer {
    public static final String MOD_ID = "hm_autotrader";
    public static final Settings SETTINGS = new Settings();
    public static final Favorites FAVORITES = new Favorites();
    public static final TraderController TRADER = new TraderController();
    private static KeyMapping toggleKey;
    private static KeyMapping settingsKey;

    @Override
    public void onInitializeClient() {
        SETTINGS.load();
        FAVORITES.load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "keys"));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hm_autotrader.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, category));
        settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hm_autotrader.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, category));
        ClientTickEvents.END_CLIENT_TICK.register(HMAutoTraderClient::tick);
    }

    private static void tick(Minecraft client) {
        if (client.player == null) {
            TRADER.reset();
            SETTINGS.enabled = false;
            return;
        }
        if (client.gui.screen() == null) {
            while (settingsKey.consumeClick()) client.gui.setScreen(new SettingsScreen(null));
            while (toggleKey.consumeClick()) {
                SETTINGS.enabled = !SETTINGS.enabled;
                SETTINGS.save();
                TRADER.reset();
                client.player.sendSystemMessage(Component.literal("[HM Auto-trader] " +
                        (SETTINGS.enabled ? "Enabled" : "Stopped")));
            }
        } else {
            while (toggleKey.consumeClick()) { /* discard while GUI open */ }
            while (settingsKey.consumeClick()) { /* discard while GUI open */ }
        }
        TRADER.tick(client);
    }

    public static void status(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.gui.hud.setOverlayMessage(Component.literal("HM Auto-trader: " + message), false);
    }
}
