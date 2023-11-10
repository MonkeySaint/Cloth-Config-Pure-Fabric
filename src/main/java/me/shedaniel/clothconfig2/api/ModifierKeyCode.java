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

import me.shedaniel.clothconfig2.impl.ModifierKeyCodeImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public interface ModifierKeyCode {
    static ModifierKeyCode of(InputUtil.Key keyCode, Modifier modifier) {
        return new ModifierKeyCodeImpl().setKeyCodeAndModifier(keyCode, modifier);
    }
    
    static ModifierKeyCode copyOf(ModifierKeyCode code) {
        return of(code.getKeyCode(), code.getModifier());
    }
    
    static ModifierKeyCode unknown() {
        return of(InputUtil.UNKNOWN_KEY, Modifier.none());
    }
    
    InputUtil.Key getKeyCode();
    
    ModifierKeyCode setKeyCode(InputUtil.Key keyCode);
    
    default InputUtil.Type getType() {
        return getKeyCode().getCategory();
    }
    
    Modifier getModifier();
    
    ModifierKeyCode setModifier(Modifier modifier);
    
    default ModifierKeyCode copy() {
        return copyOf(this);
    }
    
    default boolean matchesMouse(int button) {
        return !isUnknown() && getType() == InputUtil.Type.MOUSE && getKeyCode().getCode() == button && getModifier().matchesCurrent();
    }
    
    default boolean matchesKey(int keyCode, int scanCode) {
        if (isUnknown())
            return false;
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            return getType() == InputUtil.Type.SCANCODE && getKeyCode().getCode() == scanCode && getModifier().matchesCurrent();
        } else {
            return getType() == InputUtil.Type.KEYSYM && getKeyCode().getCode() == keyCode && getModifier().matchesCurrent();
        }
    }
    
    default boolean matchesCurrentMouse() {
        if (!isUnknown() && getType() == InputUtil.Type.MOUSE && getModifier().matchesCurrent()) {
            return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), getKeyCode().getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
    
    default boolean matchesCurrentKey() {
        return !isUnknown() && getType() == InputUtil.Type.KEYSYM && getModifier().matchesCurrent() && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), getKeyCode().getCode());
    }
    
    default ModifierKeyCode setKeyCodeAndModifier(InputUtil.Key keyCode, Modifier modifier) {
        setKeyCode(keyCode);
        setModifier(modifier);
        return this;
    }
    
    default ModifierKeyCode clearModifier() {
        return setModifier(Modifier.none());
    }
    
    String toString();
    
    Text getLocalizedName();
    
    default boolean isUnknown() {
        return getKeyCode().equals(InputUtil.UNKNOWN_KEY);
    }
}
