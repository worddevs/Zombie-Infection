package com.carloshdzz22.zombieinfection.outbreak;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/** The level still derives from time. Only the highest announced threshold is saved in the Overworld. */
public final class OutbreakNotifications {
    public static final AttachmentType<Integer> HIGHEST_ANNOUNCED = AttachmentRegistry.create(
            ZombieInfection.id("highest_announced_outbreak"), builder -> builder.persistent(Codec.intRange(0, 4)));

    private OutbreakNotifications() { }

    public static void initialize() {
        // Baseline existing worlds quietly; loading a server is not crossing a threshold.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> baseline(server.overworld()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            ServerLevel level = server.overworld();
            OutbreakLevel stage = OutbreakLevel.from(level);
            if (recordIncrease(level, stage.index())) {
                Component message = Component.translatable("message.zombie-infection.outbreak_increased",
                        stage.index(), Component.translatable(stage.nameKey()));
                // One compact chat notice avoids replacing treatment feedback in the action bar.
                for (var player : server.getPlayerList().getPlayers()) player.sendSystemMessage(message);
            }
        });
    }

    public static void baseline(ServerLevel overworld) {
        if (!overworld.hasAttached(HIGHEST_ANNOUNCED)) {
            overworld.setAttached(HIGHEST_ANNOUNCED, OutbreakLevel.from(overworld).index());
        }
    }

    public static boolean recordIncrease(ServerLevel overworld, int index) {
        baseline(overworld);
        int bounded = Math.clamp(index, 0, 4);
        if (bounded <= overworld.getAttachedOrElse(HIGHEST_ANNOUNCED, 0)) return false;
        overworld.setAttached(HIGHEST_ANNOUNCED, bounded);
        return true;
    }
}
