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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.Expandable;
import me.shedaniel.math.Rectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class MultiElementListEntry<T> extends TooltipListEntry<T> implements Expandable {
    
    private static final Identifier CONFIG_TEX = new Identifier("cloth-config2", "textures/gui/cloth_config.png");
    private final T object;
    private final List<AbstractConfigListEntry<?>> entries;
    private final CategoryLabelWidget widget;
    private final List<Object> children; // GuiEventListener & NarratableEntry
    private boolean expanded;
    
    @ApiStatus.Internal
    public MultiElementListEntry(Text categoryName, T object, List<AbstractConfigListEntry<?>> entries, boolean defaultExpanded) {
        super(categoryName, null);
        this.object = object;
        this.entries = entries;
        this.expanded = defaultExpanded;
        this.widget = new CategoryLabelWidget();
        this.children = Lists.newArrayList(widget);
        this.children.addAll(entries);
        this.setReferenceProviderEntries((List) entries);
    }
    
    @Override
    public boolean isRequiresRestart() {
        for (AbstractConfigListEntry<?> entry : entries)
            if (entry.isRequiresRestart())
                return true;
        return false;
    }
    
    @Override
    public boolean isEdited() {
        for (AbstractConfigListEntry<?> entry : entries) {
            if (entry.isEdited()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterator<String> getSearchTags() {
        return Iterators.concat(super.getSearchTags(), Iterators.concat(entries.stream().map(AbstractConfigListEntry::getSearchTags).iterator()));
    }
    
    @Override
    public void setRequiresRestart(boolean requiresRestart) {
        
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    public Text getCategoryName() {
        return getFieldName();
    }
    
    @Override
    public T getValue() {
        return object;
    }
    
    @Override
    public Optional<T> getDefaultValue() {
        return Optional.empty();
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        boolean insideWidget = widget.rectangle.contains(mouseX, mouseY);
        RenderSystem.setShaderTexture(0, CONFIG_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.drawTexture(CONFIG_TEX, x - 15, y + 5, 24, (isEnabled() ? (insideWidget ? 18 : 0) : 36) + (isExpanded() ? 9 : 0), 9, 9);
        graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getDisplayedFieldName().asOrderedText(), x, y + 6, insideWidget ? 0xffe6fe16 : -1);
        for (AbstractConfigListEntry entry : entries) {
            entry.setParent(getParent());
            entry.setScreen(getConfigScreen());
        }
        if (isExpanded()) {
            int yy = y + 24;
            for (AbstractConfigListEntry<?> entry : entries) {
                entry.render(graphics, -1, yy, x + 14, entryWidth - 14, entry.getItemHeight(), mouseX, mouseY, isHovered, delta);
                yy += entry.getItemHeight();
                yy += Math.max(0, entry.getMorePossibleHeight());
            }
        }
    }
    
    @Override
    public Rectangle getEntryArea(int x, int y, int entryWidth, int entryHeight) {
        widget.rectangle.x = x - 15;
        widget.rectangle.y = y;
        widget.rectangle.width = entryWidth + 15;
        widget.rectangle.height = 24;
        return new Rectangle(getParent().left, y, getParent().right - getParent().left, 20);
    }
    
    @Override
    public int getItemHeight() {
        if (isExpanded()) {
            int i = 24;
            for (AbstractConfigListEntry<?> entry : entries)
                i += entry.getItemHeight();
            return i;
        }
        return 24;
    }
    
    @Override
    public void updateSelected(boolean isSelected) {
        for (AbstractConfigListEntry<?> entry : entries) {
            entry.updateSelected(isExpanded() && isSelected && getFocused() == entry);
        }
    }
    
    @Override
    public int getInitialReferenceOffset() {
        return 24;
    }
    
    @Override
    public void lateRender(DrawContext graphics, int mouseX, int mouseY, float delta) {
        if (isExpanded()) {
            for (AbstractConfigListEntry<?> entry : entries) {
                entry.lateRender(graphics, mouseX, mouseY, delta);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public int getMorePossibleHeight() {
        if (!isExpanded()) return -1;
        List<Integer> list = new ArrayList<>();
        int i = 24;
        for (AbstractConfigListEntry<?> entry : entries) {
            i += entry.getItemHeight();
            if (entry.getMorePossibleHeight() >= 0) {
                list.add(i + entry.getMorePossibleHeight());
            }
        }
        list.add(i);
        return list.stream().max(Integer::compare).orElse(0) - getItemHeight();
    }
    
    @Override
    public List<? extends Element> children() {
        return isExpanded() ? (List) children : Collections.singletonList(widget);
    }
    
    @Override
    public List<? extends Selectable> narratables() {
        return isExpanded() ? (List) children : Collections.singletonList(widget);
    }
    
    @Override
    public void save() {
        entries.forEach(AbstractConfigListEntry::save);
    }
    
    @Override
    public Optional<Text> getError() {
        List<Text> errors = entries.stream().map(AbstractConfigListEntry::getConfigError).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        
        if (errors.size() > 1)
            return Optional.of(Text.translatable("text.cloth-config.multi_error"));
        else
            return errors.stream().findFirst();
    }
    
    @Override
    public boolean isExpanded() {
        return this.expanded && isEnabled();
    }
    
    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    public class CategoryLabelWidget implements Element, Selectable {
        private final Rectangle rectangle = new Rectangle();
        private boolean isHovered;
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int int_1) {
            if (isEnabled() && rectangle.contains(mouseX, mouseY)) {
                setExpanded(!expanded);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return isHovered = true;
            }
            return isHovered = false;
        }
        
        @Override
        public void setFocused(boolean bl) {
        
        }
    
        @Override
        public boolean isFocused() {
            return false;
        }
        
        @Override
        public SelectionType getType() {
            return isHovered ? SelectionType.HOVERED : SelectionType.NONE;
        }
        
        @Override
        public void appendNarrations(NarrationMessageBuilder narrationElementOutput) {
            narrationElementOutput.put(NarrationPart.TITLE, getFieldName());
        }
    }
}