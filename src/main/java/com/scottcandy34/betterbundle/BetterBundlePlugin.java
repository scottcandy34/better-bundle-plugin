package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BetterBundlePlugin extends JavaPlugin {

    private NamespacedKey bundleKey;
    private NamespacedKey slotKey;

    private ItemFactory itemFactory;
    private BundleManager bundleManager;

    @Override
    public void onEnable() {
        bundleKey = new NamespacedKey(this, "is_bundle");
        slotKey = new NamespacedKey(this, "is_slot");

        itemFactory = new ItemFactory(this, bundleKey, slotKey);
        bundleManager = new BundleManager(this);

        BundleListener listener = new BundleListener(this, itemFactory, bundleManager);
        getServer().getPluginManager().registerEvents(listener, this);

        registerBundleRecipe();

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
            item = itemFactory.createBundleItem(amount);
            itemName = "Bundle";
        } else if (type.equals("slot")) {
            item = itemFactory.createSlotItem(amount);
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

    @Override
    public void onDisable() {
        getLogger().info("BetterBundle disabled.");
    }

    private void registerBundleRecipe() {
        ItemStack bundle = itemFactory.createBundleItem(1);

        ShapedRecipe recipe = new ShapedRecipe(Constants.RECIPE_KEY, bundle);
        recipe.shape(" S ", "HHH", " H ");
        recipe.setIngredient('S', org.bukkit.Material.STRING);
        recipe.setIngredient('H', org.bukkit.Material.RABBIT_HIDE);

        Bukkit.addRecipe(recipe);
        getLogger().info("Registered custom Bundle crafting recipe.");
    }

    // === Getters for other classes ===
    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public BundleManager getBundleManager() {
        return bundleManager;
    }

    // Used by ItemFactory
    public void updateBundle(ItemStack bundle) {
        if (bundleManager != null) {
            bundleManager.updateBundle(bundle);
        }
    }
}