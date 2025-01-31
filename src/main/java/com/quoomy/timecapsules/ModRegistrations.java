package com.quoomy.timecapsules;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModRegistrations
{
    public static final ComponentType<Integer> TIME_CAPSULE_ID_COMPONENT = ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_IS_SENDING_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_SEND_ATTEMPTED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();

    public static final Item RECEIVED_TIME_CAPSULE_ITEM = registerItem("time_capsule", TimeCapsuleItem::new, new Item.Settings().fireproof().maxCount(1));

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> constructor, Item.Settings settings)
    {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Timecapsules.MOD_ID, name));
        return Registry.register(Registries.ITEM, key, constructor.apply(settings.registryKey(key)));
    }

    public static void register()
    {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_id"), TIME_CAPSULE_ID_COMPONENT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_fetch_attempted"), TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_is_sending"), TIME_CAPSULE_IS_SENDING_COMPONENT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_send_attempted"), TIME_CAPSULE_SEND_ATTEMPTED_COMPONENT);
    }
}
