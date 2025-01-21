package com.quoomy.timecapsules.fabric;

import net.fabricmc.api.ModInitializer;

import com.quoomy.timecapsules.TimeCapsulesMod;

public final class TimeCapsulesModFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        TimeCapsulesMod.init();
    }
}
