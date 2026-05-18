package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BundleListener implements Listener {

    private final BetterBundlePlugin plugin;
    private final ItemFactory itemFactory;
    private final BundleManager bundleManager;
    private final BundleClickHandler clickHandler;

    public BundleListener(BetterBundlePlugin plugin, ItemFactory itemFactory, BundleManager bundleManager) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.bundleManager = bundleManager;
        this.clickHandler = new BundleClickHandler(plugin, itemFactory, bundleManager);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (itemFactory.isOurBundle(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot place this Bundle as a block!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!itemFactory.isOurBundle(mainHand)) return;

        event.setUseItemInHand(Result.DENY);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Result.DENY);
        }
        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            bundleManager.performBundleRemoval(player, mainHand);
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (Constants.GUI_OPENING_BLOCKS.contains(block.getType()) || Constants.NON_GUI_INTERACTIVE_BLOCKS.contains(block.getType())) {
            event.setCancelled(false);
            return;
        }

        bundleManager.performBundleRemoval(player, mainHand);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        clickHandler.handle(event);
    }

    // When player closes the bundle GUI, refresh lore + durability bar on all bundles
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (!bundleManager.isOurInventoryView(event.getView())) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (itemFactory.isOurBundle(item)) {
                Inventory inv = bundleManager.getBundleInventory(item);
                if (inv != null) {
                    bundleManager.saveBundleInventory(item, inv);
                }
                bundleManager.updateBundle(item);
            }
        }

        ItemStack cursor = player.getItemOnCursor();
        if (itemFactory.isOurBundle(cursor)) {
            Inventory inv = bundleManager.getBundleInventory(cursor);
            if (inv != null) {
                bundleManager.saveBundleInventory(cursor, inv);
            }
            bundleManager.updateBundle(cursor);
        }
    }
}
