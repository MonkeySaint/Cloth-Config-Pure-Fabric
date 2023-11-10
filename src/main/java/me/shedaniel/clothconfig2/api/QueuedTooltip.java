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

package me.shedaniel.clothconfig2.api;

import me.shedaniel.math.Point;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueuedTooltip implements Tooltip {
    private final Point location;
    private final List<OrderedText> text;
    
    private QueuedTooltip(Point location, List<OrderedText> text) {
        this.location = location;
        this.text = Collections.unmodifiableList(text);
    }
    
    public static QueuedTooltip create(Point location, List<Text> text) {
        return new QueuedTooltip(location, Language.getInstance().reorder((List) text));
    }
    
    public static QueuedTooltip create(Point location, Text... text) {
        return QueuedTooltip.create(location, Arrays.asList(text));
    }
    
    public static QueuedTooltip create(Point location, OrderedText... text) {
        return new QueuedTooltip(location, Arrays.asList(text));
    }
    
    public static QueuedTooltip create(Point location, StringVisitable... text) {
        return new QueuedTooltip(location, Language.getInstance().reorder(Arrays.asList(text)));
    }
    
    @Override
    public Point getPoint() {
        return location;
    }
    
    @ApiStatus.Internal
    @Override
    public List<OrderedText> getText() {
        return text;
    }
}
