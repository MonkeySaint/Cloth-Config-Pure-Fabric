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

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.Tooltip;
import me.shedaniel.math.Point;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public abstract class TooltipListEntry<T> extends AbstractConfigListEntry<T> {
    @Nullable private Supplier<Optional<Text[]>> tooltipSupplier;
    
    @ApiStatus.Internal
    @Deprecated
    public TooltipListEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, tooltipSupplier, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public TooltipListEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, requiresRestart);
        this.tooltipSupplier = tooltipSupplier;
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        if (isMouseInside(mouseX, mouseY, x, y, entryWidth, entryHeight)) {
            getTooltip(mouseX, mouseY)
                    .map(lines -> Tooltip.of(new Point(mouseX, mouseY), wrapLinesToScreen(lines)))
                    .ifPresent(this::addTooltip);
        }
    }
    
    public Optional<Text[]> getTooltip() {
        Stream<Text> tooltipStream = Stream.ofNullable(tooltipSupplier)
                .map(Supplier::get)
                .flatMap(Optional::stream)
                .flatMap(Arrays::stream);
        
        @Nullable Text disabled = this.isEnabled() ? null : Text.translatable("text.cloth-config.disabled_tooltip");
        
        Text[] lines = Stream.concat(tooltipStream, Stream.ofNullable(disabled))
                .toArray(Text[]::new);
        
        return lines.length < 1 ? Optional.empty() : Optional.of(lines);
        
    }
    
    public Optional<Text[]> getTooltip(int mouseX, int mouseY) {
        return getTooltip();
    }
    
    @Nullable
    public Supplier<Optional<Text[]>> getTooltipSupplier() {
        return tooltipSupplier;
    }
    
    public void setTooltipSupplier(@Nullable Supplier<Optional<Text[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
    }

}
