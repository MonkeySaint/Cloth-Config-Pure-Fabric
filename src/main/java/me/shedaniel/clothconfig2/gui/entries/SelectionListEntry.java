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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SelectionListEntry<T> extends TooltipListEntry<T> {
    
    private final ImmutableList<T> values;
    private final AtomicInteger index;
    private final int original;
    private final ButtonWidget buttonWidget;
    private final ButtonWidget resetButton;
    private final Supplier<T> defaultValue;
    private final List<ClickableWidget> widgets;
    private final Function<T, Text> nameProvider;
    
    @ApiStatus.Internal
    @Deprecated
    public SelectionListEntry(Text fieldName, T[] valuesArray, T value, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer) {
        this(fieldName, valuesArray, value, resetButtonKey, defaultValue, saveConsumer, null);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public SelectionListEntry(Text fieldName, T[] valuesArray, T value, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<T, Text> nameProvider) {
        this(fieldName, valuesArray, value, resetButtonKey, defaultValue, saveConsumer, nameProvider, null);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public SelectionListEntry(Text fieldName, T[] valuesArray, T value, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<T, Text> nameProvider, Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, valuesArray, value, resetButtonKey, defaultValue, saveConsumer, nameProvider, tooltipSupplier, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public SelectionListEntry(Text fieldName, T[] valuesArray, T value, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<T, Text> nameProvider, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        if (valuesArray != null)
            this.values = ImmutableList.copyOf(valuesArray);
        else
            this.values = ImmutableList.of(value);
        this.defaultValue = defaultValue;
        this.index = new AtomicInteger(this.values.indexOf(value));
        this.index.compareAndSet(-1, 0);
        this.original = this.values.indexOf(value);
        this.buttonWidget = ButtonWidget.builder(Text.empty(), widget -> {
            SelectionListEntry.this.index.incrementAndGet();
            SelectionListEntry.this.index.compareAndSet(SelectionListEntry.this.values.size(), 0);
        }).dimensions(0, 0, 150, 20).build();
        this.resetButton = ButtonWidget.builder(resetButtonKey, widget -> {
            SelectionListEntry.this.index.set(getDefaultIndex());
        }).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20).build();
        this.saveCallback = saveConsumer;
        this.widgets = Lists.newArrayList(buttonWidget, resetButton);
        this.nameProvider = nameProvider == null ? (t -> Text.translatable(t instanceof Translatable ? ((Translatable) t).getKey() : t.toString())) : nameProvider;
    }
    
    @Override
    public boolean isEdited() {
        return super.isEdited() || !Objects.equals(this.index.get(), this.original);
    }
    
    @Override
    public T getValue() {
        return this.values.get(this.index.get());
    }
    
    @Override
    public Optional<T> getDefaultValue() {
        return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.resetButton.active = isEditable() && getDefaultValue().isPresent() && getDefaultIndex() != this.index.get();
        this.resetButton.setY(y);
        this.buttonWidget.active = isEditable();
        this.buttonWidget.setY(y);
        this.buttonWidget.setMessage(nameProvider.apply(getValue()));
        Text displayedFieldName = getDisplayedFieldName();
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayedFieldName.asOrderedText(), window.getScaledWidth() - x - MinecraftClient.getInstance().textRenderer.getWidth(displayedFieldName), y + 6, getPreferredTextColor());
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
    
    private int getDefaultIndex() {
        return Math.max(0, this.values.indexOf(this.defaultValue.get()));
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public List<? extends Selectable> narratables() {
        return widgets;
    }
    
    public interface Translatable {
        @NotNull String getKey();
    }
    
}
