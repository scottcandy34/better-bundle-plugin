package com.scottcandy34.betterbundle;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class Constants {
    public static final int MAX_WEIGHT = 64;
    public static final NamespacedKey RECIPE_KEY = new NamespacedKey("betterbundle", "bundle_recipe");

    public static final Set<Material> GUI_OPENING_BLOCKS = Set.of(
        // === FUNCTIONAL / WORKSTATION BLOCKS (no InventoryHolder) ===
        Material.CRAFTING_TABLE,
        Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
        Material.LOOM,
        Material.SMITHING_TABLE,
        Material.CARTOGRAPHY_TABLE,
        Material.GRINDSTONE,
        Material.STONECUTTER,
        Material.ENCHANTING_TABLE,
        Material.BEACON,
        Material.LECTERN,
        Material.CRAFTER,

        // === CONTAINERS (implement InventoryHolder / Container) ===
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.BARREL,
        Material.ENDER_CHEST,

        Material.FURNACE,
        Material.BLAST_FURNACE,
        Material.SMOKER,

        Material.BREWING_STAND,

        Material.HOPPER,
        Material.DISPENSER,
        Material.DROPPER,

        // === ALL 17 SHULKER BOXES ===
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX
    );
    
    public static final Set<Material> NON_GUI_INTERACTIVE_BLOCKS = Set.of(
        // === DOORS (wooden + copper open by hand; iron only by redstone) ===
        Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR,
        Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.MANGROVE_DOOR, Material.CHERRY_DOOR,
        Material.BAMBOO_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR,
        Material.IRON_DOOR, Material.COPPER_DOOR, Material.EXPOSED_COPPER_DOOR,
        Material.WEATHERED_COPPER_DOOR, Material.OXIDIZED_COPPER_DOOR,

        // === TRAPDOORS (except iron) ===
        Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
        Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.CHERRY_TRAPDOOR,
        Material.BAMBOO_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR,
        Material.COPPER_TRAPDOOR, Material.EXPOSED_COPPER_TRAPDOOR, Material.WEATHERED_COPPER_TRAPDOOR, Material.OXIDIZED_COPPER_TRAPDOOR,

        // === FENCE GATES ===
        Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
        Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.MANGROVE_FENCE_GATE, Material.CHERRY_FENCE_GATE,
        Material.BAMBOO_FENCE_GATE, Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE,

        // === BUTTONS ===
        Material.STONE_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON,
        Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON,
        Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.MANGROVE_BUTTON, Material.CHERRY_BUTTON,
        Material.BAMBOO_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,

        // === REDSTONE & MECHANISMS ===
        Material.LEVER,
        Material.REPEATER,
        Material.COMPARATOR,
        Material.DAYLIGHT_DETECTOR,
        Material.REDSTONE_WIRE,

        // === USABLE BLOCKS ===
        Material.BELL,
        Material.NOTE_BLOCK,
        Material.CAKE,
        Material.ITEM_FRAME,
        Material.GLOW_ITEM_FRAME,
        Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED, Material.LIGHT_BLUE_BED,
        Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED, Material.GRAY_BED,
        Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED,
        Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED,

        Material.DRAGON_EGG,
        Material.RESPAWN_ANCHOR,
        Material.SWEET_BERRY_BUSH,
        Material.CHISELED_BOOKSHELF,
        Material.DECORATED_POT,

        // === ADMIN / CREATIVE BLOCKS ===
        Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
        Material.STRUCTURE_BLOCK, Material.JIGSAW
    );
}
