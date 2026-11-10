package com.example.coustommod.client;

import com.example.coustommod.config.CommandKeybindConfig;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public class InlineKeybindEntry extends TooltipListEntry<String> {
    private final Button button;
    private String combo;
    private boolean isRecording = false;
    private final Set<Integer> pressedKeys = new LinkedHashSet<>();
    private final int slotIndex;
    private final List<CommandKeybindConfig.BindingEntry> allBindings;
    private final Consumer<String> onSave;
    private final Runnable onComboChanged;

    public InlineKeybindEntry(Component label, String initialCombo, int slotIndex,
                              List<CommandKeybindConfig.BindingEntry> allBindings,
                              Consumer<String> onSave, Runnable onComboChanged) {
        super(label != null ? label : Component.literal("Keybind"), () -> Optional.empty());
        this.combo = (initialCombo != null && !initialCombo.isBlank()) ? initialCombo : "None";
        this.slotIndex = slotIndex;
        this.allBindings = allBindings;
        this.onSave = onSave;
        this.onComboChanged = onComboChanged;

        this.button = Button.builder(Component.empty(), btn -> startRecording())
                .bounds(0, 0, 140, 20)
                .build();
        this.button.setTooltip(null);

        updateButtonDisplay();
    }

    private void startRecording() {
        this.isRecording = true;
        this.pressedKeys.clear();
        this.button.setMessage(Component.literal("§e> Press keys... <"));
    }

    public void updateButtonDisplay() {
        String conflict = KeyCombo.getConflict(combo, slotIndex, allBindings);
        if (conflict != null) {
            this.button.setMessage(Component.literal("§e[ ⚠ " + combo + " ]"));
        } else {
            this.button.setMessage(Component.literal("§b[ " + combo + " ]"));
        }
    }

    @Override
    public Optional<Component[]> getTooltip() {
        return Optional.empty();
    }

    @Override
    public Optional<Component[]> getTooltip(int mouseX, int mouseY) {
        return Optional.empty();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!isRecording) {
            return super.keyPressed(event);
        }

        int key = event.key();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            isRecording = false;
            pressedKeys.clear();
            combo = "None";
            updateButtonDisplay();
            if (onSave != null) onSave.accept(combo);
            if (onComboChanged != null) onComboChanged.run();
            return true;
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            finishRecording();
            return true;
        }

        pressedKeys.add(key);

        List<String> names = new ArrayList<>();
        for (int k : pressedKeys) {
            String n = KeyCombo.getKeyName(k);
            if (!names.contains(n)) names.add(n);
        }

        if (!names.isEmpty()) {
            button.setMessage(Component.literal("§e> " + String.join(" + ", names) + " <"));
        }

        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (!isRecording) {
            return super.keyReleased(event);
        }

        finishRecording();
        return true;
    }

    private void finishRecording() {
        if (!pressedKeys.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (int k : pressedKeys) {
                String n = KeyCombo.getKeyName(k);
                if (!names.contains(n)) names.add(n);
            }
            if (!names.isEmpty()) {
                this.combo = KeyCombo.format(String.join(" + ", names));
            }
        }
        pressedKeys.clear();
        isRecording = false;
        updateButtonDisplay();

        if (onSave != null) onSave.accept(combo);
        if (onComboChanged != null) onComboChanged.run();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean dragging) {
        if (isRecording && !button.isMouseOver(event.x(), event.y())) {
            isRecording = false;
            pressedKeys.clear();
            updateButtonDisplay();
        }
        return super.mouseClicked(event, dragging);
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public String getValue() {
        return combo;
    }

    @Override
    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(combo);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(button);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(button);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.extractRenderState(extractor, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

        Component fieldName = getDisplayedFieldName();
        if (fieldName != null && !fieldName.getString().isEmpty()) {
            extractor.text(Minecraft.getInstance().font, fieldName, x, y + 6, getPreferredTextColor());
        }

        int btnWidth = 140;
        button.setX(x + entryWidth - btnWidth);
        button.setY(y + 2);
        button.setWidth(btnWidth);
        button.extractRenderState(extractor, mouseX, mouseY, delta);
    }
}
