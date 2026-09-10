package com.carloshdzz22.zombieinfection.infection;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Server-local, per-player feedback throttle. No saved or client-writable state. */
public final class InfectionFeedback {
    private static final AttachmentType<Long> NEXT_NOTICE = AttachmentRegistry.create(
            ZombieInfection.id("next_infection_notice"), builder -> builder.initializer(() -> 0L));

    private InfectionFeedback() { }
    public static void initialize() { }

    public static boolean notice(ServerPlayer player, String key, Object... arguments) {
        long now = InfectionManager.serverTime(player);
        if (now < player.getAttachedOrElse(NEXT_NOTICE, 0L)) return false;
        treatment(player, key, arguments);
        return true;
    }

    /** Successful deliberate treatments take priority over recent environmental notices. */
    public static void treatment(ServerPlayer player, String key, Object... arguments) {
        long now = InfectionManager.serverTime(player);
        player.setAttached(NEXT_NOTICE, now > Long.MAX_VALUE - 40 ? Long.MAX_VALUE : now + 40);
        player.displayClientMessage(Component.translatable(key, arguments), true);
    }
}
