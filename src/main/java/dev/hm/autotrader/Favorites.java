package dev.hm.autotrader;

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

/** Favorites are keyed by input/output item IDs, not by villager UUID or volatile trade index. */
public final class Favorites {
    private final Set<String> keys = new LinkedHashSet<>();
    private Path path() { return FabricLoader.getInstance().getConfigDir().resolve("hm-autotrader-favorites.txt"); }

    public static String itemId(ItemStack stack) {
        return stack.isEmpty() ? "-" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
    public static String key(MerchantOffer offer) {
        return itemId(offer.getBaseCostA()) + "|" + itemId(offer.getCostB()) + "|" + itemId(offer.getResult());
    }
    public void load() {
        keys.clear();
        try {
            for (String line : Files.readAllLines(path(), StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (!t.isEmpty() && !t.startsWith("#") && t.split("\\|", -1).length == 3) keys.add(t);
            }
        } catch (IOException ignored) { /* first run */ }
    }
    public void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.write(path(), keys, StandardCharsets.UTF_8);
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
