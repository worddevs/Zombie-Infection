package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.screen.MedicalLaboratoryMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final MenuType<MedicalLaboratoryMenu> MEDICAL_LABORATORY_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ZombieInfection.id("medical_laboratory"),
            new MenuType<>(MedicalLaboratoryMenu::new, FeatureFlags.VANILLA_SET)
    );

    private ModMenuTypes() {}

    public static void initialize() {
        ZombieInfection.LOGGER.info("Registered Zombie Infection menu types");
    }
}
