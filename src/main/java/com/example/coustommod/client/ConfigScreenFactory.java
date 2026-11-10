package com.example.coustommod.client;

import com.example.coustommod.config.CommandKeybindConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreenFactory {

    public static Screen createConfigScreen(Screen parent) {
        return createConfigScreen(parent, -1);
    }

    public static Screen createConfigScreen(Screen parent, int expandedIndex) {
        CommandKeybindConfig config = CommandKeybindConfig.load();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("SlashKey Configuration"))
                .setDoesConfirmSave(false);

        ConfigCategory category = builder.getOrCreateCategory(Component.literal("Shortcuts"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Clean header description
        category.addEntry(entryBuilder.startTextDescription(
                Component.literal("§7Click any shortcut to edit. Click a keybind button to rebind."))
                .build());

        // Quick "+ Create New Shortcut" button at the top of the list
        category.addEntry(new ButtonListEntry(
                Component.empty(),
                Component.literal("§a＋ Create New Shortcut"),
                btn -> {
                    flushCurrentScreen();
                    int newIndex = config.bindings.size();
                    config.bindings.add(new CommandKeybindConfig.BindingEntry(
                            "New Shortcut #" + (newIndex + 1),
                            "None",
                            "/command",
                            true
                    ));
                    config.save();
                    KeybindClientMod.reloadConfig();
                    openScreen(parent, newIndex);
                }
        ).setAlwaysEdited(true));

        List<InlineKeybindEntry> allKeybindWidgets = new ArrayList<>();

        // Render each shortcut with neat inline fields
        for (int i = 0; i < config.bindings.size(); i++) {
            CommandKeybindConfig.BindingEntry entry = config.bindings.get(i);
            final int slotIndex = i;
            String keyDisplay = (entry.keyCombo != null && !entry.keyCombo.isBlank()) ? entry.keyCombo : "None";
            String nameDisplay = (entry.name != null && !entry.name.isBlank()) ? entry.name : "Shortcut #" + (slotIndex + 1);
            String cmdDisplay = (entry.command != null && !entry.command.isBlank()) ? entry.command : "/command";

            // Check if another shortcut in SlashKey uses this exact keybind
            String conflict = KeyCombo.getConflict(entry.keyCombo, slotIndex, config.bindings);

            // Neatly formatted header: Name (command) — [Keybind]
            String keyTag;
            if (conflict != null) {
                keyTag = "§e[⚠ " + keyDisplay + "]";
            } else if (keyDisplay.equalsIgnoreCase("None")) {
                keyTag = "§7[None]";
            } else {
                keyTag = "§b[" + keyDisplay + "]";
            }

            Component title;
            if (entry.enabled) {
                title = Component.literal("§f" + nameDisplay + " §7(" + cmdDisplay + ") §8— " + keyTag);
            } else {
                title = Component.literal("§7" + nameDisplay + " §8(" + cmdDisplay + ") §8— §8[Disabled]");
            }

            SubCategoryBuilder sub = entryBuilder.startSubCategory(title);
            // Expand only if explicitly requested (e.g. newly created item), otherwise keep collapsed
            sub.setExpanded(slotIndex == expandedIndex);

            // 1. Shortcut Name field - instant single-click focus and typing
            sub.add(new InlineTextEntry(
                    Component.literal("Shortcut Name"),
                    entry.name,
                    entry.name,
                    newName -> {
                        entry.name = newName.trim();
                    },
                    liveName -> {
                        entry.name = liveName.trim();
                    }
            ));

            // 2. Normal inline Keybind button (click to rebind, live key display, yellow if duplicate)
            InlineKeybindEntry keybindWidget = new InlineKeybindEntry(
                    Component.literal("Keybind"),
                    entry.keyCombo,
                    slotIndex,
                    config.bindings,
                    newCombo -> {
                        entry.keyCombo = newCombo;
                        config.save();
                        KeybindClientMod.reloadConfig();
                    },
                    () -> {
                        // Smoothly update all keybind buttons' conflict colors without destroying the screen
                        for (InlineKeybindEntry w : allKeybindWidgets) {
                            w.updateButtonDisplay();
                        }
                    }
            );
            allKeybindWidgets.add(keybindWidget);
            sub.add(keybindWidget);

            // 3. Command field - instant single-click focus and typing
            sub.add(new InlineTextEntry(
                    Component.literal("Command to Run"),
                    entry.command,
                    entry.command,
                    newCmd -> {
                        entry.command = newCmd.trim();
                    },
                    liveCmd -> {
                        entry.command = liveCmd.trim();
                    }
            ));

            // 4. Enabled toggle
            sub.add(entryBuilder.startBooleanToggle(Component.literal("Enabled"), entry.enabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> entry.enabled = val)
                    .build());

            // 5. Instant Delete button
            sub.add(new ButtonListEntry(
                    Component.literal("Remove Shortcut"),
                    Component.literal("§c✕ Delete"),
                    btn -> {
                        flushCurrentScreen();
                        if (slotIndex >= 0 && slotIndex < config.bindings.size()) {
                            config.bindings.remove(slotIndex);
                        }
                        config.save();
                        KeybindClientMod.reloadConfig();
                        openScreen(parent, -1);
                    }
            ));

            category.addEntry(sub.build());
        }

        // Saving runnable when user clicks Cloth Config's bottom "Save & Done"
        builder.setSavingRunnable(() -> {
            config.save();
            KeybindClientMod.reloadConfig();
        });

        return builder.build();
    }

    private static void flushCurrentScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.gui != null && client.gui.screen() instanceof AbstractConfigScreen acs) {
            acs.saveAll(false);
        }
    }

    private static void openScreen(Screen parent, int expandedIndex) {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.execute(() -> {
                if (client.gui != null) {
                    client.gui.setScreen(createConfigScreen(parent, expandedIndex));
                }
            });
        }
    }
}
