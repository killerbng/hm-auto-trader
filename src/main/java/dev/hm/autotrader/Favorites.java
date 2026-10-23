package dev.hm.autotrader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

/** Global trades, stored in user-readable JSON. Uses Minecraft's bundled Gson. */
public final class Favorites {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Set<String> keys = new LinkedHashSet<>();

    private Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("hm-autotrader-favorites.json");
    }
    private Path legacyPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("hm-autotrader-favorites.txt");
    }

    public static String itemId(ItemStack stack) {
        return stack.isEmpty() ? "-" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
    public static String key(MerchantOffer offer) {
        return itemId(offer.getBaseCostA()) + "|" + itemId(offer.getCostB()) + "|" + itemId(offer.getResult());
    }

    /** JSON is authoritative once it exists. First launch imports the old .txt automatically. */
    public void load() {
        keys.clear();
        Path file = path();
        if (Files.exists(file)) {
            try {
                JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                if (!root.isJsonArray()) throw new IllegalArgumentException("Expected an array of trade objects");
                for (JsonElement element : root.getAsJsonArray()) {
                    if (!element.isJsonObject()) continue;
                    JsonObject row = element.getAsJsonObject();
                    String sell = stringField(row, "sell", "");
                    String buy = stringField(row, "buy", "");
                    String extra = stringField(row, "sell_extra", "-");
                    if (!sell.isBlank() && !buy.isBlank()) {
                        keys.add(sell + "|" + (extra.isBlank() ? "-" : extra) + "|" + buy);
                    }
                }
            } catch (IOException | RuntimeException ex) {
                System.err.println("[HM Auto-trader] Can't read favorites JSON; check " + file + ": " + ex);
                // Don't silently regenerate a damaged user-edited file.
            }
            return;
        }
        Path old = legacyPath();
        if (!Files.isRegularFile(old)) return;
        try {
            for (String line : Files.readAllLines(old, StandardCharsets.UTF_8)) {
                String text = line.trim();
                if (text.isEmpty() || text.startsWith("#")) continue;
                String[] parts = text.split("\\|", -1);
                if (parts.length == 3 && !parts[0].isBlank() && !parts[2].isBlank()) keys.add(text);
            }
            // Preserve the legacy .txt as an untouched backup.
            save();
            System.out.println("[HM Auto-trader] Imported " + keys.size() + " favorites into " + file);
        } catch (IOException ex) {
            System.err.println("[HM Auto-trader] Can't import legacy favorites: " + ex);
        }
    }

    private static String stringField(JsonObject row, String field, String fallback) {
        JsonElement value = row.get(field);
        return value == null || value.isJsonNull() ? fallback : value.getAsString().trim();
    }

    public void save() {
        JsonArray list = new JsonArray();
        for (String key : keys) {
            String[] parts = key.split("\\|", -1);
            if (parts.length != 3) continue;
            JsonObject row = new JsonObject();
            row.addProperty("sell", parts[0]);
            if (!"-".equals(parts[1])) row.addProperty("sell_extra", parts[1]);
            row.addProperty("buy", parts[2]);
            list.add(row);
        }
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), JSON.toJson(list) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException ex) { System.err.println("[HM Auto-trader] Can't save favorites: " + ex); }
    }

    public boolean contains(MerchantOffer offer) { return keys.contains(key(offer)); }
    public boolean toggle(MerchantOffer offer) {
        String key = key(offer);
        if (keys.remove(key)) { save(); return false; }
        keys.add(key); save(); return true;
    }
    public List<String> entries() { return new ArrayList<>(keys); }
    public int size() { return keys.size(); }
    public void remove(String key) { keys.remove(key); save(); }
    public void clear() { keys.clear(); save(); }
}
