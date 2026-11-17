package fluffical.radiusinteract;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().create();
    private static final File configFile = FabricLoader.getInstance().getConfigDir().resolve("fradiusinteract.json").toFile();

    public static void saveConfig(Config config) {
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public static Config loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Config config = GSON.fromJson(reader, Config.class);
                if (config != null)
                    return config;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        Config config = new Config();
        saveConfig(config);
        return config;
    }
}
