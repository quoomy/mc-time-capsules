package com.quoomy.timecapsules;

import com.quoomy.timecapsules.item.timecapsulepainting.TimeCapsulePaintingEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TimecapsulesClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(ModRegistrations.TIME_CAPSULE_PAINTING_ENTITY_ENTITY_TYPE, TimeCapsulePaintingEntityRenderer::new);
    }
}
