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

import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TextListEntry extends TooltipListEntry<Object> {
    public static final int LINE_HEIGHT = 12;
    public static final int DISABLED_COLOR = Objects.requireNonNull(Formatting.DARK_GRAY.getColorValue());
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final int color;
    private final Text text;
    private int savedWidth = -1;
    private int savedX = -1;
    private int savedY = -1;
    private List<OrderedText> wrappedLines;
    
    @ApiStatus.Internal
    @Deprecated
    public TextListEntry(Text fieldName, Text text) {
        this(fieldName, text, -1);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public TextListEntry(Text fieldName, Text text, int color) {
        this(fieldName, text, color, null);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public TextListEntry(Text fieldName, Text text, int color, Supplier<Optional<Text[]>> tooltipSupplier) {
        super(fieldName, tooltipSupplier);
        this.text = text;
        this.color = color;
        this.wrappedLines = Collections.emptyList();
    }
    
    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        if (this.savedWidth != entryWidth || this.savedX != x || this.savedY != y) {
            this.wrappedLines = this.textRenderer.wrapLines(this.text, entryWidth);
            this.savedWidth = entryWidth;
            this.savedX = x;
            this.savedY = y;
        }
        int yy = y + 7;
        int textColor = isEnabled() ? color : DISABLED_COLOR;
        for (OrderedText string : wrappedLines) {
            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, string, x, yy, textColor);
            yy += MinecraftClient.getInstance().textRenderer.fontHeight + 3;
        }
        
        Style style = this.getTextAt(mouseX, mouseY);
        AbstractConfigScreen configScreen = this.getConfigScreen();
        
        if (style != null && configScreen != null) {
            graphics.drawHoverEvent(MinecraftClient.getInstance().textRenderer, style, mouseX, mouseY);
        }
    }
    
    @Override
    public int getItemHeight() {
        if (savedWidth == -1) return LINE_HEIGHT;
        int lineCount = this.wrappedLines.size();
        return lineCount == 0 ? 0 : 14 + lineCount * LINE_HEIGHT;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = this.getTextAt(mouseX, mouseY);
            AbstractConfigScreen configScreen = this.getConfigScreen();
            if (configScreen != null && configScreen.handleTextClick(style)) {
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Nullable
    private Style getTextAt(double x, double y) {
        int lineCount = this.wrappedLines.size();
        
        if (lineCount > 0) {
            int textX = MathHelper.floor(x - this.savedX);
            int textY = MathHelper.floor(y - 7 - this.savedY);
            if (textX >= 0 && textY >= 0 && textX <= this.savedWidth && textY < LINE_HEIGHT * lineCount + lineCount) {
                int line = textY / LINE_HEIGHT;
                if (line < this.wrappedLines.size()) {
                    OrderedText orderedText = this.wrappedLines.get(line);
                    return this.textRenderer.getTextHandler().getStyleAt(orderedText, textX);
                }
            }
        }
        return null;
    }
    
    @Override
    public Object getValue() {
        return null;
    }
    
    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public List<? extends Selectable> narratables() {
        return Collections.emptyList();
    }
}
