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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class DynamicElementListWidget<E extends DynamicElementListWidget.ElementEntry<E>> extends DynamicSmoothScrollingEntryListWidget<E> {
    private static final Text USAGE_NARRATION = Text.translatable("narration.selection.usage");
    
    public DynamicElementListWidget(MinecraftClient client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
        super(client, width, height, top, bottom, backgroundLocation);
    }
    
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation focusNavigationEvent) {
        if (this.getItemCount() == 0) {
            return null;
        } else if (!(focusNavigationEvent instanceof GuiNavigation.Arrow arrowNavigation)) {
            return super.getNavigationPath(focusNavigationEvent);
        } else {
            E entry = this.getFocused();
            if (arrowNavigation.direction().getAxis() == NavigationAxis.HORIZONTAL && entry != null) {
                return GuiNavigationPath.of(this, entry.getNavigationPath(focusNavigationEvent));
            } else {
                int i = -1;
                NavigationDirection screenDirection = arrowNavigation.direction();
                if (entry != null) {
                    i = entry.children().indexOf(entry.getFocused());
                }
                
                if (i == -1) {
                    switch (screenDirection) {
                        case LEFT:
                            i = Integer.MAX_VALUE;
                            screenDirection = NavigationDirection.DOWN;
                            break;
                        case RIGHT:
                            i = 0;
                            screenDirection = NavigationDirection.DOWN;
                            break;
                        default:
                            i = 0;
                    }
                }
                
                E entry2 = entry;
                
                GuiNavigationPath componentPath;
                do {
                    entry2 = this.nextEntry(screenDirection, (entryx) -> {
                        return !entryx.children().isEmpty();
                    }, entry2);
                    if (entry2 == null) {
                        return null;
                    }
                    
                    componentPath = entry2.focusPathAtIndex(arrowNavigation, i);
                } while (componentPath == null);
                
                return GuiNavigationPath.of(this, componentPath);
            }
        }
    }
    
    public void appendNarrations(NarrationMessageBuilder narrationElementOutput) {
        E entry = this.hoveredItem;
        if (entry != null) {
            entry.appendNarrations(narrationElementOutput.nextMessage());
            this.narrateListElementPosition(narrationElementOutput, entry);
        } else {
            E entry2 = this.getFocused();
            if (entry2 != null) {
                entry2.appendNarrations(narrationElementOutput.nextMessage());
                this.narrateListElementPosition(narrationElementOutput, entry2);
            }
        }
        
        narrationElementOutput.put(NarrationPart.USAGE, Text.translatable("narration.component_list.usage"));
    }
    
    public void setFocused(@Nullable Element guiEventListener) {
        super.setFocused(guiEventListener);
        if (guiEventListener == null) {
            this.selectItem(null);
        }
        
    }
    
    public SelectionType getType() {
        return this.isFocused() ? SelectionType.FOCUSED : super.getType();
    }
    
    protected boolean isSelected(int i) {
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public abstract static class ElementEntry<E extends ElementEntry<E>> extends Entry<E> implements ParentElement, Selectable {
        @Nullable
        private Element focused;
        @Nullable
        private Selectable lastNarratable;
        private boolean dragging;
        
        public ElementEntry() {
        }
        
        public boolean isDragging() {
            return this.dragging;
        }
        
        public void setDragging(boolean bl) {
            this.dragging = bl;
        }
        
        @Nullable
        public Element getFocused() {
            return this.focused;
        }
        
        public void setFocused(@Nullable Element guiEventListener) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }
            
            if (guiEventListener != null) {
                guiEventListener.setFocused(true);
            }
            
            this.focused = guiEventListener;
        }
        
        @Nullable
        public GuiNavigationPath focusPathAtIndex(GuiNavigation focusNavigationEvent, int i) {
            if (this.children().isEmpty()) {
                return null;
            } else {
                GuiNavigationPath componentPath = this.children().get(Math.min(i, this.children().size() - 1)).getNavigationPath(focusNavigationEvent);
                return GuiNavigationPath.of(this, componentPath);
            }
        }
        
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation focusNavigationEvent) {
            if (focusNavigationEvent instanceof GuiNavigation.Arrow arrowNavigation) {
                int var10000 = switch (arrowNavigation.direction()) {
                    case LEFT -> -1;
                    case RIGHT -> 1;
                    case UP, DOWN -> 0;
                };
    
                if (var10000 == 0) {
                    return null;
                }
                
                int j = MathHelper.clamp(var10000 + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);
                
                for (int k = j; k >= 0 && k < this.children().size(); k += var10000) {
                    Element guiEventListener = this.children().get(k);
                    GuiNavigationPath componentPath = guiEventListener.getNavigationPath(focusNavigationEvent);
                    if (componentPath != null) {
                        return GuiNavigationPath.of(this, componentPath);
                    }
                }
            }
            
            return ParentElement.super.getNavigationPath(focusNavigationEvent);
        }
        
        public abstract List<? extends Selectable> narratables();
        
        public void appendNarrations(NarrationMessageBuilder narrationElementOutput) {
            List<? extends Selectable> list = this.narratables();
            Screen.SelectedElementNarrationData narratableSearchResult = Screen.findSelectedElementData(list, this.lastNarratable);
            if (narratableSearchResult != null) {
                if (narratableSearchResult.selectType.isFocused()) {
                    this.lastNarratable = narratableSearchResult.selectable;
                }
                
                if (list.size() > 1) {
                    narrationElementOutput.put(NarrationPart.POSITION, Text.translatable("narrator.position.object_list", narratableSearchResult.index + 1, list.size()));
                    if (narratableSearchResult.selectType == SelectionType.FOCUSED) {
                        narrationElementOutput.put(NarrationPart.USAGE, Text.translatable("narration.component_list.usage"));
                    }
                }
                
                narratableSearchResult.selectable.appendNarrations(narrationElementOutput.nextMessage());
            }
            
        }
        
        @Override
        public boolean isNarratable() {
            return false;
        }
        
        @Override
        public SelectionType getType() {
            if (this.isFocused()) {
                return SelectionType.FOCUSED;
            } else {
                return SelectionType.NONE;
            }
        }
        
        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.mouseClicked(d, e, i);
        }
        
        @Override
        public boolean mouseReleased(double d, double e, int i) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.mouseReleased(d, e, i);
        }
        
        @Override
        public boolean mouseDragged(double d, double e, int i, double f, double g) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.mouseDragged(d, e, i, f, g);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double amountX, double amountY) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.mouseScrolled(d, e, amountX, amountY);
        }
        
        @Override
        public boolean keyPressed(int i, int j, int k) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.keyPressed(i, j, k);
        }
        
        @Override
        public boolean keyReleased(int i, int j, int k) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.keyReleased(i, j, k);
        }
        
        @Override
        public boolean charTyped(char c, int i) {
            if (!isEnabled()) {
                return false;
            }
            return ParentElement.super.charTyped(c, i);
        }
    }
}

