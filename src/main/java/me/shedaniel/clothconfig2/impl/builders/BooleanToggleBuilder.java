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

package me.shedaniel.clothconfig2.impl.builders;

import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BooleanToggleBuilder extends AbstractFieldBuilder<Boolean, BooleanListEntry, BooleanToggleBuilder> {
    @Nullable private Function<Boolean, Text> yesNoTextSupplier = null;
    
    public BooleanToggleBuilder(Text resetButtonKey, Text fieldNameKey, boolean value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    @Override
    public BooleanToggleBuilder setErrorSupplier(Function<Boolean, Optional<Text>> errorSupplier) {
        return super.setErrorSupplier(errorSupplier);
    }
    
    @Override
    public BooleanToggleBuilder requireRestart() {
        return super.requireRestart();
    }
    
    @Override
    public BooleanToggleBuilder setSaveConsumer(Consumer<Boolean> saveConsumer) {
        return super.setSaveConsumer(saveConsumer);
    }
    
    @Override
    public BooleanToggleBuilder setDefaultValue(Supplier<Boolean> defaultValue) {
        return super.setDefaultValue(defaultValue);
    }
    
    public BooleanToggleBuilder setDefaultValue(boolean defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    @Override
    public BooleanToggleBuilder setTooltipSupplier(Function<Boolean, Optional<Text[]>> tooltipSupplier) {
        return super.setTooltipSupplier(tooltipSupplier);
    }
    
    @Override
    public BooleanToggleBuilder setTooltipSupplier(Supplier<Optional<Text[]>> tooltipSupplier) {
        return super.setTooltipSupplier(tooltipSupplier);
    }
    
    @Override
    public BooleanToggleBuilder setTooltip(Optional<Text[]> tooltip) {
        return super.setTooltip(tooltip);
    }
    
    @Override
    public BooleanToggleBuilder setTooltip(Text... tooltip) {
        return super.setTooltip(tooltip);
    }
    
    @Nullable
    public Function<Boolean, Text> getYesNoTextSupplier() {
        return yesNoTextSupplier;
    }
    
    public BooleanToggleBuilder setYesNoTextSupplier(@Nullable Function<Boolean, Text> yesNoTextSupplier) {
        this.yesNoTextSupplier = yesNoTextSupplier;
        return this;
    }
    
    @NotNull
    @Override
    public BooleanListEntry build() {
        BooleanListEntry entry = new BooleanListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, getSaveConsumer(), null, isRequireRestart()) {
            @Override
            public Text getYesNoText(boolean bool) {
                if (yesNoTextSupplier == null)
                    return super.getYesNoText(bool);
                return yesNoTextSupplier.apply(bool);
            }
        };
        entry.setTooltipSupplier(() -> getTooltipSupplier().apply(entry.getValue()));
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        return finishBuilding(entry);
    }
    
}
