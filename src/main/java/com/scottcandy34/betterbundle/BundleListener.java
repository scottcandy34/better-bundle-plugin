package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BundleListener implements Listener {

    private final BetterBundlePlugin plugin;
    private final ItemFactory itemFactory = new ItemFactory();
    private final BundleActions bundleActions = new BundleActions();
    private final BundleClickConditions clickConditions = new BundleClickConditions();

    public BundleListener(BetterBundlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!itemFactory.isOurBundle(mainHand)) return;

        // Shift + Right Click while holding Bundle → Open GUI
        if (player.isSneaking()) {
            bundleActions.handleOpenBundle(player, mainHand);
            return;
        }

        // Normal Right Click → Remove last item
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            bundleActions.performBundleRemoval(player, mainHand);
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (Constants.GUI_OPENING_BLOCKS.contains(block.getType()) || Constants.NON_GUI_INTERACTIVE_BLOCKS.contains(block.getType())) {
            event.setCancelled(false);
            return;
        }

        bundleActions.performBundleRemoval(player, mainHand);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getType() == InventoryType.CREATIVE) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // === BUNDLE GUI PROTECTION (UUID matched) ===
        // While THIS specific Bundle's GUI is open, fully block the player
        // from moving, clicking, or interacting with that exact Bundle item.
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (topHolder instanceof BundleInventoryHolder openedHolder) {
            if (clickConditions.isInteractingWithOpenedBundle(event)) {
                event.setCancelled(true);
                return;
            }

            // === PROTECT SLOT ITEMS INSIDE BUNDLE GUI ===
            if (clickConditions.isBlockedSlotInteractionInBundleGui(event) || clickConditions.isNumberKeyClick(event)) {
                event.setCancelled(true);
                return;
            }

            // Only schedule sync after removal-type clicks inside the Bundle GUI
            if (clickConditions.isRemovalClickInBundleGui(event) || clickConditions.isCursorDropClickInBundleGui(event)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    openedHolder.syncFromGuiInventory(event.getView().getTopInventory());
                    Inventory gui = event.getView().getTopInventory();
                    gui.setContents(openedHolder.getBundleItem().getInventory().getHandle().getContents());
                });
            }

            // === BUNDLE INVENTORY GUI HANDLING ===
            if (clickConditions.isShiftClickInsertionIntoBundleGui(event)) {
                event.setCancelled(true);
                bundleActions.handleGuiItemInsertion(player, event.getView().getTopInventory(), current);
                return;
            }

            if (clickConditions.isLeftClickInsertionIntoBundleGui(event)) {
                event.setCancelled(true);
                bundleActions.handleGuiItemInsertion(player, event.getView().getTopInventory(), cursor);
                return;
            }

            if (clickConditions.isRightClickInsertionIntoBundleGui(event)) {
                event.setCancelled(true);
                bundleActions.handleGuiSingleItemInsertion(player, event.getView().getTopInventory(), cursor);
                return;
            }
        }

        // === SLOT HANDLING ===
        // Slot insertion limit (12 unique items)
        if (clickConditions.isAttemptingSlotInsertion(event)) {
            event.setCancelled(true);
            ItemStack slotItem = itemFactory.isOurBundleSlot(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurBundleSlot(current) ? cursor : current;
            boolean isCancelled = bundleActions.handleSlotInsertion(player, slotItem, insertItem);
            event.setCancelled(isCancelled);
            return;
        }

        // SHIFT_RIGHT on Slot (empty into inventory)
        if (clickConditions.isShiftRightClickToEmptySlot(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            bundleActions.handleEmptySlot(player, cursor, targetInv);
            return;
        }

        // === BUNDLE HANDLING ===
        // SHIFT_RIGHT on Bundle (empty into inventory)
        if (clickConditions.isShiftRightClickToEmptyBundle(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            bundleActions.handleEmptyBundle(player, cursor, targetInv);
            return;
        }

        // SHIFT_RIGHT on vanilla original Bundle (empty all contents into inventory)
        if (clickConditions.isShiftRightClickToEmptyVanillaBundle(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            bundleActions.handleEmptyVanillaBundle(player, cursor, targetInv);
            return;
        }

        // LEFT CLICK on Bundle (insert item)
        if (clickConditions.isLeftClickToInsert(event)) {
            event.setCancelled(true);
            ItemStack bundleItem = itemFactory.isOurBundle(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurBundle(current) ? cursor : current;
            bundleActions.handleInsertItem(player, bundleItem, insertItem);
            return;
        }

        // RIGHT CLICK on Bundle (remove item)
        if (clickConditions.isRightClickToRemove(event)) {
            event.setCancelled(true);
            if (itemFactory.isOurBundle(cursor)) {
                Inventory targetInv = getTargetInventory(event);
                bundleActions.handleRemoveItem(player, cursor, targetInv, event.getSlot());
            } else {
                bundleActions.handleRemoveItem(player, current);
            }
            return;
        }
    }

    private Inventory getTargetInventory(InventoryClickEvent event) {
        return (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER)
                ? event.getClickedInventory()
                : event.getWhoClicked().getInventory();
    }

    /**
     * When a player closes a Bundle inventory GUI, save the changes
     * back to the specific BundleItem that was opened.
     *
     * Uses BundleInventoryHolder to identify the exact Bundle instead of
     * scanning the player's inventory (which was unreliable when multiple
     * Bundles were present).
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BundleInventoryHolder bundleHolder)) {
            return; // Not one of our Bundle inventories
        }

        bundleHolder.syncFromGuiInventory(event.getInventory());
        bundleHolder.refreshPlayerInventory(player);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || !itemFactory.isOurBundle(result)) return;

        // Replace with a fresh Bundle that has its own UUID
        ItemStack fresh = itemFactory.createCopperBundleItem(result.getAmount());
        event.getInventory().setResult(fresh);
    }
}
