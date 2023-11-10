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
import java.util.List;

public interface Tooltip {
    static Tooltip of(Point location, Text... text) {
        return QueuedTooltip.create(location, text);
    }
    
    static Tooltip of(Point location, StringVisitable... text) {
        return QueuedTooltip.create(location, text);
    }
    
    static Tooltip of(Point location, OrderedText... text) {
        return QueuedTooltip.create(location, text);
    }
    
    Point getPoint();
    
    default int getX() {
        return getPoint().getX();
    }
    
    default int getY() {
        return getPoint().getY();
    }
    
    List<OrderedText> getText();
}
