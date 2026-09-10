package com.carloshdzz22.zombieinfection.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import com.carloshdzz22.zombieinfection.client.config.HealthBarConfig;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Locale;

/** Only rendered, nearby entities are considered; there are no world-wide entity scans. */
final class InfectedHealthBar {
    private static final float WORLD_SCALE = 0.018F;
    private final Map<Zombie, DamageState> damageStates = new WeakHashMap<>();

    void extract(Zombie entity, InfectedRenderState state, float partialTick) {
        state.showHealth = false;
        Minecraft client = Minecraft.getInstance();
        var settings = HealthBarConfig.current();
        double rangeSquared = settings.range() * settings.range();
        if (!settings.enabled() || client.player == null || client.options.hideGui || !entity.isAlive()
                || entity.isInvisible() || client.player.distanceToSqr(entity) > rangeSquared
                || state.distanceToCameraSq > rangeSquared) {
            DamageState hidden = damageStates.get(entity);
            if (hidden != null) {
                hidden.opacity = 0;
            }
            return;
        }

        DamageState damage = damageStates.computeIfAbsent(entity, ignored -> new DamageState(entity.getHealth()));
        float now = entity.tickCount + partialTick;
        float elapsed = Mth.clamp(now - damage.lastFrame, 0, 1);
        if (now - damage.lastFrame > 10) {
            damage.opacity = 0;
        }
        damage.lastFrame = now;
        if (entity.getHealth() < damage.lastHealth || entity.hurtTime > damage.lastHurtTime) {
            damage.trailingHealth = Math.max(damage.trailingHealth, damage.lastHealth);
            damage.lastDamageTick = now;
        }
        float health = Math.max(0, entity.getHealth());
        float maxHealth = Math.max(0.001F, entity.getMaxHealth());
        if (health != damage.lastHealth || maxHealth != damage.lastMaxHealth || damage.healthText.isEmpty()) {
            damage.healthText = formatHealth(health) + " / " + formatHealth(maxHealth);
        }
        damage.lastMaxHealth = maxHealth;
        damage.lastHealth = entity.getHealth();
        damage.lastHurtTime = entity.hurtTime;
        boolean recentDamage = now - damage.lastDamageTick < settings.damageSeconds() * 20;
        var camera = client.gameRenderer.getMainCamera();
        Vec3 origin = camera.getPosition();
        boolean aimedAt = InfectedTargeting.isTarget(entity);
        // Show immediately; only leaving aim / expiring damage causes a short fade.
        damage.opacity = recentDamage || aimedAt ? 1 : Math.max(0,
                damage.opacity - elapsed / (float) (settings.fadeSeconds() * 20));
        // Font treats almost-zero alpha as an unspecified color; finish the fade before that.
        if (damage.opacity <= 0.025F) {
            damage.opacity = 0;
            return;
        }
        // Camera-based collision check also works in third person. Text/quads additionally
        // use depth testing, never the vanilla SEE_THROUGH name-tag pass.
        if (entity.level().clip(new ClipContext(origin, entity.getEyePosition(partialTick),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, client.player)).getType() != HitResult.Type.MISS) {
            damage.opacity = 0;
            return;
        }
        if (now - damage.lastDamageTick > 2) {
            damage.trailingHealth = Mth.lerp(Math.min(1, elapsed / 3), damage.trailingHealth, health);
        }
        damage.trailingHealth = Mth.clamp(damage.trailingHealth, health, maxHealth);
        state.showHealth = true;
        state.health = health;
        state.maxHealth = maxHealth;
        state.healthText = damage.healthText;
        state.trailingHealth = settings.damageTrail() ? damage.trailingHealth : health;
        state.healthOpacity = damage.opacity;
        state.infectedName = entity.hasCustomName() ? entity.getCustomName() : entity.getType().getDescription();
        state.damageFlash = settings.damageFlash() && now - damage.lastDamageTick < 3;
    }

