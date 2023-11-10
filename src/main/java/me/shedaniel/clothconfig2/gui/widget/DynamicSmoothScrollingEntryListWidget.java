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

import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import static me.shedaniel.clothconfig2.api.scroll.ScrollingContainer.clampExtension;
import static me.shedaniel.clothconfig2.api.scroll.ScrollingContainer.handleBounceBack;

@Environment(EnvType.CLIENT)
public abstract class DynamicSmoothScrollingEntryListWidget<E extends DynamicEntryListWidget.Entry<E>> extends DynamicEntryListWidget<E> {
    
    protected boolean smoothScrolling = true;
    protected final NumberAnimator<Double> scrollAnimator = ValueAnimator.ofDouble();
    
    public DynamicSmoothScrollingEntryListWidget(MinecraftClient client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
        super(client, width, height, top, bottom, backgroundLocation);
    }
    
    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }
    
    public void setSmoothScrolling(boolean smoothScrolling) {
        this.smoothScrolling = smoothScrolling;
    }
    
    @Override
    public void capYPosition(double scroll) {
        if (!smoothScrolling) {
            scrollAnimator.setAs(MathHelper.clamp(scroll, 0.0D, this.getMaxScroll()));
        } else {
            scrollAnimator.setAs(clampExtension(scroll, getMaxScroll()));
        }
        this.scroll = scrollAnimator.value();
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!smoothScrolling)
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if ((this.getFocused() != null && this.isDragging() && button == 0) && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        } else if (button == 0 && this.scrolling) {
            if (mouseY < (double) this.top) {
                this.capYPosition(0.0D);
            } else if (mouseY > (double) this.bottom) {
                this.capYPosition(this.getMaxScroll());
            } else {
                double double_5 = Math.max(1, this.getMaxScroll());
                int int_2 = this.bottom - this.top;
                int int_3 = MathHelper.clamp((int) ((float) (int_2 * int_2) / (float) this.getMaxScrollPosition()), 32, int_2 - 8);
                double double_6 = Math.max(1.0D, double_5 / (double) (int_2 - int_3));
                this.capYPosition(MathHelper.clamp(this.getScroll() + deltaY * double_6, 0, getMaxScroll()));
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        for (E entry : visibleChildren()) {
            if (entry.mouseScrolled(mouseX, mouseY, amountX, amountY)) {
                return true;
            }
        }
        if (amountY == 0) return false;
        if (!smoothScrolling) {
            scroll += 16 * -amountY;
            this.scroll = MathHelper.clamp(amountY, 0.0D, this.getMaxScroll());
            return true;
        }
        offset(ClothConfigInitializer.getScrollStep() * -amountY, true);
        return true;
    }
    
    public void offset(double value, boolean animated) {
        scrollTo(scrollAnimator.target() + value, animated);
    }
    
    public void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    public void scrollTo(double value, boolean animated, long duration) {
        if (animated) {
            scrollAnimator.setTo(value, duration);
        } else {
            scrollAnimator.setAs(value);
        }
    }
    
    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        scrollAnimator.setTarget(handleBounceBack(this.scrollAnimator.target(), this.getMaxScroll(), delta));
        this.scrollAnimator.update(delta);
        this.scroll = scrollAnimator.value();
        super.render(graphics, mouseX, mouseY, delta);
    }
    
    @Override
    protected void renderScrollBar(DrawContext graphics, Tessellator tessellator, BufferBuilder buffer, int maxScroll, int scrollbarPositionMinX, int scrollbarPositionMaxX) {
        if (!smoothScrolling)
            super.renderScrollBar(graphics, tessellator, buffer, maxScroll, scrollbarPositionMinX, scrollbarPositionMaxX);
        else if (maxScroll > 0) {
            int height = ((this.bottom - this.top) * (this.bottom - this.top)) / this.getMaxScrollPosition();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height -= Math.min((scroll < 0 ? (int) -scroll : scroll > getMaxScroll() ? (int) scroll - getMaxScroll() : 0), height * .95);
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) this.getScroll() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            
            int bottomc = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(PointHelper.ofMouse()) ? 168 : 128;
            int topc = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(PointHelper.ofMouse()) ? 222 : 172;
            
            graphics.fill(scrollbarPositionMinX, this.top, scrollbarPositionMaxX, this.bottom, 0xff000000);
            graphics.fill(scrollbarPositionMinX, minY, scrollbarPositionMaxX, minY + height,
                    ColorHelper.Argb.getArgb(255, bottomc, bottomc, bottomc));
            graphics.fill(scrollbarPositionMinX, minY, scrollbarPositionMaxX - 1, minY + height - 1,
                    ColorHelper.Argb.getArgb(255, topc, topc, topc));
        }
    }
    
    public static class Interpolation {
        public static double expoEase(double start, double end, double amount) {
            return start + (end - start) * ClothConfigInitializer.getEasingMethod().apply(amount);
        }
    }
    
    public static class Precision {
        public static final float FLOAT_EPSILON = 1e-3f;
        public static final double DOUBLE_EPSILON = 1e-7;
        
        public static boolean almostEquals(float value1, float value2, float acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
        
        public static boolean almostEquals(double value1, double value2, double acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
    }
    
}
