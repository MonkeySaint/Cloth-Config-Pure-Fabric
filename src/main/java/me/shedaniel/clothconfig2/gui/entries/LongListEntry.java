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
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class LongListEntry extends AbstractNumberListEntry<Long> {
    @ApiStatus.Internal
    @Deprecated
    public LongListEntry(Text fieldName, Long value, Text resetButtonKey, Supplier<Long> defaultValue, Consumer<Long> saveConsumer) {
        super(fieldName, value, resetButtonKey, defaultValue);
        this.saveCallback = saveConsumer;
    }
    
    @ApiStatus.Internal
    @Deprecated
    public LongListEntry(Text fieldName, Long value, Text resetButtonKey, Supplier<Long> defaultValue, Consumer<Long> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public LongListEntry(Text fieldName, Long value, Text resetButtonKey, Supplier<Long> defaultValue, Consumer<Long> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
        this.saveCallback = saveConsumer;
    }
    
    @Override
    protected Map.Entry<Long, Long> getDefaultRange() {
        return new AbstractMap.SimpleEntry<>(-Long.MAX_VALUE, Long.MAX_VALUE);
    }
    
    public LongListEntry setMinimum(long minimum) {
        this.minimum = minimum;
        return this;
    }
    
    public LongListEntry setMaximum(long maximum) {
        this.maximum = maximum;
        return this;
    }
    
    @Override
    public Long getValue() {
        try {
            return Long.valueOf(textFieldWidget.getText());
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public Optional<Text> getError() {
        try {
            long i = Long.parseLong(textFieldWidget.getText());
            if (i > maximum)
                return Optional.of(Text.translatable("text.cloth-config.error.too_large", maximum));
            else if (i < minimum)
                return Optional.of(Text.translatable("text.cloth-config.error.too_small", minimum));
        } catch (NumberFormatException ex) {
            return Optional.of(Text.translatable("text.cloth-config.error.not_valid_number_long"));
        }
        return super.getError();
    }
}
