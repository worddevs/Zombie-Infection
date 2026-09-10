package com.carloshdzz22.zombieinfection.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public final class SpecialInfectedRenderer<T extends Zombie>
        extends AbstractZombieRenderer<T, InfectedRenderState, ZombieModel<InfectedRenderState>> {
    private final ResourceLocation texture;
    private final float visualScale;
    private final InfectedHealthBar healthBar = new InfectedHealthBar();

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
    public ResourceLocation getTextureLocation(InfectedRenderState state) {
        return texture;
    }

    @Override
    protected void scale(InfectedRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(visualScale, visualScale, visualScale);
    }

    @Override
    public InfectedRenderState createRenderState() {
        return new InfectedRenderState();
    }

    @Override
    public void extractRenderState(T entity, InfectedRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Our compact bar supplies the name with the same visibility restrictions.
        state.nameTag = null;
        healthBar.extract(entity, state, partialTick);
    }

    @Override
    public void render(InfectedRenderState state, PoseStack poses,
            net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight) {
        super.render(state, poses, buffers, packedLight);
        if (state.showHealth) {
            InfectedHealthBar.render(state, poses, buffers, getFont(),
                    entityRenderDispatcher.cameraOrientation(), visualScale);
        }
    }
}
