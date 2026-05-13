package com.grok.betterbundles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class BetterBundlesPlugin extends JavaPlugin implements Listener {

    private NamespacedKey copperBundleKey;
    private NamespacedKey recipeKey;
    private NamespacedKey colorKey;
    private final List<NamespacedKey> coloredRecipeKeys = new ArrayList<>();

    @Override
    public void onEnable() {
        copperBundleKey = new NamespacedKey(this, "copper_bundle");
        recipeKey = new NamespacedKey(this, "copper_bundle_recipe");
        colorKey = new NamespacedKey(this, "bundle_color");

        registerCopperBundleRecipe();
        registerAllColoredRecipes();

        getServer().getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipe(recipeKey);
            for (NamespacedKey key : coloredRecipeKeys) {
                player.discoverRecipe(key);
            }
        }

        getLogger().info("BetterBundles plugin enabled! All colored Copper Bundle recipes added.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterBundles plugin disabled.");
    }

    private void registerCopperBundleRecipe() {
        Bukkit.removeRecipe(recipeKey);

        ItemStack copperBundle = createColoredCopperBundle(null, true);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, copperBundle);
        recipe.shape("CCC", "CBC", "CCC");
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('B', Material.BUNDLE);
        recipe.setGroup("copper_bundles");           // ← Added this line

        Bukkit.addRecipe(recipe);
        getLogger().info("Registered base Copper Bundle recipe.");
    }

    private void registerAllColoredRecipes() {
        coloredRecipeKeys.clear();

        for (DyeColor color : DyeColor.values()) {
            NamespacedKey key = new NamespacedKey(this, "copper_bundle_" + color.name().toLowerCase());
            coloredRecipeKeys.add(key);

            Bukkit.removeRecipe(key);

            ItemStack result = createColoredCopperBundle(color, true);

            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("CCC", "CBC", "CCC");
            recipe.setIngredient('C', Material.COPPER_INGOT);
            recipe.setIngredient('B', getBundleMaterialForColor(color));
            recipe.setGroup("copper_bundles");

            Bukkit.addRecipe(recipe);
        }

        getLogger().info("Registered " + coloredRecipeKeys.size() + " colored Copper Bundle recipes.");
    }

    private Material getBundleMaterialForColor(DyeColor color) {
        if (color == null) return Material.BUNDLE;
        return switch (color) {
            case WHITE -> Material.WHITE_BUNDLE;
            case ORANGE -> Material.ORANGE_BUNDLE;
            case MAGENTA -> Material.MAGENTA_BUNDLE;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_BUNDLE;
            case YELLOW -> Material.YELLOW_BUNDLE;
            case LIME -> Material.LIME_BUNDLE;
            case PINK -> Material.PINK_BUNDLE;
            case GRAY -> Material.GRAY_BUNDLE;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_BUNDLE;
            case CYAN -> Material.CYAN_BUNDLE;
            case PURPLE -> Material.PURPLE_BUNDLE;
            case BLUE -> Material.BLUE_BUNDLE;
            case BROWN -> Material.BROWN_BUNDLE;
            case GREEN -> Material.GREEN_BUNDLE;
            case RED -> Material.RED_BUNDLE;
            case BLACK -> Material.BLACK_BUNDLE;
            default -> Material.BUNDLE;
        };
    }

    public boolean isCopperBundle(ItemStack item) {
        if (item == null || !isAnyBundle(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(copperBundleKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.discoverRecipe(recipeKey);
        for (NamespacedKey key : coloredRecipeKeys) {
            player.discoverRecipe(key);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory() == null) return;
        ItemStack[] matrix = event.getInventory().getMatrix();

        // Dyeing
        ItemStack dyeItem = null;
        ItemStack bundleItem = null;

        for (ItemStack item : matrix) {
            if (item == null || item.getAmount() == 0) continue;
            if (isDye(item.getType())) dyeItem = item;
            else if (isAnyBundle(item)) bundleItem = item;
        }

        if (dyeItem != null && bundleItem != null) {
            DyeColor newDyeColor = getDyeColorFromMaterial(dyeItem.getType());
            if (newDyeColor != null) {
                boolean isCopper = isCopperBundle(bundleItem);
                DyeColor nameColor = isCopper ? getBundleDyeColor(bundleItem) : newDyeColor;
                if (nameColor == null) nameColor = newDyeColor;

                ItemStack result = createColoredCopperBundle(nameColor, isCopper);
                event.getInventory().setResult(result);
            }
            return;
        }

        // Crafting with dyed center
        int copperCount = 0;
        ItemStack centerBundle = null;

        for (ItemStack item : matrix) {
            if (item == null || item.getAmount() == 0) continue;
            if (item.getType() == Material.COPPER_INGOT) copperCount++;
            else if (isAnyBundle(item)) centerBundle = item;
        }

        if (copperCount == 8 && centerBundle != null) {
            DyeColor existingColor = getBundleDyeColor(centerBundle);
            ItemStack result = createColoredCopperBundle(existingColor != null ? existingColor : null, true);
            event.getInventory().setResult(result);
        }
    }

    private boolean isDye(Material material) {
        return material != null && material.name().endsWith("_DYE");
    }

    private boolean isAnyBundle(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.equals("BUNDLE") || name.endsWith("_BUNDLE");
    }

    private DyeColor getDyeColorFromMaterial(Material material) {
        if (material == null) return null;
        try {
            return DyeColor.valueOf(material.name().replace("_DYE", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private DyeColor getBundleDyeColor(ItemStack bundle) {
        if (bundle == null || !isAnyBundle(bundle)) return null;

        ItemMeta meta = bundle.getItemMeta();

        if (meta != null && meta.getPersistentDataContainer().has(colorKey, PersistentDataType.STRING)) {
            try {
                return DyeColor.valueOf(meta.getPersistentDataContainer().get(colorKey, PersistentDataType.STRING));
            } catch (Exception ignored) {}
        }

        String name = bundle.getType().name();
        if (name.endsWith("_BUNDLE") && !name.equals("BUNDLE")) {
            try {
                return DyeColor.valueOf(name.replace("_BUNDLE", ""));
            } catch (Exception ignored) {}
        }
        return null;
    }

    public ItemStack createColoredCopperBundle(DyeColor color, boolean isCopper) {
        Material material = (color != null) ? getDyedBundleMaterial(color) : Material.BUNDLE;
        ItemStack result = new ItemStack(material);
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return result;

        if (isCopper) {
            meta.getPersistentDataContainer().set(copperBundleKey, PersistentDataType.STRING, "true");

            if (color != null) {
                String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
                meta.setDisplayName(ChatColor.GOLD + colorName + " Copper Bundle");

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Reinforced by copper.");
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(colorKey, PersistentDataType.STRING, color.name());
            } else {
                meta.setDisplayName(ChatColor.GOLD + "Copper Bundle");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Reinforced by copper.");
                meta.setLore(lore);
            }
        }
        result.setItemMeta(meta);
        return result;
    }

    private Material getDyedBundleMaterial(DyeColor color) {
        if (color == null) return Material.BUNDLE;
        return switch (color) {
            case WHITE -> Material.WHITE_BUNDLE;
            case ORANGE -> Material.ORANGE_BUNDLE;
            case MAGENTA -> Material.MAGENTA_BUNDLE;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_BUNDLE;
            case YELLOW -> Material.YELLOW_BUNDLE;
            case LIME -> Material.LIME_BUNDLE;
            case PINK -> Material.PINK_BUNDLE;
            case GRAY -> Material.GRAY_BUNDLE;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_BUNDLE;
            case CYAN -> Material.CYAN_BUNDLE;
            case PURPLE -> Material.PURPLE_BUNDLE;
            case BLUE -> Material.BLUE_BUNDLE;
            case BROWN -> Material.BROWN_BUNDLE;
            case GREEN -> Material.GREEN_BUNDLE;
            case RED -> Material.RED_BUNDLE;
            case BLACK -> Material.BLACK_BUNDLE;
            default -> Material.BUNDLE;
        };
    }

    private ChatColor getChatColorForDye(DyeColor dye) {
        return switch (dye) {
            case RED -> ChatColor.RED;
            case BLUE -> ChatColor.BLUE;
            case GREEN -> ChatColor.GREEN;
            case YELLOW -> ChatColor.YELLOW;
            case ORANGE -> ChatColor.GOLD;
            case PURPLE -> ChatColor.DARK_PURPLE;
            case PINK -> ChatColor.LIGHT_PURPLE;
            case LIME -> ChatColor.GREEN;
            case CYAN, LIGHT_BLUE -> ChatColor.AQUA;
            case MAGENTA -> ChatColor.LIGHT_PURPLE;
            case BROWN -> ChatColor.GOLD;
            case BLACK -> ChatColor.DARK_GRAY;
            case GRAY, LIGHT_GRAY -> ChatColor.GRAY;
            case WHITE -> ChatColor.WHITE;
            default -> ChatColor.WHITE;
        };
    }
}