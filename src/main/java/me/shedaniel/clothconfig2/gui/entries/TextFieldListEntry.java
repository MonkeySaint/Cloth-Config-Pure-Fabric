/*
 * This file is part of Cloth Config.
 * Copyright (C) 2020 - 2021 shedaniel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package me.shedaniel.clothconfig2.gui.entries;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class TextFieldListEntry<T> extends TooltipListEntry<T> {
    
    protected TextFieldWidget textFieldWidget;
    protected ButtonWidget resetButton;
    protected Supplier<T> defaultValue;
    protected T original;
    protected List<ClickableWidget> widgets;
    private boolean isSelected = false;
    
    @ApiStatus.Internal
    @Deprecated
    protected TextFieldListEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue) {
        this(fieldName, original, resetButtonKey, defaultValue, null);
    }
    
    @ApiStatus.Internal
    @Deprecated
    protected TextFieldListEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    protected TextFieldListEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.defaultValue = defaultValue;
        this.original = original;
        this.textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 148, 18, Text.empty()) {
            @Override
            public void render(DrawContext graphics, int int_1, int int_2, float float_1) {
                setFocused(isSelected && TextFieldListEntry.this.getFocused() == this);
                textFieldPreRender(this);
                super.render(graphics, int_1, int_2, float_1);
            }
            
            @Override
            public void write(String string_1) {
                super.write(stripAddText(string_1));
            }
        };
        textFieldWidget.setMaxLength(999999);
        textFieldWidget.setText(String.valueOf(original));
        this.resetButton = ButtonWidget.builder(resetButtonKey, widget -> {
            TextFieldListEntry.this.textFieldWidget.setText(String.valueOf(defaultValue.get()));
        }).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20).build();
        this.widgets = Lists.newArrayList(textFieldWidget, resetButton);
    }
    
    @Override
    public boolean isEdited() {
        return isChanged(original, textFieldWidget.getText());
    }
    
    protected boolean isChanged(T original, String s) {
        return !String.valueOf(original).equals(s);
    }
    
    protected static void setTextFieldWidth(TextFieldWidget widget, int width) {
        widget.setWidth(width);
    }
    
    @Deprecated
    public void setValue(String s) {
        textFieldWidget.setText(String.valueOf(s));
    }
    
    protected String stripAddText(String s) {
        return s;
    }
    
    protected void textFieldPreRender(TextFieldWidget widget) {
        widget.setEditableColor(getConfigError().isPresent() ? 0xff5555 : 0xe0e0e0);
    }
    
    @Override
    public void updateSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.resetButton.active = isEditable() && getDefaultValue().isPresent() && !isMatchDefault(textFieldWidget.getText());
        this.resetButton.setY(y);
        this.textFieldWidget.setEditable(isEditable());
        this.textFieldWidget.setY(y + 1);
        Text displayedFieldName = getDisplayedFieldName();
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayedFieldName.asOrderedText(), window.getScaledWidth() - x - MinecraftClient.getInstance().textRenderer.getWidth(displayedFieldName), y + 6, getPreferredTextColor());
            this.resetButton.setX(x);
            this.textFieldWidget.setX(x + resetButton.getWidth());
        } else {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayedFieldName.asOrderedText(), x, y + 6, getPreferredTextColor());
            this.resetButton.setX(x + entryWidth - resetButton.getWidth());
            this.textFieldWidget.setX(x + entryWidth - 148);
        }
        setTextFieldWidth(textFieldWidget, 148 - resetButton.getWidth() - 4);
        resetButton.render(graphics, mouseX, mouseY, delta);
        textFieldWidget.render(graphics, mouseX, mouseY, delta);
    }
    
    protected boolean isMatchDefault(String text) {
        Optional<T> defaultValue = getDefaultValue();
        return defaultValue.isPresent() && text.equals(defaultValue.get().toString());
    }
    
    @Override
    public Optional<T> getDefaultValue() {
        return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public List<? extends Selectable> narratables() {
        return widgets;
    }
}
