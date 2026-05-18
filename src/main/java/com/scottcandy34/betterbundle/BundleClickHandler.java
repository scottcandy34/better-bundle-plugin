package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BundleClickHandler {

    private final BetterBundlePlugin plugin;
    private final ItemFactory itemFactory;
    private final BundleManager bundleManager;

    public BundleClickHandler(BetterBundlePlugin plugin, ItemFactory itemFactory, BundleManager bundleManager) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.bundleManager = bundleManager;
    }

    public void handle(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getType() == InventoryType.CREATIVE) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // === SLOT HANDLING ===
        // Slot insertion limit (12 unique items)
        if (isAttemptingSlotInsertion(event)) {
            event.setCancelled(true);
            ItemStack slotItem = itemFactory.isOurSlot(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurSlot(current) ? cursor : current;
            boolean isCancelled = handleSlotInsertion(player, slotItem, insertItem);
            event.setCancelled(isCancelled);
        }

        // SHIFT_RIGHT on Slot (empty into inventory)
        if (isShiftRightClickToEmptySlot(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            handleEmptySlot(player, cursor, targetInv);
        }

        // === BUNDLE HANDLING ===
        // SHIFT_LEFT → Open bundle
        if (isShiftLeftClickToOpen(event)) {
            event.setCancelled(true);
            handleOpenBundle(player, current);
        }

        // SHIFT_RIGHT on Bundle (empty into inventory)
        if (isShiftRightClickToEmptyBundle(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            handleEmptyBundle(player, cursor, targetInv);
        }

        // LEFT CLICK on Bundle (insert item)
        if (isLeftClickToInsert(event)) {
            event.setCancelled(true);
            ItemStack bundleItem = itemFactory.isOurBundle(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurBundle(current) ? cursor : current;
            handleInsertItem(player, bundleItem, insertItem);
        }

        // RIGHT CLICK on Bundle (remove item)
        if (isRightClickToRemove(event)) {
            event.setCancelled(true);
            if (itemFactory.isOurBundle(cursor)) {
                Inventory targetInv = getTargetInventory(event);
                handleRemoveItem(player, cursor, targetInv, event.getSlot());
            } else {
                handleRemoveItem(player, current);
            }
        }
    }

    // ==================== Helper Methods ====================

    private Inventory getTargetInventory(InventoryClickEvent event) {
        return (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER)
                ? event.getClickedInventory()
                : event.getWhoClicked().getInventory();
    }

    // --- Condition Checks ---
    private boolean isAttemptingSlotInsertion(InventoryClickEvent event) {
        return event.getClick().isLeftClick() &&
                ((itemFactory.isOurSlot(event.getCurrentItem()) && event.getCursor() != null && event.getCursor().getType() != Material.AIR) ||
                 (itemFactory.isOurSlot(event.getCursor()) && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR));
    }

    private boolean isShiftLeftClickToOpen(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_LEFT && itemFactory.isOurBundle(event.getCurrentItem());
    }

    private boolean isShiftRightClickToEmptySlot(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_RIGHT && itemFactory.isOurSlot(event.getCursor());
    }

    private boolean isShiftRightClickToEmptyBundle(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_RIGHT && itemFactory.isOurBundle(event.getCursor());
    }

    private boolean isLeftClickToInsert(InventoryClickEvent event) {
        return event.getClick().isLeftClick() &&
                ((itemFactory.isOurBundle(event.getCurrentItem()) && event.getCursor() != null && event.getCursor().getType() != Material.AIR) ||
                 (itemFactory.isOurBundle(event.getCursor()) && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR));
    }

    private boolean isRightClickToRemove(InventoryClickEvent event) {
        return event.getClick().isRightClick() &&
                ((itemFactory.isOurBundle(event.getCursor()) && (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) && bundleManager.hasItemInInventory(event.getCursor())) ||
                 (itemFactory.isOurBundle(event.getCurrentItem()) && (event.getCursor() == null || event.getCursor().getType() == Material.AIR) && bundleManager.hasItemInInventory(event.getCurrentItem())));
    }

    // --- Handlers ---
    private boolean handleSlotInsertion(Player player, ItemStack slotItem, ItemStack insertItem) {
        int currentUnique = bundleManager.getRepackService().getUniqueItemCount(slotItem);
        Material insertType = (insertItem != null) ? insertItem.getType() : Material.AIR;

        boolean alreadyPresent = false;
        if (insertType != Material.AIR && slotItem.getItemMeta() instanceof BundleMeta meta) {
            for (ItemStack existing : meta.getItems()) {
                if (existing != null && existing.getType() == insertType) {
                    alreadyPresent = true;
                    break;
                }
            }
        }

        if (currentUnique < 12 || alreadyPresent) return false;
        
        BundleSound.INSERT_FAIL.play(player);
        player.sendMessage(Component.text("Slot can only hold 12 different items!", NamedTextColor.RED));
        return true;
    }

    private void handleEmptySlot(Player player, ItemStack slotItem, Inventory clickedInv) {
        if (!(slotItem.getItemMeta() instanceof BundleMeta meta)) return;

        int emptySlots = bundleManager.getEmptySlotsInInventory(clickedInv);
        if (emptySlots <= 0) {
            player.sendMessage(Component.text("No empty space available!", NamedTextColor.RED));
            return;
        }

        List<ItemStack> slotContents = new ArrayList<>(meta.getItems());
        List<ItemStack> extracted = new ArrayList<>();

        for (int i = slotContents.size() - 1; i >= 0 && extracted.size() < emptySlots; i--) {
            ItemStack item = slotContents.get(i);
            if (item != null && item.getType() != Material.AIR) {
                extracted.add(item.clone());
                slotContents.remove(i);
            }
        }

        if (!extracted.isEmpty()) {
            HashMap<Integer, ItemStack> leftovers = clickedInv.addItem(extracted.toArray(new ItemStack[0]));
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    if (leftover != null && leftover.getType() != Material.AIR) slotContents.add(leftover);
                }
            }
            meta.setItems(slotContents);
            slotItem.setItemMeta(meta);
        }

        if (extracted.isEmpty()) {
            player.sendMessage(Component.text("The Slot is empty.", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("Emptied " + extracted.size() + " item(s) from the end!", NamedTextColor.GRAY));
            BundleSound.DROP_CONTENTS.play(player);
        }
    }

    private void handleOpenBundle(Player player, ItemStack bundleItem) {
        bundleManager.updateBundle(bundleItem);
        Inventory bundleInv = bundleManager.getBundleInventory(bundleItem);
        if (bundleInv != null) {
            player.openInventory(bundleInv);
        }
    }

    private void handleEmptyBundle(Player player, ItemStack bundleItem, Inventory targetInv) {
        Inventory bundleInv = bundleManager.getBundleInventory(bundleItem);
        if (bundleInv == null) return;

        int emptySlots = bundleManager.getEmptySlotsInInventory(targetInv);
        if (emptySlots <= 0) {
            player.sendMessage(Component.text("No empty space available!", NamedTextColor.RED));
            return;
        }

        List<ItemStack> extracted = bundleManager.extractLastItemsFromBundle(bundleInv, emptySlots);

        if (!extracted.isEmpty()) {
            HashMap<Integer, ItemStack> leftovers = targetInv.addItem(extracted.toArray(new ItemStack[0]));
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    if (leftover != null && leftover.getType() != Material.AIR) {
                        bundleInv.addItem(leftover);
                    }
                }
            }
            bundleManager.repackBundle(bundleInv, 0);
        }

        bundleManager.saveBundleInventory(bundleItem, bundleInv);
        bundleManager.updateBundle(bundleItem);

        if (extracted.isEmpty()) {
            player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("Emptied " + extracted.size() + " item(s) from the end!", NamedTextColor.GRAY));
            BundleSound.DROP_CONTENTS.play(player);
        }
    }

    private void handleInsertItem(Player player, ItemStack bundleItem, ItemStack insertItem) {
        Inventory bundleInv = bundleManager.getBundleInventory(bundleItem);
        if (bundleInv == null) return;

        if (bundleManager.isBlockedItem(insertItem)) {
            player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
            BundleSound.INSERT_FAIL.play(player);
            return;
        }

        ItemStack leftover = bundleManager.addItemToBundle(bundleInv, insertItem);

        if (leftover != null) {
            insertItem.setAmount(leftover.getAmount());
        } else {
            insertItem.setAmount(0);
        }

        if (leftover != null && leftover.getAmount() == insertItem.getAmount()) {
            BundleSound.INSERT_FAIL.play(player);
        } else {
            BundleSound.INSERT.play(player);
        }

        bundleManager.saveBundleInventory(bundleItem, bundleInv);
        bundleManager.updateBundle(bundleItem);
    }

    private void handleRemoveItem(Player player, ItemStack bundleItem, Inventory clickedInv, int slot) {
        Inventory bundleInv = bundleManager.getBundleInventory(bundleItem);
        if (bundleInv == null) return;

        ItemStack removed = bundleManager.removeLastItemFromBundle(bundleInv);

        if (removed != null) {
            if (clickedInv != null) {
                clickedInv.setItem(slot, removed);
            } else {
                player.getInventory().addItem(removed);
            }
            BundleSound.REMOVE_ONE.play(player);
        } else {
            player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
        }

        bundleManager.saveBundleInventory(bundleItem, bundleInv);
        bundleManager.updateBundle(bundleItem);
    }

    private void handleRemoveItem(Player player, ItemStack bundleItem) {
        Inventory bundleInv = bundleManager.getBundleInventory(bundleItem);
        if (bundleInv == null) return;

        ItemStack removed = bundleManager.removeLastItemFromBundle(bundleInv);

        if (removed != null) {
            player.setItemOnCursor(removed.clone());
            BundleSound.REMOVE_ONE.play(player);
        } else {
            player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
        }

        bundleManager.saveBundleInventory(bundleItem, bundleInv);
        bundleManager.updateBundle(bundleItem);
    }
}