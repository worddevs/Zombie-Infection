package com.carloshdzz22.zombieinfection.client.renderer;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.network.chat.Component;

/** Client-only snapshot; rendering never needs to retain a live entity. */
public final class InfectedRenderState extends ZombieRenderState {
    public boolean showHealth;
    public boolean damageFlash;
    public float healthOpacity;
    public float trailingHealth;
    public float health;
    public float maxHealth;
    public String healthText = "";
    public Component infectedName = Component.empty();
}
