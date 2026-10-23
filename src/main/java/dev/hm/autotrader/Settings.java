package dev.hm.autotrader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/** Pure Java settings store; no Cloth Config, MaLiLib, or Mod Menu dependency. */
public final class Settings {
    public boolean enabled = false;
    public String profession = "minecraft:cleric";
    public double range = 4.5;
    public int clickDelayTicks = 4;
    public int villagerDelayTicks = 20;
    public int roundDelayTicks = 200;
    public boolean closeAfterVillager = true;
    public boolean stopIfFull = true;

    private Path path() { return FabricLoader.getInstance().getConfigDir().resolve("hm-autotrader.properties"); }

    public void load() {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path())) { props.load(in); }
        catch (IOException ignored) { return; }
        profession = props.getProperty("profession", profession);
        try { range = Double.parseDouble(props.getProperty("range", "4.5")); } catch (NumberFormatException ignored) {}
        try { clickDelayTicks = Integer.parseInt(props.getProperty("clickDelayTicks", "4")); } catch (NumberFormatException ignored) {}
        try { villagerDelayTicks = Integer.parseInt(props.getProperty("villagerDelayTicks", "20")); } catch (NumberFormatException ignored) {}
        try { roundDelayTicks = Integer.parseInt(props.getProperty("roundDelayTicks", "200")); } catch (NumberFormatException ignored) {}
        closeAfterVillager = Boolean.parseBoolean(props.getProperty("closeAfterVillager", "true"));
        stopIfFull = Boolean.parseBoolean(props.getProperty("stopIfFull", "true"));
        range = Math.max(2, Math.min(6, range));
        clickDelayTicks = Math.max(2, Math.min(20, clickDelayTicks));
        villagerDelayTicks = Math.max(5, Math.min(200, villagerDelayTicks));
        roundDelayTicks = Math.max(40, Math.min(2400, roundDelayTicks));
        enabled = false; // Never unexpectedly restart automation on launch.
    }
    public void save() {
        Properties p = new Properties();
        p.setProperty("profession", profession);
        p.setProperty("range", Double.toString(range));
        p.setProperty("clickDelayTicks", Integer.toString(clickDelayTicks));
        p.setProperty("villagerDelayTicks", Integer.toString(villagerDelayTicks));
        p.setProperty("roundDelayTicks", Integer.toString(roundDelayTicks));
        p.setProperty("closeAfterVillager", Boolean.toString(closeAfterVillager));
        p.setProperty("stopIfFull", Boolean.toString(stopIfFull));
        try {
            Files.createDirectories(path().getParent());
            try (OutputStream out = Files.newOutputStream(path())) { p.store(out, "HM Auto-trader (26.2)"); }
        } catch (IOException ex) { System.err.println("[HM Auto-trader] Can't save settings: " + ex); }
    }
}
