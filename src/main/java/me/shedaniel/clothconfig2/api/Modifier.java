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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import java.util.Objects;

/**
 * Taken from amecs for a lightweight modifers api:
 * https://github.com/Siphalor/amecs
 *
 * @author Siphalor
 */
@Environment(EnvType.CLIENT)
public class Modifier {
    private final short value;
    
    /**
     * Constructs a new modifier object by a raw value
     *
     * @param value the raw value with flags set
     */
    private Modifier(short value) {
        this.value = value;
    }
    
    public static Modifier none() {
        return of((short) 0);
    }
    
    /**
     * Constructs a new modifier object by all modifier bits
     *
     * @param alt     sets whether the alt flag should be set
     * @param control sets whether the control flag should be set
     * @param shift   sets whether the shift flag should be set
     */
    public static Modifier of(boolean alt, boolean control, boolean shift) {
        short value = setFlag((short) 0, (short) 1, alt);
        value = setFlag(value, (short) 2, control);
        value = setFlag(value, (short) 4, shift);
        return of(value);
    }
    
    public static Modifier of(short value) {
        return new Modifier(value);
    }
    
    public static Modifier current() {
        return Modifier.of(Screen.hasAltDown(), Screen.hasControlDown(), Screen.hasShiftDown());
    }
    
    private static short setFlag(short base, short flag, boolean val) {
        return val ? setFlag(base, flag) : removeFlag(base, flag);
    }
    
    private static short setFlag(short base, short flag) {
        return (short) (base | flag);
    }
    
    private static short removeFlag(short base, short flag) {
        return (short) (base & (~flag));
    }
    
    private static boolean getFlag(short base, short flag) {
        return (base & flag) != 0;
    }
    
    /**
     * Compares this object with the current pressed keys
     *
     * @return whether the modifiers match in the current context
     */
    public boolean matchesCurrent() {
        return equals(current());
    }
    
    /**
     * Gets the raw value
     *
     * @return the value with all flags set
     */
    public short getValue() {
        return value;
    }
    
    /**
     * Gets the state of the alt flag
     *
     * @return whether the alt key needs to be pressed
     */
    public boolean hasAlt() {
        return getFlag(value, (short) 1);
    }
    
    /**
     * Gets the state of the control flag
     *
     * @return whether the control key needs to be pressed
     */
    public boolean hasControl() {
        return getFlag(value, (short) 2);
    }
    
    /**
     * Gets the state of the shift flag
     *
     * @return whether the shift key needs to be pressed
     */
    public boolean hasShift() {
        return getFlag(value, (short) 4);
    }
    
    /**
     * Returns whether no flag is set
     *
     * @return value == 0
     */
    public boolean isEmpty() {
        return value == 0;
    }
    
    /**
     * Returns whether this object equals another one
     *
     * @param other another modifier object
     * @return whether both values are equal
     */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof Modifier))
            return false;
        return value == ((Modifier) other).value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
