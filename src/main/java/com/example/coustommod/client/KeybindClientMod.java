package com.example.coustommod.client;

import com.example.coustommod.config.CommandKeybindConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;

public class KeybindClientMod implements ClientModInitializer {
    private static CommandKeybindConfig config;
    private static final Map<Integer, Boolean> keyState = new HashMap<>();

    @Override
    public void onInitializeClient() {
        reloadConfig();

        // Register /keybinds client command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("keybinds")
                .executes(context -> {
                    Minecraft client = Minecraft.getInstance();
                    client.execute(() -> {
                        if (client.gui != null) {
                            Screen current = client.gui.screen();
                            Screen parent = (current instanceof ChatScreen) ? null : current;
                            client.gui.setScreen(ConfigScreenFactory.createConfigScreen(parent));
                        }
                    });
                    return 1;
                }));
        });

        // Register client tick to check for shortcut key presses
        ClientTickEvents.END_CLIENT_TICK.register(KeybindClientMod::onTick);
    }

    public static void reloadConfig() {
        config = CommandKeybindConfig.load();
        keyState.clear();
    }

    private static void onTick(Minecraft client) {
        if (client.player == null || client.getConnection() == null || client.gui == null || client.gui.screen() != null) {
            return;
        }

        if (config == null || config.bindings == null) {
            return;
        }

        for (int i = 0; i < config.bindings.size(); i++) {
            CommandKeybindConfig.BindingEntry entry = config.bindings.get(i);

            if (!entry.enabled || entry.command == null || entry.command.isBlank()
                    || entry.keyCombo == null || entry.keyCombo.isBlank() || entry.keyCombo.equalsIgnoreCase("None")) {
                keyState.put(i, false);
                continue;
            }

            boolean isPressed = KeyCombo.isComboPressed(entry.keyCombo);
            boolean wasPressed = keyState.getOrDefault(i, false);
            keyState.put(i, isPressed);

            if (isPressed && !wasPressed) {
                String cmd = entry.command.trim();
                if (cmd.startsWith("/")) {
                    client.getConnection().sendCommand(cmd.substring(1));
                } else {
                    client.getConnection().sendChat(cmd);
                }
            }
        }
    }
}
