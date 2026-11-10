package com.example.coustommod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommandKeybindConfig {
    public static class BindingEntry {
        public String name = "Shortcut";
        public String keyCombo = "CTRL + O";
        public String keyName = "key.keyboard.o";
        public boolean ctrl = true;
        public boolean alt = false;
        public boolean shift = false;
        public String command = "/orders";
        public boolean enabled = true;

        public BindingEntry() {}

        public BindingEntry(String name, String keyCombo, String command, boolean enabled) {
            this.name = name;
            this.keyCombo = keyCombo;
            this.command = command;
            this.enabled = enabled;
        }

        public String getReadableKey() {
            if (keyCombo != null && !keyCombo.isBlank()) {
                return keyCombo;
            }
            StringBuilder sb = new StringBuilder();
            if (ctrl) sb.append("CTRL + ");
            if (alt) sb.append("ALT + ");
            if (shift) sb.append("SHIFT + ");
            if (keyName != null && !keyName.isEmpty() && !keyName.equals("key.keyboard.unknown")) {
                String k = keyName.replace("key.keyboard.", "").replace("key.mouse.", "MOUSE_").toUpperCase();
                sb.append(k);
            }
            return sb.toString();
        }
    }

    public List<BindingEntry> bindings = new ArrayList<>();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("slashkey.json");
    private static final Path OLD_CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("coustomkeybind.json");
    private static final Path OLDEST_CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("coustom-mod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static CommandKeybindConfig createDefaultConfig() {
        CommandKeybindConfig cfg = new CommandKeybindConfig();
        // 3 default example shortcuts: /orders, /shop, /home
        cfg.bindings.add(new BindingEntry("Orders", "CTRL + O", "/orders", true));
        cfg.bindings.add(new BindingEntry("Shop", "SHIFT + S", "/shop", true));
        cfg.bindings.add(new BindingEntry("Home", "ALT + H", "/home", true));
        cfg.save();
        return cfg;
    }

    public static CommandKeybindConfig load() {
        Path targetPath = Files.exists(CONFIG_PATH) ? CONFIG_PATH : (Files.exists(OLD_CONFIG_PATH) ? OLD_CONFIG_PATH : (Files.exists(OLDEST_CONFIG_PATH) ? OLDEST_CONFIG_PATH : null));
        if (targetPath != null) {
            try {
                String json = Files.readString(targetPath);
                CommandKeybindConfig cfg = GSON.fromJson(json, CommandKeybindConfig.class);
                if (cfg != null && cfg.bindings != null) {
                    for (int i = 0; i < cfg.bindings.size(); i++) {
                        BindingEntry b = cfg.bindings.get(i);
                        if (b.name == null || b.name.isBlank()) {
                            b.name = "Shortcut #" + (i + 1);
                        }
                        if (b.keyCombo == null || b.keyCombo.isBlank()) {
                            b.keyCombo = b.getReadableKey();
                        }
                        if (b.keyCombo == null || b.keyCombo.isBlank()) {
                            b.keyCombo = "None";
                        }
                    }
                    return cfg;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return createDefaultConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
