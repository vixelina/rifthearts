package sinily.rifthearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class RiftHeartsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("rifthearts.json");

    public String heartMode = "auto"; // "auto", "on", "off"

    public static RiftHeartsConfig load() {
        if (Files.exists(PATH)) {
            try (Reader r = Files.newBufferedReader(PATH)) {
                RiftHeartsConfig cfg = GSON.fromJson(r, RiftHeartsConfig.class);
                if (cfg != null) return cfg;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        RiftHeartsConfig cfg = new RiftHeartsConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}