package com.carloshdzz22.zombieinfection.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExtractionKitItem extends Item {
    public ExtractionKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
        tooltip.accept(net.minecraft.network.chat.Component.translatable("item.zombie-infection.extraction_kit.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
