package com.quoomy.timecapsules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimeCapsulesMod
{
    public static final String MOD_ID = "timecapsules";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init()
    {
        LOGGER.info("Hello from Time Capsules!");

        ModContent.register();
    }
}
