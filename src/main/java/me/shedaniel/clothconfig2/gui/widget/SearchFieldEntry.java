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

package me.shedaniel.clothconfig2.gui.widget;

import com.google.common.collect.Iterators;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigScreen;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import java.util.*;

public class SearchFieldEntry extends AbstractConfigListEntry<Object> {
    private final TextFieldWidget editBox;
    private String[] lowerCases;
    
    public SearchFieldEntry(ConfigScreen screen, ClothConfigScreen.ListWidget<AbstractConfigEntry<AbstractConfigEntry<?>>> listWidget) {
        super(Text.empty(), false);
        this.editBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 100, 18, Text.empty());
        this.lowerCases = editBox.getText().isEmpty() ? new String[0] : editBox.getText().toLowerCase(Locale.ROOT).split(" ");
        this.editBox.setChangedListener(s -> {
            lowerCases = s.isEmpty() ? new String[0] : s.toLowerCase(Locale.ROOT).split(" ");
        });
        listWidget.entriesTransformer = entries -> {
            return new AbstractList<AbstractConfigEntry<AbstractConfigEntry<?>>>() {
                @Override
                public Iterator<AbstractConfigEntry<AbstractConfigEntry<?>>> iterator() {
                    if (editBox.getText().isEmpty())
                        return entries.iterator();
                    return Iterators.filter(entries.iterator(), entry -> {
                        return entry.isDisplayed() && screen.matchesSearch(entry.getSearchTags());
                    });
                }
                
                @Override
                public AbstractConfigEntry<AbstractConfigEntry<?>> get(int index) {
                    return Iterators.get(iterator(), index);
                }
                
                @Override
                public void add(int index, AbstractConfigEntry<AbstractConfigEntry<?>> element) {
                    entries.add(index, element);
                }
                
                @Override
                public AbstractConfigEntry<AbstractConfigEntry<?>> remove(int index) {
                    AbstractConfigEntry<AbstractConfigEntry<?>> entry = get(index);
                    return entries.remove(entry) ? entry : null;
                }
                
                @Override
                public boolean remove(Object o) {
                    return entries.remove(o);
                }
                
                @Override
                public void clear() {
                    entries.clear();
                }
                
                @Override
                public int size() {
                    return Iterators.size(iterator());
                }
            };
        };
    }
    
    public boolean matchesSearch(Iterator<String> tags) {
        if (lowerCases.length == 0) return true;
        if (!tags.hasNext()) return true;
        for (String lowerCase : lowerCases) {
            boolean found = false;
            for (String tag : (Iterable<? extends String>) (() -> tags)) {
                if (tag.toLowerCase(Locale.ROOT).contains(lowerCase)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.editBox.setWidth(MathHelper.clamp(entryWidth - 10, 0, 500));
        this.editBox.setX(x + entryWidth / 2 - this.editBox.getWidth() / 2);
        this.editBox.setY(y + entryHeight / 2 - 9);
        this.editBox.render(graphics, mouseX, mouseY, delta);
        if (this.editBox.getText().isEmpty()) {
            this.editBox.setSuggestion("Search...");
        } else {
            this.editBox.setSuggestion(null);
        }
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
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
    public List<? extends Selectable> narratables() {
        return List.of(editBox);
    }
    
    @Override
    public List<? extends Element> children() {
        return List.of(editBox);
    }
}
