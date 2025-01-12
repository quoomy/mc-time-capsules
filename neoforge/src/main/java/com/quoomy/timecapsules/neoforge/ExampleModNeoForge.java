package com.quoomy.timecapsules.neoforge;

import net.neoforged.fml.common.Mod;

import com.quoomy.timecapsules.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
