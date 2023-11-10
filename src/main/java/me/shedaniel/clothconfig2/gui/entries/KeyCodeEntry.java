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
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
@Environment(EnvType.CLIENT)
public class KeyCodeEntry extends TooltipListEntry<ModifierKeyCode> {
    private ModifierKeyCode value;
    private final ModifierKeyCode original;
    private final ButtonWidget buttonWidget;
    private final ButtonWidget resetButton;
    private final Supplier<ModifierKeyCode> defaultValue;
    private final List<ClickableWidget> widgets;
    private boolean allowMouse = true, allowKey = true, allowModifiers = true;
    
    @ApiStatus.Internal
    @Deprecated
    public KeyCodeEntry(Text fieldName, ModifierKeyCode value, Text resetButtonKey, Supplier<ModifierKeyCode> defaultValue, Consumer<ModifierKeyCode> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.defaultValue = defaultValue;
        this.value = value.copy();
        this.original = value.copy();
        this.buttonWidget = ButtonWidget.builder(Text.empty(), widget -> {
            getConfigScreen().setFocusedBinding(this);
        }).dimensions(0, 0, 150, 20).build();
        this.resetButton = ButtonWidget.builder(resetButtonKey, widget -> {
            KeyCodeEntry.this.value = getDefaultValue().orElse(null).copy();
            getConfigScreen().setFocusedBinding(null);
        }).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20).build();
        this.saveCallback = saveConsumer;
        this.widgets = Lists.newArrayList(buttonWidget, resetButton);
    }
    
    @Override
    public boolean isEdited() {
        return super.isEdited() || !this.original.equals(getValue());
    }
    
    public boolean isAllowModifiers() {
        return allowModifiers;
    }
    
    public void setAllowModifiers(boolean allowModifiers) {
        this.allowModifiers = allowModifiers;
    }
    
    public boolean isAllowKey() {
        return allowKey;
    }
    
    public void setAllowKey(boolean allowKey) {
        this.allowKey = allowKey;
    }
    
    public boolean isAllowMouse() {
        return allowMouse;
    }
    
    public void setAllowMouse(boolean allowMouse) {
        this.allowMouse = allowMouse;
    }
    
    @Override
    public ModifierKeyCode getValue() {
        return value;
    }
    
    public void setValue(ModifierKeyCode value) {
        this.value = value;
    }
    
    @Override
    public Optional<ModifierKeyCode> getDefaultValue() {
        return Optional.ofNullable(defaultValue).map(Supplier::get).map(ModifierKeyCode::copy);
    }
    
    private Text getLocalizedName() {
        return this.value.getLocalizedName();
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.resetButton.active = isEditable() && getDefaultValue().isPresent() && !getDefaultValue().get().equals(getValue());
        this.resetButton.setY(y);
        this.buttonWidget.active = isEditable();
        this.buttonWidget.setY(y);
        this.buttonWidget.setMessage(getLocalizedName());
        if (getConfigScreen().getFocusedBinding() == this)
            this.buttonWidget.setMessage(Text.literal("> ").formatted(Formatting.WHITE).append(this.buttonWidget.getMessage().copyContentOnly().formatted(Formatting.YELLOW)).append(Text.literal(" <").formatted(Formatting.WHITE)));
        Text displayedFieldName = getDisplayedFieldName();
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayedFieldName.asOrderedText(), window.getScaledWidth() - x - MinecraftClient.getInstance().textRenderer.getWidth(displayedFieldName), y + 6, 16777215);
            this.resetButton.setX(x);
            this.buttonWidget.setX(x + resetButton.getWidth() + 2);
        } else {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayedFieldName.asOrderedText(), x, y + 6, getPreferredTextColor());
            this.resetButton.setX(x + entryWidth - resetButton.getWidth());
            this.buttonWidget.setX(x + entryWidth - 150);
        }
        this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2);
        resetButton.render(graphics, mouseX, mouseY, delta);
        buttonWidget.render(graphics, mouseX, mouseY, delta);
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
