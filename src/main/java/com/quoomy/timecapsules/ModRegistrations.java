package com.quoomy.timecapsules;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRegistrations
{
    public static final ComponentType<Integer> RECEIVED_TIME_CAPSULE_ID_COMPONENT = ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();

    public static final Item RECEIVED_TIME_CAPSULE_ITEM = registerItem("time_capsule", new ReceivedTimeCapsuleItem(new Item.Settings().fireproof().maxCount(1)));

    private static Item registerItem(String name, Item item)
    {
        return Registry.register(Registries.ITEM, Identifier.of(Timecapsules.MOD_ID, name), item);
    }

    public static void register()
    {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "received_time_capsule"), RECEIVED_TIME_CAPSULE_ID_COMPONENT);
    }
}
