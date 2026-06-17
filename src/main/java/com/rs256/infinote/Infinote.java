package com.rs256.infinote;

import com.rs256.infinote.commands.InfinoteCommand;
//? if >=1.20.3 {
import com.rs256.infinote.commands.BpmCommand;
//?}
import com.rs256.infinote.config.InfinoteConfig;

import net.fabricmc.api.ModInitializer;
//? if <=1.18.2 {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
 *///?} else {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//?}

//? if <=1.18.2 {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?} else {
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
//?}

public class Infinote implements ModInitializer {
	public static final String MOD_ID = "infinote";
	public static final String VERSION = /*$ mod_version*/ "1.5.1";
	public static final String MINECRAFT = /*$ minecraft*/ "26.2";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	//? if <=1.18.2 {
	/*public static final Logger LOGGER = LogManager.getLogger();
	 *///?} else {
	public static final Logger LOGGER = LogUtils.getLogger();
	//?}


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("He said the sky is the limit!");
		InfinoteConfig.load();
		registerCommands();
	}

	//? if <=1.18.2 {
	/*public void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> InfinoteCommand.register(dispatcher));
	}
	 *///?} else {
	public void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			InfinoteCommand.register(dispatcher, registryAccess);
			//? if >=1.20.3 {
			BpmCommand.register(dispatcher);
			//?}
		});
	}
	//?}
}
