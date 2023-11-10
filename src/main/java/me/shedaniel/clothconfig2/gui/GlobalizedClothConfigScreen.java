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

package me.shedaniel.clothconfig2.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.clothconfig2.gui.entries.EmptyEntry;
import me.shedaniel.clothconfig2.gui.widget.SearchFieldEntry;
import me.shedaniel.math.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Supplier;

public class GlobalizedClothConfigScreen extends AbstractConfigScreen implements ReferenceBuildingConfigScreen, Expandable {
    public ClothConfigScreen.ListWidget<AbstractConfigEntry<AbstractConfigEntry<?>>> listWidget;
    private ClickableWidget cancelButton, exitButton;
    private final LinkedHashMap<Text, List<AbstractConfigEntry<?>>> categorizedEntries = Maps.newLinkedHashMap();
    private final ScrollingContainer sideScroller = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return new Rectangle(4, 4, getSideSliderPosition() - 14 - 4, height - 8);
        }
        
        @Override
        public int getMaxScrollHeight() {
            int i = 0;
            for (Reference reference : references) {
                if (i != 0) i += 3 * reference.getScale();
                i += textRenderer.fontHeight * reference.getScale();
            }
            return i;
        }
    };
    private Reference lastHoveredReference = null;
    private SearchFieldEntry searchFieldEntry;
    private final ScrollingContainer sideSlider = new ScrollingContainer() {
        private final Rectangle empty = new Rectangle();
        
        @Override
        public Rectangle getBounds() {
            return empty;
        }
        
        @Override
        public int getMaxScrollHeight() {
            return 1;
        }
    };
    private final List<Reference> references = Lists.newArrayList();
    private final LazyResettable<Integer> sideExpandLimit = new LazyResettable<>(() -> {
        int max = 0;
        for (Reference reference : references) {
            Text referenceText = reference.getText();
            int width = textRenderer.getWidth(Text.literal(StringUtils.repeat("  ", reference.getIndent()) + "- ").append(referenceText));
            if (width > max) max = width;
        }
        return Math.min(max + 8, width / 4);
    });
    private boolean requestingReferenceRebuilding = false;
    
    @ApiStatus.Internal
    public GlobalizedClothConfigScreen(Screen parent, Text title, Map<String, ConfigCategory> categoryMap, Identifier backgroundLocation) {
        super(parent, title, backgroundLocation);
        categoryMap.forEach((categoryName, category) -> {
            List<AbstractConfigEntry<?>> entries = Lists.newArrayList();
            for (Object object : category.getEntries()) {
                AbstractConfigListEntry<?> entry;
                if (object instanceof Pair<?, ?>) {
                    entry = (AbstractConfigListEntry<?>) ((Pair<?, ?>) object).getRight();
                } else {
                    entry = (AbstractConfigListEntry<?>) object;
                }
                entry.setScreen(this);
                entries.add(entry);
            }
            categorizedEntries.put(category.getCategoryKey(), entries);
        });
        this.sideSlider.scrollTo(0, false);
    }
    
    @Override
    public void requestReferenceRebuilding() {
        this.requestingReferenceRebuilding = true;
    }
    
    @Override
    public Map<Text, List<AbstractConfigEntry<?>>> getCategorizedEntries() {
        return this.categorizedEntries;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    protected void init() {
        super.init();
        this.sideExpandLimit.reset();
        this.references.clear();
        buildReferences();
        this.addSelectableChild(listWidget = new ClothConfigScreen.ListWidget<>(this, client, width - 14, height, 30, height - 32, getBackgroundLocation()));
        this.listWidget.setLeftPos(14);
        this.listWidget.children().add((AbstractConfigEntry) new EmptyEntry(5));
        this.listWidget.children().add((AbstractConfigEntry) (searchFieldEntry = new SearchFieldEntry(this, listWidget)));
        this.listWidget.children().add((AbstractConfigEntry) new EmptyEntry(5));
        this.categorizedEntries.forEach((category, entries) -> {
            if (!listWidget.children().isEmpty())
                this.listWidget.children().add((AbstractConfigEntry) new EmptyEntry(5));
            this.listWidget.children().add((AbstractConfigEntry) new EmptyEntry(4));
            this.listWidget.children().add((AbstractConfigEntry) new CategoryTextEntry(category, category.copy().formatted(Formatting.BOLD)));
            this.listWidget.children().add((AbstractConfigEntry) new EmptyEntry(4));
            this.listWidget.children().addAll((List) entries);
        });
        int buttonWidths = Math.min(200, (width - 50 - 12) / 3);
        addDrawableChild(cancelButton = ButtonWidget.builder(isEdited() ? Text.translatable("text.cloth-config.cancel_discard") : Text.translatable("gui.cancel"), widget -> quit()).dimensions(0, height - 26, buttonWidths, 20).build());
        addDrawableChild(exitButton = new ButtonWidget(0, height - 26, buttonWidths, 20, Text.empty(), button -> saveAll(true), Supplier::get) {
            @Override
            public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
                boolean hasErrors = false;
                label:
                for (List<AbstractConfigEntry<?>> entries : categorizedEntries.values()) {
                    for (AbstractConfigEntry<?> entry : entries) {
                        if (entry.getConfigError().isPresent()) {
                            hasErrors = true;
                            break label;
                        }
                    }
                }
                active = isEdited() && !hasErrors;
                setMessage(hasErrors ? Text.translatable("text.cloth-config.error_cannot_save") : Text.translatable("text.cloth-config.save_and_done"));
                super.render(graphics, mouseX, mouseY, delta);
            }
        });
        Optional.ofNullable(this.afterInitConsumer).ifPresent(consumer -> consumer.accept(this));
    }
    
    @Override
    public boolean matchesSearch(Iterator<String> tags) {
        return searchFieldEntry.matchesSearch(tags);
    }
    
    private void buildReferences() {
        categorizedEntries.forEach((categoryText, entries) -> {
            this.references.add(new CategoryReference(categoryText));
            for (AbstractConfigEntry<?> entry : entries) buildReferenceFor(entry, 1);
        });
    }
    
    private void buildReferenceFor(AbstractConfigEntry<?> entry, int layer) {
        List<ReferenceProvider<?>> referencableEntries = entry.getReferenceProviderEntries();
        if (referencableEntries != null) {
            this.references.add(new ConfigEntryReference(entry, layer));
            for (ReferenceProvider<?> referencableEntry : referencableEntries) {
                buildReferenceFor(referencableEntry.provideReferenceEntry(), layer + 1);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        this.lastHoveredReference = null;
        if (requestingReferenceRebuilding) {
            this.references.clear();
            buildReferences();
            requestingReferenceRebuilding = false;
        }
        int sliderPosition = getSideSliderPosition();
        ScissorsHandler.INSTANCE.scissor(new Rectangle(sliderPosition, 0, width - sliderPosition, height));
        if (isTransparentBackground()) {
            graphics.fillGradient(14, 0, width, height, -1072689136, -804253680);
        } else {
            renderBackgroundTexture(graphics);
            overlayBackground(graphics, new Rectangle(14, 0, width, height), 64, 64, 64, 255, 255);
        }
        listWidget.width = width - sliderPosition;
        listWidget.setLeftPos(sliderPosition);
        listWidget.render(graphics, mouseX, mouseY, delta);
        ScissorsHandler.INSTANCE.scissor(new Rectangle(listWidget.left, listWidget.top, listWidget.width, listWidget.bottom - listWidget.top));
        for (AbstractConfigEntry<?> child : listWidget.children())
            child.lateRender(graphics, mouseX, mouseY, delta);
        ScissorsHandler.INSTANCE.removeLastScissor();
        graphics.drawTextWithShadow(textRenderer, title.asOrderedText(), (int) (sliderPosition + (width - sliderPosition) / 2f - textRenderer.getWidth(title) / 2f), 12, -1);
        ScissorsHandler.INSTANCE.removeLastScissor();
        cancelButton.setX(sliderPosition + (width - sliderPosition) / 2 - cancelButton.getWidth() - 3);
        exitButton.setX(sliderPosition + (width - sliderPosition) / 2 + 3);
        super.render(graphics, mouseX, mouseY, delta);
        sideSlider.updatePosition(delta);
        sideScroller.updatePosition(delta);
        if (isTransparentBackground()) {
            graphics.fillGradient(0, 0, sliderPosition, height, -1240461296, -972025840);
            graphics.fillGradient(0, 0, sliderPosition - 14, height, 1744830464, 1744830464);
        } else {
            Tessellator tesselator = Tessellator.getInstance();
            BufferBuilder buffer = tesselator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.setShaderTexture(0, getBackgroundLocation());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(sliderPosition - 14, height, 0.0D).texture(0, height / 32.0F).color(68, 68, 68, 255).next();
            buffer.vertex(sliderPosition, height, 0.0D).texture(14 / 32.0F, height / 32.0F).color(68, 68, 68, 255).next();
            buffer.vertex(sliderPosition, 0, 0.0D).texture(14 / 32.0F, 0).color(68, 68, 68, 255).next();
            buffer.vertex(sliderPosition - 14, 0, 0.0D).texture(0, 0).color(68, 68, 68, 255).next();
            
            buffer.vertex(0, height, 0.0D).texture(0, (height + sideScroller.scrollAmountInt()) / 32.0F).color(32, 32, 32, 255).next();
            buffer.vertex(sliderPosition - 14, height, 0.0D).texture((sliderPosition - 14) / 32.0F, (height + sideScroller.scrollAmountInt()) / 32.0F).color(32, 32, 32, 255).next();
            buffer.vertex(sliderPosition - 14, 0, 0.0D).texture((sliderPosition - 14) / 32.0F, sideScroller.scrollAmountInt() / 32.0F).color(32, 32, 32, 255).next();
            buffer.vertex(0, 0, 0.0D).texture(0, sideScroller.scrollAmountInt() / 32.0F).color(32, 32, 32, 255).next();
            tesselator.draw();
        }
        {
            Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Tessellator tesselator = Tessellator.getInstance();
            BufferBuilder buffer = tesselator.getBuffer();
            int shadeColor = isTransparentBackground() ? 120 : 160;
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, sliderPosition + 4, 0, 100.0F).color(0, 0, 0, 0).next();
            buffer.vertex(matrix, sliderPosition, 0, 100.0F).color(0, 0, 0, shadeColor).next();
            buffer.vertex(matrix, sliderPosition, height, 100.0F).color(0, 0, 0, shadeColor).next();
            buffer.vertex(matrix, sliderPosition + 4, height, 100.0F).color(0, 0, 0, 0).next();
            tesselator.draw();
            shadeColor /= 2;
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, sliderPosition - 14, 0, 100.0F).color(0, 0, 0, shadeColor).next();
            buffer.vertex(matrix, sliderPosition - 14 - 4, 0, 100.0F).color(0, 0, 0, 0).next();
            buffer.vertex(matrix, sliderPosition - 14 - 4, height, 100.0F).color(0, 0, 0, 0).next();
            buffer.vertex(matrix, sliderPosition - 14, height, 100.0F).color(0, 0, 0, shadeColor).next();
            tesselator.draw();
            RenderSystem.disableBlend();
        }
        Rectangle slideArrowBounds = new Rectangle(sliderPosition - 14, 0, 14, height);
        {
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            textRenderer.drawLayer(">", sliderPosition - 7 - textRenderer.getWidth(">") / 2f, height / 2, (slideArrowBounds.contains(mouseX, mouseY) ? 16777120 : 16777215) | MathHelper.clamp(MathHelper.ceil((1 - sideSlider.scrollAmount()) * 255.0F), 0, 255) << 24, false, graphics.getMatrices().peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            textRenderer.drawLayer("<", sliderPosition - 7 - textRenderer.getWidth("<") / 2f, height / 2, (slideArrowBounds.contains(mouseX, mouseY) ? 16777120 : 16777215) | MathHelper.clamp(MathHelper.ceil(sideSlider.scrollAmount() * 255.0F), 0, 255) << 24, false, graphics.getMatrices().peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            immediate.draw();
            
            Rectangle scrollerBounds = sideScroller.getBounds();
            if (!scrollerBounds.isEmpty()) {
                ScissorsHandler.INSTANCE.scissor(new Rectangle(0, 0, sliderPosition - 14, height));
                int scrollOffset = scrollerBounds.y - sideScroller.scrollAmountInt();
                for (Reference reference : references) {
                    graphics.getMatrices().push();
                    graphics.getMatrices().scale(reference.getScale(), reference.getScale(), reference.getScale());
                    MutableText text = Text.literal(StringUtils.repeat("  ", reference.getIndent()) + "- ").append(reference.getText());
                    if (lastHoveredReference == null && new Rectangle(scrollerBounds.x, (int) (scrollOffset - 4 * reference.getScale()), (int) (textRenderer.getWidth(text) * reference.getScale()), (int) ((textRenderer.fontHeight + 4) * reference.getScale())).contains(mouseX, mouseY))
                        lastHoveredReference = reference;
                    graphics.drawText(textRenderer, text.asOrderedText(), scrollerBounds.x, scrollOffset, lastHoveredReference == reference ? 16769544 : 16777215, false);
                    graphics.getMatrices().pop();
                    scrollOffset += (textRenderer.fontHeight + 3) * reference.getScale();
                }
                ScissorsHandler.INSTANCE.removeLastScissor();
                sideScroller.renderScrollBar(graphics);
            }
        }
    }
    
    @Override
    public void renderBackground(DrawContext guiGraphics, int i, int j, float f) {
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Rectangle slideBounds = new Rectangle(0, 0, getSideSliderPosition() - 14, height);
        if (button == 0 && slideBounds.contains(mouseX, mouseY) && lastHoveredReference != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            lastHoveredReference.go();
            return true;
        }
        Rectangle slideArrowBounds = new Rectangle(getSideSliderPosition() - 14, 0, 14, height);
        if (button == 0 && slideArrowBounds.contains(mouseX, mouseY)) {
            setExpanded(!isExpanded());
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean isExpanded() {
        return sideSlider.scrollTarget() == 1;
    }
    
    @Override
    public void setExpanded(boolean expanded) {
        this.sideSlider.scrollTo(expanded ? 1 : 0, true, 2000);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        Rectangle slideBounds = new Rectangle(0, 0, getSideSliderPosition() - 14, height);
        if (amountY != 0 && slideBounds.contains(mouseX, mouseY)) {
            sideScroller.offset(ClothConfigInitializer.getScrollStep() * -amountY, true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
    }
    
    private int getSideSliderPosition() {
        return (int) (sideSlider.scrollAmount() * sideExpandLimit.get() + 14);
    }
    
    private static class CategoryTextEntry extends AbstractConfigListEntry<Object> {
        private final Text category;
        private final Text text;
        
        public CategoryTextEntry(Text category, Text text) {
            super(Text.literal(UUID.randomUUID().toString()), false);
            this.category = category;
            this.text = text;
        }
        
        @Override
        public int getItemHeight() {
            List<OrderedText> strings = MinecraftClient.getInstance().textRenderer.wrapLines(text, getParent().getItemWidth());
            if (strings.isEmpty())
                return 0;
            return 4 + strings.size() * 10;
        }
        
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation focusNavigationEvent) {
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
        public boolean isMouseInside(int mouseX, int mouseY, int x, int y, int entryWidth, int entryHeight) {
            return false;
        }
        
        @Override
        public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
            int yy = y + 2;
            List<OrderedText> texts = MinecraftClient.getInstance().textRenderer.wrapLines(this.text, getParent().getItemWidth());
            for (OrderedText text : texts) {
                graphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x - 4 + entryWidth / 2 - MinecraftClient.getInstance().textRenderer.getWidth(text) / 2, yy, -1);
                yy += 10;
            }
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
    
    private interface Reference {
        default int getIndent() {
            return 0;
        }
        
        Text getText();
        
        float getScale();
        
        void go();
    }
    
    private class CategoryReference implements Reference {
        private final Text category;
        
        public CategoryReference(Text category) {
            this.category = category;
        }
        
        @Override
        public Text getText() {
            return category;
        }
        
        @Override
        public float getScale() {
            return 1.0F;
        }
        
        @Override
        public void go() {
            int i = 0;
            for (AbstractConfigEntry<?> child : listWidget.children()) {
                if (child instanceof CategoryTextEntry && ((CategoryTextEntry) child).category == category) {
                    listWidget.scrollTo(i, true);
                    return;
                }
                i += child.getItemHeight();
            }
        }
    }
    
    private class ConfigEntryReference implements Reference {
        private final AbstractConfigEntry<?> entry;
        private final int layer;
        
        public ConfigEntryReference(AbstractConfigEntry<?> entry, int layer) {
            this.entry = entry;
            this.layer = layer;
        }
        
        @Override
        public int getIndent() {
            return layer;
        }
        
        @Override
        public Text getText() {
            return entry.getFieldName();
        }
        
        @Override
        public float getScale() {
            return 1.0F;
        }
        
        @Override
        public void go() {
            int[] i = {0};
            for (AbstractConfigEntry<?> child : listWidget.children()) {
                int i1 = i[0];
                if (goChild(i, null, child)) return;
                i[0] = i1 + child.getItemHeight();
            }
        }
        
        private boolean goChild(int[] i, Integer expandedParent, AbstractConfigEntry<?> root) {
            if (root == entry) {
                listWidget.scrollTo(expandedParent == null ? i[0] : expandedParent, true);
                return true;
            }
            int j = i[0];
            i[0] += root.getInitialReferenceOffset();
            boolean expanded = root instanceof Expandable && ((Expandable) root).isExpanded();
            if (root instanceof Expandable) ((Expandable) root).setExpanded(true);
            List<? extends Element> children = root.children();
            if (root instanceof Expandable) ((Expandable) root).setExpanded(expanded);
            for (Element child : children) {
                if (child instanceof ReferenceProvider<?>) {
                    int i1 = i[0];
                    if (goChild(i, expandedParent != null ? expandedParent : root instanceof Expandable && !expanded ? j : null, ((ReferenceProvider<?>) child).provideReferenceEntry())) {
                        return true;
                    }
                    i[0] = i1 + ((ReferenceProvider<?>) child).provideReferenceEntry().getItemHeight();
                }
            }
            return false;
        }
    }
}
