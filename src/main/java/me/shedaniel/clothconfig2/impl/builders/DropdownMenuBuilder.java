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

import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.DefaultSelectionCellCreator;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.DefaultSelectionTopCellElement;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.SelectionCellCreator;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry.SelectionTopCellElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class DropdownMenuBuilder<T> extends FieldBuilder<T, DropdownBoxEntry<T>, DropdownMenuBuilder<T>> {
    protected SelectionTopCellElement<T> topCellElement;
    protected SelectionCellCreator<T> cellCreator;
    protected Function<T, Optional<Text[]>> tooltipSupplier = str -> Optional.empty();
    protected Consumer<T> saveConsumer = null;
    protected Iterable<T> selections = Collections.emptyList();
    protected boolean suggestionMode = true;
    
    public DropdownMenuBuilder(Text resetButtonKey, Text fieldNameKey, SelectionTopCellElement<T> topCellElement, SelectionCellCreator<T> cellCreator) {
        super(resetButtonKey, fieldNameKey);
        this.topCellElement = Objects.requireNonNull(topCellElement);
        this.cellCreator = Objects.requireNonNull(cellCreator);
    }
    
    public DropdownMenuBuilder<T> setSelections(Iterable<T> selections) {
        this.selections = selections;
        return this;
    }
    
    public DropdownMenuBuilder<T> setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public DropdownMenuBuilder<T> setDefaultValue(T defaultValue) {
        this.defaultValue = () -> Objects.requireNonNull(defaultValue);
        return this;
    }
    
    public DropdownMenuBuilder<T> setSaveConsumer(Consumer<T> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public DropdownMenuBuilder<T> setTooltipSupplier(Supplier<Optional<Text[]>> tooltipSupplier) {
        this.tooltipSupplier = str -> tooltipSupplier.get();
        return this;
    }
    
    public DropdownMenuBuilder<T> setTooltipSupplier(Function<T, Optional<Text[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public DropdownMenuBuilder<T> setTooltip(Optional<Text[]> tooltip) {
        this.tooltipSupplier = str -> tooltip;
        return this;
    }
    
    public DropdownMenuBuilder<T> setTooltip(Text... tooltip) {
        this.tooltipSupplier = str -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public DropdownMenuBuilder<T> requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public DropdownMenuBuilder<T> setErrorSupplier(Function<T, Optional<Text>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }
    
    public DropdownMenuBuilder<T> setSuggestionMode(boolean suggestionMode) {
        this.suggestionMode = suggestionMode;
        return this;
    }
    
    public boolean isSuggestionMode() {
        return suggestionMode;
    }
    
    @NotNull
    @Override
    public DropdownBoxEntry<T> build() {
        DropdownBoxEntry<T> entry = new DropdownBoxEntry<>(getFieldNameKey(), getResetButtonKey(), null, isRequireRestart(), defaultValue, saveConsumer, selections, topCellElement, cellCreator);
        entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        entry.setSuggestionMode(suggestionMode);
        return finishBuilding(entry);
    }
    
    public static class TopCellElementBuilder {
        public static final Function<String, Identifier> IDENTIFIER_FUNCTION = str -> {
            try {
                return new Identifier(str);
            } catch (NumberFormatException e) {
                return null;
            }
        };
        public static final Function<String, Identifier> ITEM_IDENTIFIER_FUNCTION = str -> {
            try {
                Identifier identifier = new Identifier(str);
                if (Registries.ITEM.getOrEmpty(identifier).isPresent())
                    return identifier;
            } catch (Exception ignored) {
            }
            return null;
        };
        public static final Function<String, Identifier> BLOCK_IDENTIFIER_FUNCTION = str -> {
            try {
                Identifier identifier = new Identifier(str);
                if (Registries.BLOCK.getOrEmpty(identifier).isPresent())
                    return identifier;
            } catch (Exception ignored) {
            }
            return null;
        };
        public static final Function<String, Item> ITEM_FUNCTION = str -> {
            try {
                return Registries.ITEM.getOrEmpty(new Identifier(str)).orElse(null);
            } catch (Exception ignored) {
            }
            return null;
        };
        public static final Function<String, Block> BLOCK_FUNCTION = str -> {
            try {
                return Registries.BLOCK.getOrEmpty(new Identifier(str)).orElse(null);
            } catch (Exception ignored) {
            }
            return null;
        };
        private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
        
        public static <T> SelectionTopCellElement<T> of(T value, Function<String, T> toObjectFunction) {
            return of(value, toObjectFunction, t -> Text.literal(t.toString()));
        }
        
        public static <T> SelectionTopCellElement<T> of(T value, Function<String, T> toObjectFunction, Function<T, Text> toTextFunction) {
            return new DefaultSelectionTopCellElement<>(value, toObjectFunction, toTextFunction);
        }
        
        public static SelectionTopCellElement<Identifier> ofItemIdentifier(Item item) {
            return new DefaultSelectionTopCellElement<Identifier>(Registries.ITEM.getId(item), ITEM_IDENTIFIER_FUNCTION, identifier -> Text.literal(identifier.toString())) {
                @Override
                public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                    textFieldWidget.setX(x + 4);
                    textFieldWidget.setY(y + 6);
                    textFieldWidget.setWidth(width - 4 - 20);
                    textFieldWidget.setEditable(getParent().isEditable());
                    textFieldWidget.setEditableColor(getPreferredTextColor());
                    textFieldWidget.render(graphics, mouseX, mouseY, delta);
                    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                    ItemStack stack = hasConfigError() ? BARRIER : new ItemStack(Registries.ITEM.get(getValue()));
                    graphics.drawItem(stack, x + width - 18, y + 2);
                }
            };
        }
        
        public static SelectionTopCellElement<Identifier> ofBlockIdentifier(Block block) {
            return new DefaultSelectionTopCellElement<Identifier>(Registries.BLOCK.getId(block), BLOCK_IDENTIFIER_FUNCTION, identifier -> Text.literal(identifier.toString())) {
                @Override
                public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                    textFieldWidget.setX(x + 4);
                    textFieldWidget.setY(y + 6);
                    textFieldWidget.setWidth(width - 4 - 20);
                    textFieldWidget.setEditable(getParent().isEditable());
                    textFieldWidget.setEditableColor(getPreferredTextColor());
                    textFieldWidget.render(graphics, mouseX, mouseY, delta);
                    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                    ItemStack stack = hasConfigError() ? BARRIER : new ItemStack(Registries.BLOCK.get(getValue()));
                    graphics.drawItem(stack, x + width - 18, y + 2);
                }
            };
        }
        
        public static SelectionTopCellElement<Item> ofItemObject(Item item) {
            return new DefaultSelectionTopCellElement<Item>(item, ITEM_FUNCTION, i -> Text.literal(Registries.ITEM.getId(i).toString())) {
                @Override
                public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                    textFieldWidget.setX(x + 4);
                    textFieldWidget.setY(y + 6);
                    textFieldWidget.setWidth(width - 4 - 20);
                    textFieldWidget.setEditable(getParent().isEditable());
                    textFieldWidget.setEditableColor(getPreferredTextColor());
                    textFieldWidget.render(graphics, mouseX, mouseY, delta);
                    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                    ItemStack stack = hasConfigError() ? BARRIER : new ItemStack(getValue());
                    graphics.drawItem(stack, x + width - 18, y + 2);
                }
            };
        }
        
        public static SelectionTopCellElement<Block> ofBlockObject(Block block) {
            return new DefaultSelectionTopCellElement<Block>(block, BLOCK_FUNCTION, i -> Text.literal(Registries.BLOCK.getId(i).toString())) {
                @Override
                public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                    textFieldWidget.setX(x + 4);
                    textFieldWidget.setY(y + 6);
                    textFieldWidget.setWidth(width - 4 - 20);
                    textFieldWidget.setEditable(getParent().isEditable());
                    textFieldWidget.setEditableColor(getPreferredTextColor());
                    textFieldWidget.render(graphics, mouseX, mouseY, delta);
                    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                    ItemStack stack = hasConfigError() ? BARRIER : new ItemStack(getValue());
                    graphics.drawItem(stack, x + width - 18, y + 2);
                }
            };
        }
    }
    
    public static class CellCreatorBuilder {
        public static <T> SelectionCellCreator<T> of() {
            return new DefaultSelectionCellCreator<>();
        }
        
        public static <T> SelectionCellCreator<T> of(Function<T, Text> toTextFunction) {
            return new DefaultSelectionCellCreator<>(toTextFunction);
        }
        
        public static <T> SelectionCellCreator<T> ofWidth(int cellWidth) {
            return new DefaultSelectionCellCreator<T>() {
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> ofWidth(int cellWidth, Function<T, Text> toTextFunction) {
            return new DefaultSelectionCellCreator<T>(toTextFunction) {
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> ofCellCount(int maxItems) {
            return new DefaultSelectionCellCreator<T>() {
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> ofCellCount(int maxItems, Function<T, Text> toTextFunction) {
            return new DefaultSelectionCellCreator<T>(toTextFunction) {
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> of(int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<T>() {
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> of(int cellWidth, int maxItems, Function<T, Text> toTextFunction) {
            return new DefaultSelectionCellCreator<T>(toTextFunction) {
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> of(int cellHeight, int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<T>() {
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static <T> SelectionCellCreator<T> of(int cellHeight, int cellWidth, int maxItems, Function<T, Text> toTextFunction) {
            return new DefaultSelectionCellCreator<T>(toTextFunction) {
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static SelectionCellCreator<Identifier> ofItemIdentifier() {
            return ofItemIdentifier(20, 146, 7);
        }
        
        public static SelectionCellCreator<Identifier> ofItemIdentifier(int maxItems) {
            return ofItemIdentifier(20, 146, maxItems);
        }
        
        public static SelectionCellCreator<Identifier> ofItemIdentifier(int cellHeight, int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<Identifier>() {
                @Override
                public DropdownBoxEntry.SelectionCellElement<Identifier> create(Identifier selection) {
                    ItemStack s = new ItemStack(Registries.ITEM.get(selection));
                    return new DropdownBoxEntry.DefaultSelectionCellElement<Identifier>(selection, toTextFunction) {
                        @Override
                        public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b)
                                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, toTextFunction.apply(r).asOrderedText(), x + 6 + 18, y + 6, b ? 16777215 : 8947848);
                            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                            graphics.drawItem(s, x + 4, y + 2);
                        }
                    };
                }
                
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        
        public static SelectionCellCreator<Identifier> ofBlockIdentifier() {
            return ofBlockIdentifier(20, 146, 7);
        }
        
        public static SelectionCellCreator<Identifier> ofBlockIdentifier(int maxItems) {
            return ofBlockIdentifier(20, 146, maxItems);
        }
        
        public static SelectionCellCreator<Identifier> ofBlockIdentifier(int cellHeight, int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<Identifier>() {
                @Override
                public DropdownBoxEntry.SelectionCellElement<Identifier> create(Identifier selection) {
                    ItemStack s = new ItemStack(Registries.BLOCK.get(selection));
                    return new DropdownBoxEntry.DefaultSelectionCellElement<Identifier>(selection, toTextFunction) {
                        @Override
                        public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b)
                                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, toTextFunction.apply(r).asOrderedText(), x + 6 + 18, y + 6, b ? 16777215 : 8947848);
                            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                            graphics.drawItem(s, x + 4, y + 2);
                        }
                    };
                }
                
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static SelectionCellCreator<Item> ofItemObject() {
            return ofItemObject(20, 146, 7);
        }
        
        public static SelectionCellCreator<Item> ofItemObject(int maxItems) {
            return ofItemObject(20, 146, maxItems);
        }
        
        public static SelectionCellCreator<Item> ofItemObject(int cellHeight, int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<Item>(i -> Text.literal(Registries.ITEM.getId(i).toString())) {
                @Override
                public DropdownBoxEntry.SelectionCellElement<Item> create(Item selection) {
                    ItemStack s = new ItemStack(selection);
                    return new DropdownBoxEntry.DefaultSelectionCellElement<Item>(selection, toTextFunction) {
                        @Override
                        public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b)
                                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, toTextFunction.apply(r).asOrderedText(), x + 6 + 18, y + 6, b ? 16777215 : 8947848);
                            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                            graphics.drawItem(s, x + 4, y + 2);
                        }
                    };
                }
                
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
        
        public static SelectionCellCreator<Block> ofBlockObject() {
            return ofBlockObject(20, 146, 7);
        }
        
        public static SelectionCellCreator<Block> ofBlockObject(int maxItems) {
            return ofBlockObject(20, 146, maxItems);
        }
        
        public static SelectionCellCreator<Block> ofBlockObject(int cellHeight, int cellWidth, int maxItems) {
            return new DefaultSelectionCellCreator<Block>(i -> Text.literal(Registries.BLOCK.getId(i).toString())) {
                @Override
                public DropdownBoxEntry.SelectionCellElement<Block> create(Block selection) {
                    ItemStack s = new ItemStack(selection);
                    return new DropdownBoxEntry.DefaultSelectionCellElement<Block>(selection, toTextFunction) {
                        @Override
                        public void render(DrawContext graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b)
                                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, toTextFunction.apply(r).asOrderedText(), x + 6 + 18, y + 6, b ? 16777215 : 8947848);
                            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                            graphics.drawItem(s, x + 4, y + 2);
                        }
                    };
                }
                
                @Override
                public int getCellHeight() {
                    return cellHeight;
                }
                
                @Override
                public int getCellWidth() {
                    return cellWidth;
                }
                
                @Override
                public int getDropBoxMaxHeight() {
                    return getCellHeight() * maxItems;
                }
            };
        }
    }
}
