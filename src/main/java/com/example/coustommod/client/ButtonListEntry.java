package com.example.coustommod.client;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ButtonListEntry extends TooltipListEntry<Object> {
    private final Button button;
    private final boolean centered;
    private boolean edited = false;

    public ButtonListEntry(Component label, Component buttonText, Consumer<Button> onPress) {
        super(label != null ? label : Component.empty(), () -> Optional.empty());
        this.centered = label == null || label.getString().trim().isEmpty();
        this.button = Button.builder(buttonText, onPress::accept)
                .bounds(0, 0, centered ? 220 : 110, 20)
                .build();
        this.button.setTooltip(null);
    }

    public ButtonListEntry setAlwaysEdited(boolean edited) {
        this.edited = edited;
        return this;
    }

    public Button getButton() {
        return button;
    }

    @Override
    public boolean isEdited() {
        return edited;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
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
        if (centered) {
            int w = Math.min(220, entryWidth - 20);
            button.setX(x + (entryWidth - w) / 2);
            button.setY(y + 2);
            button.setWidth(w);
        } else {
            Component fieldName = getDisplayedFieldName();
            if (fieldName != null && !fieldName.getString().isEmpty()) {
                extractor.text(Minecraft.getInstance().font, fieldName, x, y + 6, getPreferredTextColor());
            }
            int btnW = 110;
            button.setX(x + entryWidth - btnW);
            button.setY(y + 2);
            button.setWidth(btnW);
        }
        button.extractRenderState(extractor, mouseX, mouseY, delta);
    }
}
