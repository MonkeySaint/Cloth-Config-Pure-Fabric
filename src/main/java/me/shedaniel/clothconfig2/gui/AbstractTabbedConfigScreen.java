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

package me.shedaniel.clothconfig2.gui;

import com.google.common.collect.Maps;
import me.shedaniel.clothconfig2.api.TabbedConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.Map;

public abstract class AbstractTabbedConfigScreen extends AbstractConfigScreen implements TabbedConfigScreen {
    private final Map<String, Identifier> categoryBackgroundLocation = Maps.newHashMap();
    
    protected AbstractTabbedConfigScreen(Screen parent, Text title, Identifier backgroundLocation) {
        super(parent, title, backgroundLocation);
    }
    
    @Override
    public final void registerCategoryBackground(String text, Identifier identifier) {
        this.categoryBackgroundLocation.put(text, identifier);
    }
    
    @Override
    public Identifier getBackgroundLocation() {
        Text selectedCategory = getSelectedCategory();
        if (categoryBackgroundLocation.containsKey(selectedCategory.getString()))
            return categoryBackgroundLocation.get(selectedCategory.getString());
        return super.getBackgroundLocation();
    }
}