    static void render(InfectedRenderState state, PoseStack poses, MultiBufferSource buffers,
            Font font, Quaternionf cameraOrientation, float visualScale) {
        poses.pushPose();
        // Bounding-box height already includes entity scaling. Do not multiply it by
        // the renderer's scale again (which used to place Bloater's bar too high).
        float modelHeight = EntityType.ZOMBIE.getHeight() * state.scale * state.ageScale * visualScale;
        poses.translate(0, Math.max(state.boundingBoxHeight, modelHeight) + 0.14, 0);
        poses.mulPose(cameraOrientation);
        // Fixed world scale: GUI Scale never makes the label jump or resize with distance.
        float scale = WORLD_SCALE * (float) HealthBarConfig.current().scale();
        poses.scale(scale, -scale, scale);
        Matrix4f matrix = poses.last().pose();
        int nameWidth = font.width(state.infectedName);
        int hpWidth = font.width(state.healthText);
        float halfWidth = Math.max(nameWidth + 12, 42 + hpWidth + 18) / 2.0F;
        float left = -halfWidth + 6;
        float barRight = halfWidth - hpWidth - 12;
        float barWidth = barRight - left;
        float ratio = Mth.clamp(state.health / state.maxHealth, 0, 1);
        int fill = ratio > 0.60F ? 0xFF83977A : ratio > 0.30F ? 0xFFB19A68 : 0xFFAC6B62;
        if (state.damageFlash) {
            fill = 0xFFCCB9A7;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.textBackground());
        float opacity = state.healthOpacity;
        quad(vertices, matrix, -halfWidth, -25, halfWidth, 0, 0, alpha(0xB0545B57, opacity));
        quad(vertices, matrix, -halfWidth + 1, -24, halfWidth - 1, -1, 0.01F, alpha(0xD0141918, opacity));
        quad(vertices, matrix, left - 1, -11, barRight + 1, -4, 0.02F, alpha(0xFF555D57, opacity));
        quad(vertices, matrix, left, -10, barRight, -5, 0.03F, alpha(0xFF2A302C, opacity));
        if (state.trailingHealth > state.health + 0.05F) {
            quad(vertices, matrix, left, -10, left + barWidth * state.trailingHealth / state.maxHealth,
                    -5, 0.04F, alpha(0xFFA4917B, opacity));
        }
        quad(vertices, matrix, left, -10, left + barWidth * ratio, -5, 0.05F, alpha(fill, opacity));
        // Keep both text rows in front of the panel, while retaining normal depth testing.
        poses.translate(0, 0, 0.06F);
        font.drawInBatch(state.infectedName, -nameWidth / 2.0F, -21, alpha(0xFFE3E5DF, opacity),
                false, poses.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        font.drawInBatch(state.healthText, halfWidth - hpWidth - 6, -12, alpha(0xFFD2D6CF, opacity),
                false, poses.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poses.popPose();
    }

    private static String formatHealth(float value) {
        return value == (int) value ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static int alpha(int color, float opacity) {
        return (Math.round((color >>> 24) * opacity) << 24) | (color & 0xFFFFFF);
    }

    private static void quad(VertexConsumer vertices, Matrix4f matrix, float left, float top,
            float right, float bottom, float depth, int color) {
        vertices.addVertex(matrix, left, top, depth).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        vertices.addVertex(matrix, left, bottom, depth).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        vertices.addVertex(matrix, right, bottom, depth).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        vertices.addVertex(matrix, right, top, depth).setColor(color).setLight(LightTexture.FULL_BRIGHT);
    }

    private static final class DamageState {
        float lastHealth;
        float lastMaxHealth;
        float trailingHealth;
        float opacity;
        float lastFrame = -10;
        String healthText = "";
        int lastHurtTime;
        float lastDamageTick = -Float.MAX_VALUE;

        DamageState(float health) {
            lastHealth = health;
            trailingHealth = health;
        }
    }
}
