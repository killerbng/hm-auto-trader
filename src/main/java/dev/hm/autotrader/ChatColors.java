package dev.hm.autotrader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Optional, user-editable RGB colors for the start/stop chat message.
 * Uses Minecraft's bundled Gson and Style.withColor(int); no extra dependencies.
 */
public final class ChatColors {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_BRACKETS = 0xFFFFFF;
    private static final int DEFAULT_NAME = 0xFF55FF;
    private static final int DEFAULT_ENABLED = 0x55FF55;
    private static final int DEFAULT_STOPPED = 0xFF5555;

    public int brackets = DEFAULT_BRACKETS;
    public int name = DEFAULT_NAME;
    public boolean nameBold = true;
    public int enabled = DEFAULT_ENABLED;
    public int stopped = DEFAULT_STOPPED;

    private Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("hm-autotrader-colors.json");
    }

    /** Reload at startup and before each toggle, so color edits apply without a restart. */
    public void load() {
        brackets = DEFAULT_BRACKETS;
        name = DEFAULT_NAME;
        nameBold = true;
        enabled = DEFAULT_ENABLED;
        stopped = DEFAULT_STOPPED;
        Path file = path();
        if (!Files.exists(file)) {
            createDefaults(file);
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) throw new IllegalArgumentException("Expected a JSON object");
            JsonObject values = root.getAsJsonObject();
            brackets = readColor(values, "brackets", DEFAULT_BRACKETS);
            name = readColor(values, "name", DEFAULT_NAME);
            nameBold = readBoolean(values, "nameBold", true);
            // Existing v0.1.4 installations already have a colors file: expose the new setting
            // without removing or resetting any of the user's own color values.
            if (!values.has("nameBold")) {
                values.addProperty("nameBold", nameBold);
                Files.writeString(file, JSON.toJson(values) + System.lineSeparator(), StandardCharsets.UTF_8);
            }
            enabled = readColor(values, "enabled", DEFAULT_ENABLED);
            stopped = readColor(values, "stopped", DEFAULT_STOPPED);
        } catch (IOException | RuntimeException ex) {
            // Never overwrite or discard an invalid user-edited file.
            System.err.println("[HM Auto-trader] Invalid colors file " + file + ": " + ex + "; using defaults");
        }
    }

    private static boolean readBoolean(JsonObject values, String name, boolean fallback) {
        JsonElement value = values.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            if (value != null) {
                System.err.println("[HM Auto-trader] Invalid boolean for '" + name + "' (expected true or false)");
            }
            return fallback;
        }
        return value.getAsBoolean();
    }

    private static int readColor(JsonObject values, String name, int fallback) {
        JsonElement value = values.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) return fallback;
        String hex = value.getAsString().trim();
        if (!hex.matches("#[0-9a-fA-F]{6}")) {
            System.err.println("[HM Auto-trader] Bad hex color for '" + name + "': '" + hex + "' (expected #RRGGBB)");
            return fallback;
        }
        return Integer.parseInt(hex.substring(1), 16);
    }

    private static void createDefaults(Path path) {
        JsonObject values = new JsonObject();
        values.addProperty("brackets", "#FFFFFF");
        values.addProperty("name", "#FF55FF");
        values.addProperty("nameBold", true);
        values.addProperty("enabled", "#55FF55");
        values.addProperty("stopped", "#FF5555");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, JSON.toJson(values) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("[HM Auto-trader] Could not create default colors file " + path + ": " + ex);
        }
    }
}
