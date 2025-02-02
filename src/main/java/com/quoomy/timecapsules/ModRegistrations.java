package com.quoomy.timecapsules;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModRegistrations
{
    // COMPONENTS
    // TimeCapsuleItem
    public static final ComponentType<Integer> TIME_CAPSULE_ID_COMPONENT = ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_FETCH_ATTEMPTED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_IS_SENDING_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_SEND_ATTEMPTED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<Boolean> TIME_CAPSULE_DATA_DONE = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build();
    public static final ComponentType<String> TIME_CAPSULE_SEND_DATA_TEXT = ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build();
    public static final ComponentType<String> TIME_CAPSULE_SEND_DATA_SIGNATURE = ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build();

    // ITEMS
    public static final Item TIME_CAPSULE_ITEM = registerItem("time_capsule", TimeCapsuleItem::new, new Item.Settings().fireproof().maxCount(1));

    // ITEM GROUPS
    public static final ItemGroup ingredients_group = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Timecapsules.MOD_ID, "ingredients_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.ingredients_group"))
                    .icon(() -> new ItemStack(TIME_CAPSULE_ITEM)).entries((displayContext, entries) -> {
                        ItemStack stack1 = new ItemStack(TIME_CAPSULE_ITEM);
                        stack1.set(TIME_CAPSULE_IS_SENDING_COMPONENT, false);
                        ItemStack stack2 = new ItemStack(TIME_CAPSULE_ITEM);
                        stack2.set(TIME_CAPSULE_IS_SENDING_COMPONENT, true);
                        entries.add(stack1);
                        entries.add(stack2);
                    }).build());

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
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_data_done"), TIME_CAPSULE_DATA_DONE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_send_data_text"), TIME_CAPSULE_SEND_DATA_TEXT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Timecapsules.MOD_ID, "time_capsule_send_data_signature"), TIME_CAPSULE_SEND_DATA_SIGNATURE);
    }
}
