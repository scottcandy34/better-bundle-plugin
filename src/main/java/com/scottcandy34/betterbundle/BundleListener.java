package com.scottcandy34.betterbundle;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BundleListener implements Listener {

    private final BetterBundlePlugin plugin;
    private final ItemFactory itemFactory = new ItemFactory();
    private final BundleManager bundleManager = new BundleManager();
    private final BundleActions bundleActions = new BundleActions();
    private final BundleClickConditions clickConditions = new BundleClickConditions();

    public BundleListener(BetterBundlePlugin plugin) {
        this.plugin = plugin;
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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getType() == InventoryType.CREATIVE) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // === SLOT HANDLING ===
        // Slot insertion limit (12 unique items)
        if (clickConditions.isAttemptingSlotInsertion(event)) {
            event.setCancelled(true);
            ItemStack slotItem = itemFactory.isOurBundleSlot(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurBundleSlot(current) ? cursor : current;
            boolean isCancelled = bundleActions.handleSlotInsertion(player, slotItem, insertItem);
            event.setCancelled(isCancelled);
        }

        // SHIFT_RIGHT on Slot (empty into inventory)
        else if (clickConditions.isShiftRightClickToEmptySlot(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            bundleActions.handleEmptySlot(player, cursor, targetInv);
        }

        // === BUNDLE HANDLING ===
        // SHIFT_LEFT → Open bundle
        else if (clickConditions.isShiftLeftClickToOpen(event)) {
            event.setCancelled(true);
            bundleActions.handleOpenBundle(player, current);
        }

        // SHIFT_RIGHT on Bundle (empty into inventory)
        else if (clickConditions.isShiftRightClickToEmptyBundle(event)) {
            event.setCancelled(true);
            Inventory targetInv = getTargetInventory(event);
            bundleActions.handleEmptyBundle(player, cursor, targetInv);
        }

        // LEFT CLICK on Bundle (insert item)
        else if (clickConditions.isLeftClickToInsert(event)) {
            event.setCancelled(true);
            ItemStack bundleItem = itemFactory.isOurBundle(current) ? current : cursor;
            ItemStack insertItem = itemFactory.isOurBundle(current) ? cursor : current;
            bundleActions.handleInsertItem(player, bundleItem, insertItem);
        }

        // RIGHT CLICK on Bundle (remove item)
        else if (clickConditions.isRightClickToRemove(event)) {
            event.setCancelled(true);
            if (itemFactory.isOurBundle(cursor)) {
                Inventory targetInv = getTargetInventory(event);
                bundleActions.handleRemoveItem(player, cursor, targetInv, event.getSlot());
            } else {
                bundleActions.handleRemoveItem(player, current);
            }
        }
    }

    private Inventory getTargetInventory(InventoryClickEvent event) {
        return (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER)
                ? event.getClickedInventory()
                : event.getWhoClicked().getInventory();
    }

    // When player closes the bundle GUI, refresh lore + durability bar on all bundles
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (!bundleManager.isOurInventoryView(event.getView())) {
            return;
        }

        Inventory closedInv = event.getInventory();

        // Save the edited GUI inventory into every Bundle the player is currently holding
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                try {
                    BundleItem bundle = new BundleItem(item);
                    bundle.saveInventory(new BundleInventory(closedInv));
                    bundle.update();
                } catch (IllegalArgumentException ignored) {
                    // Not one of our Bundles
                }
            }
        }

        // Also handle the item on the cursor
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null) {
            try {
                BundleItem bundle = new BundleItem(cursor);
                bundle.saveInventory(new BundleInventory(closedInv));
                bundle.update();
            } catch (IllegalArgumentException ignored) {
                // Not one of our Bundles
            }
        }
    }
}
