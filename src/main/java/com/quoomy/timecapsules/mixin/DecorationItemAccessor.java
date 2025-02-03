package com.quoomy.timecapsules.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.item.DecorationItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DecorationItem.class)
public interface DecorationItemAccessor
{
    @Accessor("entityType")
    EntityType<?> getEntityType();
}
