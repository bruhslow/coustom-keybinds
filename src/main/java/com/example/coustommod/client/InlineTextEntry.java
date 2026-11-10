package com.example.coustommod.client;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class InlineTextEntry extends TooltipListEntry<String> {
    private final EditBox editBox;
    private final Button resetButton;
    private final String originalValue;
    private final Consumer<String> onSave;
    private final Consumer<String> onChange;

    public InlineTextEntry(Component label, String initialValue, String defaultValue,
                           Consumer<String> onSave, Consumer<String> onChange) {
        super(label != null ? label : Component.empty(), () -> Optional.empty());
        this.originalValue = (initialValue != null) ? initialValue : "";
        this.onSave = onSave;
        this.onChange = onChange;

        Font font = Minecraft.getInstance().font;
        this.editBox = new EditBox(font, 0, 0, 150, 18, label != null ? label : Component.empty());
        this.editBox.setMaxLength(256);
        this.editBox.setValue(this.originalValue);
        this.editBox.setCanLoseFocus(true);
        this.editBox.setResponder(val -> {
            if (this.onChange != null) {
                this.onChange.accept(val);
            }
        });

        this.resetButton = Button.builder(Component.literal("↺"), btn -> {
            this.editBox.setValue(defaultValue != null ? defaultValue : "");
            this.editBox.setFocused(true);
            this.setFocused(this.editBox);
            if (this.onChange != null) {
                this.onChange.accept(this.editBox.getValue());
            }
        }).bounds(0, 0, 20, 18).build();
        this.resetButton.setTooltip(null);
    }

    public EditBox getEditBox() {
        return editBox;
    }

    @Override
    public boolean isEdited() {
        return !originalValue.equals(editBox.getValue());
    }

    @Override
    public String getValue() {
        return editBox.getValue();
    }

    @Override
    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(originalValue);
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
    public void save() {
        if (onSave != null) {
            onSave.accept(editBox.getValue());
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && editBox.isFocused()) {
            editBox.setFocused(false);
            this.setFocused(null);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean dragging) {
        if (resetButton.mouseClicked(event, dragging)) {
            return true;
        }

        // If clicked inside the text box itself
        if (editBox.isMouseOver(event.x(), event.y())) {
            editBox.mouseClicked(event, dragging);
            editBox.setFocused(true);
            this.setFocused(editBox);
            return true;
        }

        // If clicked anywhere on this entry's row (e.g. on the label or blank space)
        if (this.isMouseOver(event.x(), event.y())) {
            editBox.setFocused(true);
            this.setFocused(editBox);
            editBox.moveCursorToEnd(false);
            return true;
        }

        // Clicked outside this entry
        if (editBox.isFocused()) {
            editBox.setFocused(false);
            this.setFocused(null);
        }
        return super.mouseClicked(event, dragging);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                editBox.setFocused(false);
                this.setFocused(null);
                return true;
            }
            if (editBox.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editBox.isFocused()) {
            if (editBox.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (editBox.isFocused()) {
            if (editBox.mouseDragged(event, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(editBox, resetButton);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of(editBox, resetButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int index, int y, int x,
                                   int entryWidth, int entryHeight, int mouseX, int mouseY,
                                   boolean isHovered, float delta) {
        super.extractRenderState(extractor, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

        Component fieldName = getDisplayedFieldName();
        if (fieldName != null && !fieldName.getString().isEmpty()) {
            extractor.text(Minecraft.getInstance().font, fieldName, x, y + 5, getPreferredTextColor());
        }

        int resetWidth = 20;
        int boxWidth = Math.min(180, entryWidth - 140);
        int totalWidgetsWidth = boxWidth + 4 + resetWidth;
        int right = x + entryWidth;

        editBox.setX(right - totalWidgetsWidth);
        editBox.setY(y + 1);
        editBox.setWidth(boxWidth);
        editBox.extractRenderState(extractor, mouseX, mouseY, delta);

        resetButton.setX(right - resetWidth);
        resetButton.setY(y + 1);
        resetButton.setWidth(resetWidth);
        resetButton.active = !editBox.getValue().equals(originalValue);
        resetButton.extractRenderState(extractor, mouseX, mouseY, delta);
    }
}
