package com.rs256.infinote.compat;

import net.minecraft.world.level.block.Block;
//? if <=1.19.2 {
/*import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
 *///?} else {
import net.minecraft.core.registries.BuiltInRegistries;
//?}

public class RegistryCompat {
    //? if <=1.19.2 {
    /*public static String getKey(Block block) {
        return Registry.BLOCK.getKey(block).toString();
    }
   *///?} else {
   public static String getKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
   //?}
   
    //? if <=1.19.2 {
    /*public static Block getBlock(String blockId) {
        ResourceLocation id = IdCompat.idFromString(blockId);
        if (id == null) return null;

        if (!Registry.BLOCK.containsKey(id)) return null;
        return Registry.BLOCK.get(id);
    }
     *///?} else if <=1.21.1 {
    /*public static Block getBlock(String blockId) {
        var id = IdCompat.idFromString(blockId);
        if (id == null) return null;

        if (!BuiltInRegistries.BLOCK.containsKey(id)) return null;
        return BuiltInRegistries.BLOCK.get(id);
    }
    *///?} else {
    public static Block getBlock(String blockId) {
        var id = IdCompat.idFromString(blockId);
        if (id == null) return null;

        if (!BuiltInRegistries.BLOCK.containsKey(id)) return null;
        return BuiltInRegistries.BLOCK.getValue(id);
    }
    //?}
}
