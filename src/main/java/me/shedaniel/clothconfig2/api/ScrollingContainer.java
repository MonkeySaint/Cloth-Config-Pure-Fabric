/*
 * The smooth scrolling code is partially taken from osu-framework.
 * <p>
 * Copyright (c) 2020 ppy Pty Ltd <contact@ppy.sh>.
 * Copyright (c) 2018, 2019, 2020 shedaniel.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.shedaniel.clothconfig2.api;

import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Deprecated
public abstract class ScrollingContainer {
    public double scrollAmount;
    public double scrollTarget;
    public long start;
    public long duration;
    public boolean draggingScrollBar = false;
    
    public abstract Rectangle getBounds();
    
    public Rectangle getScissorBounds() {
        Rectangle bounds = getBounds();
        if (hasScrollBar()) {
            return new Rectangle(bounds.x, bounds.y, bounds.width - 6, bounds.height);
        }
        return bounds;
    }
    
    public int getScrollBarX() {
        return hasScrollBar() ? getBounds().getMaxX() - 6 : getBounds().getMaxX();
    }
    
    public boolean hasScrollBar() {
        return getMaxScrollHeight() > getBounds().height;
    }
    
    public abstract int getMaxScrollHeight();
    
    public final int getMaxScroll() {
        return Math.max(0, getMaxScrollHeight() - getBounds().height);
    }
    
    public final double clamp(double v) {
        return this.clamp(v, 200.0D);
    }
    
    public final double clamp(double v, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, (double) this.getMaxScroll() + clampExtension);
    }
    
    public final void offset(double value, boolean animated) {
        scrollTo(scrollTarget + value, animated);
    }
    
    public final void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    public final void scrollTo(double value, boolean animated, long duration) {
        scrollTarget = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scrollAmount = scrollTarget;
    }
    
    public void updatePosition(float delta) {
        double[] target = new double[]{this.scrollTarget};
        this.scrollAmount = handleScrollingPosition(target, this.scrollAmount, this.getMaxScroll(), delta, this.start, this.duration);
        this.scrollTarget = target[0];
    }
    
    public static double handleScrollingPosition(double[] target, double scroll, double maxScroll, float delta, double start, double duration) {
        return handleScrollingPosition(target, scroll, maxScroll, delta, start, duration, ClothConfigInitializer.getBounceBackMultiplier(), ClothConfigInitializer.getEasingMethod());
    }
    
    public static double handleScrollingPosition(double[] target, double scroll, double maxScroll, float delta, double start, double duration, double bounceBackMultiplier, EasingMethod easingMethod) {
        if (bounceBackMultiplier >= 0) {
            target[0] = clampExtension(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - bounceBackMultiplier) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - bounceBackMultiplier) * delta / 3) + maxScroll;
            }
        } else
            target[0] = clampExtension(target[0], maxScroll, 0);
        return ease(scroll, target[0], Math.min((System.currentTimeMillis() - start) / duration * delta * 3, 1), easingMethod);
    }
    
    public static double ease(double start, double end, double amount, EasingMethod easingMethod) {
        return start + (end - start) * easingMethod.apply(amount);
    }
    
    public static double clampExtension(double value, double maxScroll) {
        return clampExtension(value, maxScroll, DynamicEntryListWidget.SmoothScrollingSettings.CLAMP_EXTENSION);
    }
    
    public static double clampExtension(double v, double maxScroll, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }
    
    public void renderScrollBar(DrawContext graphics) {
        renderScrollBar(graphics, 0, 1, 1);
    }
    
    public void renderScrollBar(DrawContext graphics, int background, float alpha, float scrollBarAlphaOffset) {
        if (hasScrollBar()) {
            Rectangle bounds = getBounds();
            int maxScroll = getMaxScroll();
            int height = bounds.height * bounds.height / getMaxScrollHeight();
            height = MathHelper.clamp(height, 32, bounds.height);
            height -= Math.min((scrollAmount < 0 ? (int) -scrollAmount : scrollAmount > maxScroll ? (int) scrollAmount - maxScroll : 0), height * .95);
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) scrollAmount * (bounds.height - height) / maxScroll + bounds.y, bounds.y), bounds.getMaxY() - height);
            
            int scrollbarPositionMinX = getScrollBarX();
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            boolean hovered = (new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height)).contains(PointHelper.ofMouse());
            float bottomC = (hovered ? .67f : .5f) * scrollBarAlphaOffset;
            float topC = (hovered ? .87f : .67f) * scrollBarAlphaOffset;
            
            graphics.fill(scrollbarPositionMinX, bounds.y, scrollbarPositionMaxX, bounds.getMaxY(), background);
            graphics.fill(scrollbarPositionMinX, minY, scrollbarPositionMaxX, minY + height,
                    ColorHelper.Argb.getArgb(Math.round(alpha * 255.0F), Math.round(bottomC * 255.0F), Math.round(bottomC * 255.0F), Math.round(bottomC * 255.0F)));
            graphics.fill(scrollbarPositionMinX, minY, scrollbarPositionMaxX - 1, minY + height - 1,
                    ColorHelper.Argb.getArgb(Math.round(alpha * 255.0F), Math.round(topC * 255.0F), Math.round(topC * 255.0F), Math.round(topC * 255.0F)));
        }
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return mouseDragged(mouseX, mouseY, button, dx, dy, false, 0);
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy, boolean snapToRows, double rowSize) {
        if (button == 0 && draggingScrollBar) {
            float height = getMaxScrollHeight();
            Rectangle bounds = getBounds();
            int actualHeight = bounds.height;
            if (mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
                double maxScroll = Math.max(1, getMaxScroll());
                double int_3 = MathHelper.clamp(((double) (actualHeight * actualHeight) / (double) height), 32, actualHeight - 8);
                double double_6 = Math.max(1.0D, maxScroll / (actualHeight - int_3));
                float to = MathHelper.clamp((float) (scrollAmount + dy * double_6), 0, getMaxScroll());
                if (snapToRows) {
                    double nearestRow = Math.round(to / rowSize) * rowSize;
                    scrollTo(nearestRow, false);
                } else
                    scrollTo(to, false);
            }
            return true;
        }
        return false;
    }
    
    public boolean updateDraggingState(double mouseX, double mouseY, int button) {
        if (!hasScrollBar())
            return false;
        double height = getMaxScroll();
        Rectangle bounds = getBounds();
        int actualHeight = bounds.height;
        if (height > actualHeight && mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
            double scrollbarPositionMinX = getScrollBarX();
            if (mouseX >= scrollbarPositionMinX - 1 & mouseX <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                return true;
            }
        }
        this.draggingScrollBar = false;
        return false;
    }
}
