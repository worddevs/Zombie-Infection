package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.item.ExtractionKitItem;
import com.carloshdzz22.zombieinfection.item.AdrenalineInjectorItem;
import com.carloshdzz22.zombieinfection.item.InfectionSuppressantItem;
import com.carloshdzz22.zombieinfection.item.InfectionMedicineItem;
import com.carloshdzz22.zombieinfection.item.TooltipItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Function;

public final class ModItems {
	public static final Item INFECTED_SAMPLE = registerMaterial("infected_sample", 64);
	public static final Item EXTRACTION_KIT = register("extraction_kit", properties -> new ExtractionKitItem(properties.durability(32)), 1);
	public static final Item INFECTED_TISSUE = registerMaterial("infected_tissue", 64);
	public static final Item INFECTED_BLOOD_SAMPLE = registerMaterial("infected_blood_sample", 64);
	public static final Item VIRAL_EXTRACT = register("viral_extract", properties -> new com.carloshdzz22.zombieinfection.item.TooltipItem(properties, "item.zombie-infection.viral_extract.desc"), 64);
	public static final Item ANTIVIRAL_COMPOUND = register("antiviral_compound", properties -> new com.carloshdzz22.zombieinfection.item.TooltipItem(properties, "item.zombie-infection.antiviral_compound.desc"), 64);
	public static final Item RUNNER_ADRENAL_GLAND = registerMaterial("runner_adrenal_gland", 16);
	public static final Item BLOATER_TOXIN_SAC = registerMaterial("bloater_toxin_sac", 16);
	public static final Item SPITTER_ENZYME = registerMaterial("spitter_enzyme", 16);
	public static final Item ADRENALINE_INJECTOR = register(
			"adrenaline_injector", AdrenalineInjectorItem::new, 8);
	public static final Item INFECTION_SUPPRESSANT = register(
			"infection_suppressant", InfectionSuppressantItem::new, 8);
	public static final Item MEDICAL_LABORATORY = register("medical_laboratory", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.MEDICAL_LABORATORY, properties), 64);
	public static final Item INFECTED_SPAWN_EGG = register("infected_spawn_egg",
			properties -> new SpawnEggItem(ModEntities.INFECTED, properties), 64);
	public static final Item RUNNER_SPAWN_EGG = register("runner_spawn_egg",
			properties -> new SpawnEggItem(ModEntities.RUNNER, properties), 64);
	public static final Item BLOATER_SPAWN_EGG = register("bloater_spawn_egg",
			properties -> new SpawnEggItem(ModEntities.BLOATER, properties), 64);
	public static final Item SPITTER_SPAWN_EGG = register("spitter_spawn_egg",
			properties -> new SpawnEggItem(ModEntities.SPITTER, properties), 64);

	public static final Item BASIC_ANTIVIRAL = register(
			"basic_antiviral", properties -> new InfectionMedicineItem(properties, 15, false), 16);
	public static final Item ANTIVIRAL_INJECTION = register(
			"antiviral_injection", properties -> new InfectionMedicineItem(properties, 35, false), 16);
	public static final Item CURE = register(
			"cure", properties -> new InfectionMedicineItem(properties, 100, true), 16);
	public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, ZombieInfection.id("items"));
	public static final CreativeModeTab ITEM_GROUP = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			ITEM_GROUP_KEY,
			FabricItemGroup.builder()
					.title(Component.translatable("itemGroup.zombie-infection.items"))
					.icon(() -> new ItemStack(INFECTED_SAMPLE))
					.displayItems((parameters, output) -> addAllItems(output))
					.build()
	);

	private ModItems() {
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
				.register(entries -> entries.accept(INFECTED_SAMPLE));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.accept(BASIC_ANTIVIRAL);
			entries.accept(ANTIVIRAL_INJECTION);
			entries.accept(CURE);
		});

		ZombieInfection.LOGGER.info("Registered Zombie Infection items, including extraction and laboratory content");
	}

	private static void addAllItems(CreativeModeTab.Output output) {
		output.accept(INFECTED_SAMPLE);
		output.accept(EXTRACTION_KIT);
		output.accept(INFECTED_TISSUE);
		output.accept(INFECTED_BLOOD_SAMPLE);
		output.accept(VIRAL_EXTRACT);
		output.accept(ANTIVIRAL_COMPOUND);
		output.accept(RUNNER_ADRENAL_GLAND);
		output.accept(BLOATER_TOXIN_SAC);
		output.accept(SPITTER_ENZYME);
		output.accept(ADRENALINE_INJECTOR);
		output.accept(INFECTION_SUPPRESSANT);
		output.accept(MEDICAL_LABORATORY);
		output.accept(INFECTED_SPAWN_EGG);
		output.accept(RUNNER_SPAWN_EGG);
		output.accept(BLOATER_SPAWN_EGG);
		output.accept(SPITTER_SPAWN_EGG);
		output.accept(BASIC_ANTIVIRAL);
		output.accept(ANTIVIRAL_INJECTION);
		output.accept(CURE);
	}

	private static Item registerMaterial(String path, int maxStackSize) {
		return register(path, properties -> new TooltipItem(properties,
				"item.zombie-infection." + path + ".desc"), maxStackSize);
	}

	private static Item register(String path, Function<Item.Properties, Item> factory, int maxStackSize) {
		ResourceLocation id = ZombieInfection.id(path);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		Item item = factory.apply(new Item.Properties().setId(key).stacksTo(maxStackSize));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
