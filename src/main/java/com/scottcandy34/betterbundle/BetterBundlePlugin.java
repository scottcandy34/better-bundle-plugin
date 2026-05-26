package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BetterBundlePlugin extends JavaPlugin {

    private final ItemFactory itemFactory = new ItemFactory();

    @Override
    public void onEnable() {
        BundleListener listener = new BundleListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        registerBundleRecipe();
        registerDyeRecipes();

        getCommand("betterbundle").setExecutor(this);

        getLogger().info("BetterBundle plugin enabled for Paper 26.1.2!");
        getLogger().info("Craft a Bundle with 5 Rabbit Hide + 2 String (shaped recipe).");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("betterbundle")) return false;

        if (!sender.hasPermission("betterbundle.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args[0].equalsIgnoreCase("filltest")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
                return true;
            }

            // Create a fresh bundle
            ItemStack bundle = itemFactory.createCopperBundleItem(1);
            BundleItem bundleItem = new BundleItem(bundle);
            BundleInventory bundleInv = bundleItem.getInventory();

            if (bundleInv == null) {
                player.sendMessage(Component.text("Failed to create test bundle.", NamedTextColor.RED));
                return true;
            }

            // Fill the first 27 slots with unique items (wool + concrete)
            Material[] testItems = {
                Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
                Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL,
                Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE, Material.LIGHT_BLUE_CONCRETE,
                Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE, Material.GRAY_CONCRETE,
                Material.LIGHT_GRAY_CONCRETE, Material.CYAN_CONCRETE, Material.PURPLE_CONCRETE
            };

            for (Material mat : testItems) {
                bundleInv.addItem(new ItemStack(mat, 1));
            }

            bundleItem.getInventory().getHandle().setContents(bundleInv.getHandle().getContents());
            bundleItem.update();

            player.getInventory().addItem(bundle);
            player.sendMessage(Component.text("Gave you a test bundle with 27 unique items.", NamedTextColor.GREEN));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("Usage: /betterbundle give <bundle|slot> [amount] [player]", NamedTextColor.GRAY));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /betterbundle give <bundle|slot> [amount] [player]", NamedTextColor.GRAY));
            return true;
        }

        String type = args[1].toLowerCase();
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) amount = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount!", NamedTextColor.RED));
                return true;
            }
        }

        Player target = null;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("You must specify a player when using console.", NamedTextColor.RED));
            return true;
        }

        ItemStack item;
        String itemName;
        if (type.equals("bundle")) {
            item = itemFactory.createCopperBundleItem(amount);
            itemName = "Bundle";
        } else if (type.equals("slot")) {
            item = itemFactory.createBundleSlotItem(amount);
            itemName = "Slot";
        } else {
            sender.sendMessage(Component.text("Unknown item type! Use 'bundle' or 'slot'.", NamedTextColor.RED));
            return true;
        }

        target.getInventory().addItem(item);
        target.sendMessage(Component.text("You received " + amount + "x " + itemName + "!", NamedTextColor.GREEN));
        if (!target.equals(sender)) {
            sender.sendMessage(Component.text("Gave " + amount + "x " + itemName + " to " + target.getName(), NamedTextColor.GREEN));
        }
        return true;
    }

    private void registerDyeRecipes() {
        Material[] dyes = {
            Material.WHITE_DYE, Material.ORANGE_DYE, Material.MAGENTA_DYE, Material.LIGHT_BLUE_DYE,
            Material.YELLOW_DYE, Material.LIME_DYE, Material.PINK_DYE, Material.GRAY_DYE,
            Material.LIGHT_GRAY_DYE, Material.CYAN_DYE, Material.PURPLE_DYE, Material.BLUE_DYE,
            Material.BROWN_DYE, Material.GREEN_DYE, Material.RED_DYE, Material.BLACK_DYE
        };

        ItemStack template = itemFactory.createCopperBundleItem(1);

        int count = 0;
        for (Material dye : dyes) {
            NamespacedKey key = new NamespacedKey(this, "bundle_dye_" + dye.name().toLowerCase());

            ShapelessRecipe dyeRecipe = new ShapelessRecipe(key, template.clone());
            dyeRecipe.addIngredient(dye);
            dyeRecipe.addIngredient(new RecipeChoice.ExactChoice(template));

            Bukkit.addRecipe(dyeRecipe);
            count++;
        }

        getLogger().info("Registered " + count + " bundle dyeing recipes.");
}

    @Override
    public void onDisable() {
        getLogger().info("BetterBundle disabled.");
    }

    private void registerBundleRecipe() {
        ItemStack bundle = itemFactory.createCopperBundleItem(1);

        ShapedRecipe recipe = new ShapedRecipe(Constants.RECIPE_KEY, bundle);
        recipe.shape("CCC", "CBC", "CCC");
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('B', Material.BUNDLE);

        Bukkit.addRecipe(recipe);
        getLogger().info("Registered custom Bundle crafting recipe (Copper Ingots surrounding a normal Bundle).");
    }
}