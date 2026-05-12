package com.grok.betterbundles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BetterBundlesPlugin extends JavaPlugin implements Listener {

    private BundleManager manager;
    private final Map<UUID, BundleSession> openSessions = new HashMap<>();
    private final Map<Inventory, UUID> inventoryToPlayer = new HashMap<>();

    @Override
    public void onEnable() {
        manager = new BundleManager(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        registerRecipes();
        getLogger().info("BetterBundles plugin enabled! Upgradable bundles with large capacity support.");

        getCommand("betterbundles").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(
                        ChatColor.YELLOW + "Usage: /betterbundles <tier>  (bundle, iron, gold, diamond, netherite)");
                return true;
            }
            try {
                BundleTier tier = BundleTier.fromString(args[0]);
                ItemStack bundle = manager.createBundle(tier);
                player.getInventory().addItem(bundle);
                player.sendMessage(ChatColor.GREEN + "Gave you a " + tier.getColoredName() + ChatColor.GREEN + "!");
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid tier. Use: bundle, iron, gold, diamond, netherite");
            }
            return true;
        });
    }

    private void registerRecipes() {
        NamespacedKey ironKey = new NamespacedKey(this, "iron_bundle");
        ShapedRecipe ironRecipe = new ShapedRecipe(ironKey, manager.createBundle(BundleTier.IRON));
        ironRecipe.shape("III", "IBI", "III");
        ironRecipe.setIngredient('I', Material.IRON_INGOT);
        ironRecipe.setIngredient('B', Material.BUNDLE);
        Bukkit.addRecipe(ironRecipe);

        NamespacedKey goldKey = new NamespacedKey(this, "gold_bundle");
        ShapedRecipe goldRecipe = new ShapedRecipe(goldKey, manager.createBundle(BundleTier.GOLD));
        goldRecipe.shape("GGG", "GBG", "GGG");
        goldRecipe.setIngredient('G', Material.GOLD_INGOT);
        goldRecipe.setIngredient('B', Material.BUNDLE);
        Bukkit.addRecipe(goldRecipe);

        NamespacedKey diamondKey = new NamespacedKey(this, "diamond_bundle");
        ShapedRecipe diamondRecipe = new ShapedRecipe(diamondKey, manager.createBundle(BundleTier.DIAMOND));
        diamondRecipe.shape("DDD", "DBD", "DDD");
        diamondRecipe.setIngredient('D', Material.DIAMOND);
        diamondRecipe.setIngredient('B', Material.BUNDLE);
        Bukkit.addRecipe(diamondRecipe);

        NamespacedKey netheriteKey = new NamespacedKey(this, "netherite_bundle");
        ShapedRecipe netheriteRecipe = new ShapedRecipe(netheriteKey, manager.createBundle(BundleTier.NETHERITE));
        netheriteRecipe.shape("NNN", "NBN", "NNN");
        netheriteRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        netheriteRecipe.setIngredient('B', Material.BUNDLE);
        Bukkit.addRecipe(netheriteRecipe);

        getLogger().info("Registered upgrade recipes for all bundle tiers.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !manager.isCustomBundle(item))
            return;

        event.setCancelled(true);
        openBundleGUI(player, item, player.getInventory().getHeldItemSlot());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null)
            return;

        ItemStack clickedItem = event.getCurrentItem();

        if (event.getClick() == ClickType.RIGHT && clickedItem != null && manager.isCustomBundle(clickedItem)) {
            if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING ||
                    event.getView().getTopInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
                openBundleGUI(player, clickedItem, event.getSlot());
                return;
            }
        }

        UUID playerId = player.getUniqueId();
        if (openSessions.containsKey(playerId)) {
            BundleSession session = openSessions.get(playerId);
            if (event.getInventory().equals(session.gui)) {
                handleBundleGUIClick(event, player, session);
            }
        }
    }

    private void handleBundleGUIClick(InventoryClickEvent event, Player player, BundleSession session) {
        int slot = event.getRawSlot();
        event.setCancelled(true);

        BundleTier tier = session.tier;
        List<ItemStack> contents = session.contents;

        if (slot < 45) {
            ItemStack cursor = event.getCursor();
            ItemStack currentInSlot = event.getCurrentItem();

            if (cursor != null && cursor.getType() != Material.AIR) {
                if (manager.canAddItem(contents, cursor, tier)) {
                    contents.add(cursor.clone());
                    player.setItemOnCursor(null);
                    refreshGUI(session);
                } else {
                    player.sendMessage(
                            ChatColor.RED + "Not enough space in the " + tier.getColoredName() + ChatColor.RED + "!");
                }
            } else if (currentInSlot != null && currentInSlot.getType() != Material.AIR) {
                player.setItemOnCursor(currentInSlot.clone());
                for (int i = 0; i < contents.size(); i++) {
                    if (contents.get(i).isSimilar(currentInSlot)) {
                        contents.remove(i);
                        break;
                    }
                }
                refreshGUI(session);
            }
        } else if (slot == 45) {
            if (session.currentPage > 0) {
                session.currentPage--;
                refreshGUI(session);
            }
        } else if (slot == 53) {
            int totalPages = (contents.size() + 44) / 45;
            if (session.currentPage < totalPages - 1) {
                session.currentPage++;
                refreshGUI(session);
            }
        }
    }

    private void refreshGUI(BundleSession session) {
        Inventory gui = session.gui;
        List<ItemStack> contents = session.contents;
        int page = session.currentPage;
        int itemsPerPage = 45;
        int start = page * itemsPerPage;

        for (int i = 0; i < 45; i++) {
            gui.setItem(i, null);
        }

        for (int i = 0; i < itemsPerPage; i++) {
            int idx = start + i;
            if (idx < contents.size()) {
                gui.setItem(i, contents.get(idx).clone());
            }
        }

        int totalPages = Math.max(1, (contents.size() + itemsPerPage - 1) / itemsPerPage);

        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GREEN + "◀ Previous Page");
        prevMeta.setLore(Arrays.asList(ChatColor.GRAY + "Page " + (page + 1) + " of " + totalPages));
        prev.setItemMeta(prevMeta);
        gui.setItem(45, prev);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        double used = manager.calculateUsedCapacity(contents);
        infoMeta.setDisplayName(ChatColor.YELLOW + "Bundle Info");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Capacity: " + ChatColor.GREEN + String.format("%.1f", used) + ChatColor.GRAY
                + " / " + ChatColor.YELLOW + session.tier.getCapacity());
        infoLore.add(ChatColor.GRAY + "Items stored: " + ChatColor.WHITE + contents.size());
        infoLore.add(ChatColor.GRAY + "Page " + (page + 1) + " / " + totalPages);
        if (session.tier == BundleTier.NETHERITE) {
            infoLore.add(ChatColor.RED + "Fireproof");
        }
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(49, info);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page ▶");
        nextMeta.setLore(Arrays.asList(ChatColor.GRAY + "Page " + (page + 1) + " of " + totalPages));
        next.setItemMeta(nextMeta);
        gui.setItem(53, next);
    }

    private void openBundleGUI(Player player, ItemStack bundleItem, int sourceSlot) {
        if (openSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have a bundle open!");
            return;
        }

        BundleTier tier = manager.getTier(bundleItem);
        if (tier == null)
            return;

        List<ItemStack> contents = manager.getContents(bundleItem);

        String title = tier.getColoredName() + ChatColor.GRAY + " Inventory";
        Inventory gui = Bukkit.createInventory(null, 54, title);

        BundleSession session = new BundleSession(bundleItem, tier, contents, gui, 0, sourceSlot);
        openSessions.put(player.getUniqueId(), session);
        inventoryToPlayer.put(gui, player.getUniqueId());

        refreshGUI(session);
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID id = player.getUniqueId();
        if (!openSessions.containsKey(id))
            return;

        BundleSession session = openSessions.remove(id);
        inventoryToPlayer.remove(event.getInventory());

        manager.setContents(session.bundleItem, session.contents);
        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Bundle contents saved.");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Item itemEntity))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE &&
                event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK &&
                event.getCause() != EntityDamageEvent.DamageCause.LAVA)
            return;

        ItemStack item = itemEntity.getItemStack();
        if (manager.isCustomBundle(item) && manager.getTier(item) == BundleTier.NETHERITE) {
            event.setCancelled(true);
        }
    }

    private static class BundleSession {
        final ItemStack bundleItem;
        final BundleTier tier;
        final List<ItemStack> contents;
        final Inventory gui;
        int currentPage;
        final int sourceSlot;

        BundleSession(ItemStack bundleItem, BundleTier tier, List<ItemStack> contents, Inventory gui, int currentPage,
                int sourceSlot) {
            this.bundleItem = bundleItem;
            this.tier = tier;
            this.contents = contents;
            this.gui = gui;
            this.currentPage = currentPage;
            this.sourceSlot = sourceSlot;
        }
    }
}