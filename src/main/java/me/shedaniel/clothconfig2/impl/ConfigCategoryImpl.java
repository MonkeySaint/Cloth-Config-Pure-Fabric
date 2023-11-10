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

package me.shedaniel.clothconfig2.impl;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ConfigCategoryImpl implements ConfigCategory {
    private final ConfigBuilder builder;
    private final List<Object> data;
    @Nullable
    private Identifier background;
    private final Text categoryKey;
    @Nullable
    private Supplier<Optional<StringVisitable[]>> description = Optional::empty;
    
    ConfigCategoryImpl(ConfigBuilder builder, Text categoryKey) {
        this.builder = builder;
        this.data = Lists.newArrayList();
        this.categoryKey = categoryKey;
    }
    
    @Override
    public Text getCategoryKey() {
        return categoryKey;
    }
    
    @Override
    public List<Object> getEntries() {
        return data;
    }
    
    @Override
    public ConfigCategory addEntry(AbstractConfigListEntry entry) {
        data.add(entry);
        return this;
    }
    
    @Override
    public ConfigCategory setCategoryBackground(Identifier identifier) {
        if (builder.hasTransparentBackground())
            throw new IllegalStateException("Cannot set category background if screen is using transparent background.");
        background = identifier;
        return this;
    }
    
    @Override
    public void removeCategory() {
        builder.removeCategory(categoryKey);
    }
    
    @Override
    public void setBackground(@Nullable Identifier background) {
        this.background = background;
    }
    
    @Override
    @Nullable
    public Identifier getBackground() {
        return background;
    }
    
    @Nullable
    @Override
    public Supplier<Optional<StringVisitable[]>> getDescription() {
        return description;
    }
    
    @Override
    public void setDescription(@Nullable Supplier<Optional<StringVisitable[]>> description) {
        this.description = description;
    }
}
