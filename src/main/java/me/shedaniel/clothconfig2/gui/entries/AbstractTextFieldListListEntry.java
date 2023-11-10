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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class represents config entry lists that use one {@link TextFieldWidget} per entry.
 *
 * @param <T>    the configuration object type
 * @param <C>    the cell type
 * @param <SELF> the "curiously recurring template pattern" type parameter
 * @see AbstractListListEntry
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractTextFieldListListEntry<T, C extends AbstractTextFieldListListEntry.AbstractTextFieldListCell<T, C, SELF>, SELF extends AbstractTextFieldListListEntry<T, C, SELF>> extends AbstractListListEntry<T, C, SELF> {
    
    @ApiStatus.Internal
    public AbstractTextFieldListListEntry(Text fieldName, List<T> value, boolean defaultExpanded, Supplier<Optional<Text[]>> tooltipSupplier, Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, SELF, C> createNewCell) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, createNewCell);
    }
    
    /**
     * @param <T>           the configuration object type
     * @param <SELF>        the "curiously recurring template pattern" type parameter for this class
     * @param <OUTER_SELF>> the "curiously recurring template pattern" type parameter for the outer class
     * @see AbstractTextFieldListListEntry
     */
    @ApiStatus.Internal
    public static abstract class AbstractTextFieldListCell<T, SELF extends AbstractTextFieldListCell<T, SELF, OUTER_SELF>, OUTER_SELF extends AbstractTextFieldListListEntry<T, SELF, OUTER_SELF>> extends AbstractListCell<T, SELF, OUTER_SELF> {
        
        protected TextFieldWidget widget;
        private boolean isSelected;
        private boolean isHovered;
        
        public AbstractTextFieldListCell(@Nullable T value, OUTER_SELF listListEntry) {
            super(value, listListEntry);
            
            final T finalValue = substituteDefault(value);
            
            widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 100, 18, Text.empty()) {
                @Override
                public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
                    setFocused(isSelected);
                    super.render(graphics, mouseX, mouseY, delta);
                }
            };
            widget.setTextPredicate(this::isValidText);
            widget.setMaxLength(Integer.MAX_VALUE);
            widget.setDrawsBackground(false);
            widget.setText(Objects.toString(finalValue));
            widget.setCursorToStart(false);
            widget.setChangedListener(s -> {
                widget.setEditableColor(getPreferredTextColor());
            });
        }
        
        @Override
        public void updateSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
        
        /**
         * Allows subclasses to substitute default values.
         *
         * @param value the (possibly null) value to substitute
         * @return a substitution
         */
        @Nullable
        protected abstract T substituteDefault(@Nullable T value);
        
        /**
         * Tests if the text entered is valid. If not, the text is not changed.
         *
         * @param text the text to test
         * @return {@code true} if the text may be changed, {@code false} to prevent the change
         */
        protected abstract boolean isValidText(@NotNull String text);
        
        @Override
        public int getCellHeight() {
            return 20;
        }
        
        @Override
        public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            widget.setWidth(entryWidth - 12);
            widget.setX(x);
            widget.setY(y + 1);
            widget.setEditable(listListEntry.isEditable());
            widget.render(graphics, mouseX, mouseY, delta);
            isHovered = widget.isMouseOver(mouseX, mouseY);
            if (isSelected && listListEntry.isEditable())
                graphics.fill(x, y + 12, x + entryWidth - 12, y + 13, getConfigError().isPresent() ? 0xffff5555 : 0xffe0e0e0);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(widget);
        }
    
        @Override
        public SelectionType getType() {
            return isSelected ? SelectionType.FOCUSED : isHovered ? SelectionType.HOVERED : SelectionType.NONE;
        }
    
        @Override
        public void appendNarrations(NarrationMessageBuilder narrationElementOutput) {
            widget.appendNarrations(narrationElementOutput);
        }
    }
    
}
