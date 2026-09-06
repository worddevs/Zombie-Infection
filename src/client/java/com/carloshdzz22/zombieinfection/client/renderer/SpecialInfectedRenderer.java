package com.carloshdzz22.zombieinfection.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public final class SpecialInfectedRenderer<T extends Zombie>
        extends AbstractZombieRenderer<T, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    private final ResourceLocation texture;
    private final float visualScale;

    public SpecialInfectedRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
                                   float visualScale) {
        super(context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY_OUTER_ARMOR)));
        this.texture = texture;
        this.visualScale = visualScale;
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieRenderState state) {
        return texture;
    }

    @Override
    protected void scale(ZombieRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(visualScale, visualScale, visualScale);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
