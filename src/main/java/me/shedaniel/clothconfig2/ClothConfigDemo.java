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

package me.shedaniel.clothconfig2;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import java.util.*;
import java.util.stream.Collectors;

public class ClothConfigDemo {
    public static ConfigBuilder getConfigBuilderWithDemo() {
        class Pair<T, R> {
            final T t;
            final R r;
            
            public Pair(T t, R r) {
                this.t = t;
                this.r = r;
            }
            
            public T getLeft() {
                return t;
            }
            
            public R getRight() {
                return r;
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                
                Pair<?, ?> pair = (Pair<?, ?>) o;
                
                if (!Objects.equals(t, pair.t)) return false;
                return Objects.equals(r, pair.r);
            }
            
            @Override
            public int hashCode() {
                int result = t != null ? t.hashCode() : 0;
                result = 31 * result + (r != null ? r.hashCode() : 0);
                return result;
            }
        }
    
        enum DependencyDemoEnum {
            EXCELLENT, GOOD, OKAY, BAD, HORRIBLE
        }
        
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("title.cloth-config.config"));
        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/oak_planks.png"));
        builder.setGlobalized(true);
        builder.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory testing = builder.getOrCreateCategory(Text.translatable("category.cloth-config.testing"));
        testing.addEntry(entryBuilder.startKeyCodeField(Text.literal("Cool Key"), InputUtil.UNKNOWN_KEY).setDefaultValue(InputUtil.UNKNOWN_KEY).build());
        testing.addEntry(entryBuilder.startModifierKeyCodeField(Text.literal("Cool Modifier Key"), ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(79), Modifier.of(false, true, false))).setDefaultValue(ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(79), Modifier.of(false, true, false))).build());
        testing.addEntry(entryBuilder.startDoubleList(Text.literal("A list of Doubles"), Arrays.asList(1d, 2d, 3d)).setDefaultValue(Arrays.asList(1d, 2d, 3d)).build());
        testing.addEntry(entryBuilder.startLongList(Text.literal("A list of Longs"), Arrays.asList(1L, 2L, 3L)).setDefaultValue(Arrays.asList(1L, 2L, 3L)).setInsertButtonEnabled(false).build());
        testing.addEntry(entryBuilder.startStrList(Text.literal("A list of Strings"), Arrays.asList("abc", "xyz")).setTooltip(Text.literal("Yes this is some beautiful tooltip\nOh and this is the second line!")).setDefaultValue(Arrays.asList("abc", "xyz")).build());
        SubCategoryBuilder colors = entryBuilder.startSubCategory(Text.literal("Colors")).setExpanded(true);
        colors.add(entryBuilder.startColorField(Text.literal("A color field"), 0x00ffff).setDefaultValue(0x00ffff).build());
        colors.add(entryBuilder.startColorField(Text.literal("An alpha color field"), 0xff00ffff).setDefaultValue(0xff00ffff).setAlphaMode(true).build());
        colors.add(entryBuilder.startColorField(Text.literal("An alpha color field"), 0xffffffff).setDefaultValue(0xffff0000).setAlphaMode(true).build());
        colors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        colors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        colors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        colors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        colors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        SubCategoryBuilder innerColors = entryBuilder.startSubCategory(Text.literal("Inner Colors")).setExpanded(true);
        innerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        innerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        innerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        SubCategoryBuilder innerInnerColors = entryBuilder.startSubCategory(Text.literal("Inner Inner Colors")).setExpanded(true);
        innerInnerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        innerInnerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        innerInnerColors.add(entryBuilder.startDropdownMenu(Text.literal("lol apple"), DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(Items.APPLE), DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()).setDefaultValue(Items.APPLE).setSelections(Registries.ITEM.stream().sorted(Comparator.comparing(Item::toString)).collect(Collectors.toCollection(LinkedHashSet::new))).setSaveConsumer(item -> System.out.println("save this " + item)).build());
        innerColors.add(innerInnerColors.build());
        colors.add(innerColors.build());
        testing.addEntry(colors.build());
        testing.addEntry(entryBuilder.startDropdownMenu(Text.literal("Suggestion Random Int"), DropdownMenuBuilder.TopCellElementBuilder.of(10,
                s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        
                    }
                    return null;
                })).setDefaultValue(10).setSelections(Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).build());
        testing.addEntry(entryBuilder.startDropdownMenu(Text.literal("Selection Random Int"), DropdownMenuBuilder.TopCellElementBuilder.of(10,
                s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        
                    }
                    return null;
                })).setDefaultValue(5).setSuggestionMode(false).setSelections(Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).build());
        testing.addEntry(new NestedListListEntry<Pair<Integer, Integer>, MultiElementListEntry<Pair<Integer, Integer>>>(
                Text.literal("Nice"),
                Lists.newArrayList(new Pair<>(10, 10), new Pair<>(20, 40)),
                false,
                Optional::empty,
                list -> {},
                () -> Lists.newArrayList(new Pair<>(10, 10), new Pair<>(20, 40)),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (elem, nestedListListEntry) -> {
                    if (elem == null) {
                        Pair<Integer, Integer> newDefaultElemValue = new Pair<>(10, 10);
                        return new MultiElementListEntry<>(Text.literal("Pair"), newDefaultElemValue,
                                Lists.newArrayList(entryBuilder.startIntField(Text.literal("Left"), newDefaultElemValue.getLeft()).setDefaultValue(10).build(),
                                        entryBuilder.startIntField(Text.literal("Right"), newDefaultElemValue.getRight()).setDefaultValue(10).build()),
                                true);
                    } else {
                        return new MultiElementListEntry<>(Text.literal("Pair"), elem,
                                Lists.newArrayList(entryBuilder.startIntField(Text.literal("Left"), elem.getLeft()).setDefaultValue(10).build(),
                                        entryBuilder.startIntField(Text.literal("Right"), elem.getRight()).setDefaultValue(10).build()),
                                true);
                    }
                }
        ));
        
        SubCategoryBuilder depends = entryBuilder.startSubCategory(Text.literal("Dependencies")).setExpanded(true);
        BooleanListEntry dependency = entryBuilder.startBooleanToggle(Text.literal("A cool toggle"), false).setTooltip(Text.literal("Toggle me...")).build();
        depends.add(dependency);
        Collection<BooleanListEntry> toggles = new LinkedList<>();
        toggles.add(entryBuilder.startBooleanToggle(Text.literal("I only work when cool is toggled..."), true)
                .setRequirement(Requirement.isTrue(dependency)).build());
        toggles.add(entryBuilder.startBooleanToggle(Text.literal("I only appear when cool is toggled..."), true)
                .setDisplayRequirement(Requirement.isTrue(dependency)).build());
        depends.addAll(toggles);
        depends.add(entryBuilder.startBooleanToggle(Text.literal("I only work when cool matches both of these toggles ^^"), true)
                .setRequirement(Requirement.all(
                        toggles.stream()
                                .map(toggle -> Requirement.matches(dependency, toggle))
                                .toArray(Requirement[]::new)))
                .build());
        SubCategoryBuilder dependantSub = entryBuilder.startSubCategory(Text.literal("Sub-categories can have requirements too..."))
                .setRequirement(Requirement.isTrue(dependency));
        dependantSub.add(entryBuilder.startTextDescription(Text.literal("This sub category depends on Cool being toggled")).build());
        dependantSub.add(entryBuilder.startBooleanToggle(Text.literal("Example entry"), true).build());
        dependantSub.add(entryBuilder.startBooleanToggle(Text.literal("Another example..."), true).build());
        depends.add(dependantSub.build());
        depends.add(entryBuilder.startLongList(Text.literal("Even lists!"), Arrays.asList(1L, 2L, 3L)).setDefaultValue(Arrays.asList(1L, 2L, 3L))
                .setRequirement(Requirement.isTrue(dependency)).build());
        EnumListEntry<DependencyDemoEnum> enumDependency = entryBuilder.startEnumSelector(Text.literal("Select a good or bad option"), DependencyDemoEnum.class, DependencyDemoEnum.OKAY).build();
        depends.add(enumDependency);
        IntegerSliderEntry intDependency = entryBuilder.startIntSlider(Text.literal("Select something big or small"), 50, -100, 100).build();
        depends.add(intDependency);
        depends.add(entryBuilder.startBooleanToggle(Text.literal("I only work when a good option is chosen..."), true).setTooltip(Text.literal("Select good or better above"))
                .setRequirement(Requirement.isValue(enumDependency, DependencyDemoEnum.EXCELLENT, DependencyDemoEnum.GOOD))
                .build());
        depends.add(entryBuilder.startBooleanToggle(Text.literal("I need a good option AND a cool toggle!"), true).setTooltip(Text.literal("Select good or better and also toggle cool"))
                .setRequirement(Requirement.all(
                        Requirement.isTrue(dependency),
                        Requirement.isValue(enumDependency, DependencyDemoEnum.EXCELLENT, DependencyDemoEnum.GOOD)))
                .build());
        depends.add(entryBuilder.startBooleanToggle(Text.literal("I only work when numbers are extreme!"), true)
                .setTooltip(Text.literal("Move the slider..."))
                .setRequirement(Requirement.any(
                        () -> intDependency.getValue() < -70,
                        () -> intDependency.getValue() > 70))
                .build());
    
        testing.addEntry(depends.build());
       
        testing.addEntry(entryBuilder.startTextDescription(
                Text.translatable("text.cloth-config.testing.1",
                        Text.literal("ClothConfig").styled(s -> s.withBold(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(Util.make(new ItemStack(Items.PINK_WOOL), stack -> stack.setCustomName(Text.literal("(\u30FB\u2200\u30FB)")).addEnchantment(Enchantments.EFFICIENCY, 10)))))),
                        Text.translatable("text.cloth-config.testing.2").styled(s -> s.withColor(Formatting.BLUE).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("https://shedaniel.gitbook.io/cloth-config/"))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://shedaniel.gitbook.io/cloth-config/"))),
                        Text.translatable("text.cloth-config.testing.3").styled(s -> s.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Utils.getConfigFolder().getParent().resolve("options.txt").toString())))
                )
        ).build());
        builder.transparentBackground();
        return builder;
    }
}
