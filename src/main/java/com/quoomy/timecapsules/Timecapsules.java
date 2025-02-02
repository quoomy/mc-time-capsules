package com.quoomy.timecapsules;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timecapsules implements ModInitializer
{
	public static final String MOD_ID = "timecapsules";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String MOD_VERSION = "1.0.0"; // change this whenever meaningful changes to uploading logic are made. is sent with the capsule to the server to ensure compatibility

	@Override
	public void onInitialize()
	{
		LOGGER.info("Hello Fabric world!");

		ModRegistrations.register();
	}
}