package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BundleActions {

    private final ItemFactory itemFactory = new ItemFactory();

    public boolean handleSlotInsertion(Player player, ItemStack slotItem, ItemStack insertItem) {
        try {
            BundleSlotInventory slotInv = new BundleSlotInventory(slotItem);

            if (slotInv.canAccept(insertItem)) {
                return false; // Let Minecraft handle the insertion normally
            }

            BundleSound.INSERT_FAIL.play(player);
            player.sendMessage(Component.text("Slot can only hold 12 different items!", NamedTextColor.RED));
            return true;

        } catch (IllegalArgumentException e) {
            return false; // Not one of our Slots
        }
    }

    public void handleEmptySlot(Player player, ItemStack slotItem, Inventory clickedInv) {
        try {
            BundleSlotInventory slotInv = new BundleSlotInventory(slotItem);

            int emptySlots = getEmptySlotsInInventory(clickedInv);
            if (emptySlots <= 0) {
                player.sendMessage(Component.text("No empty space available!", NamedTextColor.RED));
                return;
            }

            List<ItemStack> extracted = slotInv.removeItems(emptySlots);

            if (!extracted.isEmpty()) {
                HashMap<Integer, ItemStack> leftovers = clickedInv.addItem(extracted.toArray(new ItemStack[0]));
                if (!leftovers.isEmpty()) {
                    // Put any items that didn't fit back into the Slot
                    slotInv.addItems(new ArrayList<>(leftovers.values()));
                }

                // Update the original Slot item with new meta
                slotItem.setItemMeta(slotInv.getBundleSlot().getItemMeta());
            }

            if (extracted.isEmpty()) {
                player.sendMessage(Component.text("The Slot is empty.", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("Emptied " + extracted.size() + " item(s) from the end!", NamedTextColor.GRAY));
                BundleSound.DROP_CONTENTS.play(player);
            }

        } catch (IllegalArgumentException e) {
            // Not one of our Slots — do nothing
        }
    }

    public void handleOpenBundle(Player player, ItemStack bundleItem) {
        try {
            BundleItem bundle = new BundleItem(bundleItem);

            bundle.update();
            bundle.open(player);

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    public void handleEmptyBundle(Player player, ItemStack bundleItem, Inventory targetInv) {
        try {
            BundleItem bundle = new BundleItem(bundleItem);
            BundleInventory bundleInv = bundle.getInventory();
            if (bundleInv == null) return;

            int emptySlots = getEmptySlotsInInventory(targetInv);
            if (emptySlots <= 0) {
                player.sendMessage(Component.text("No empty space available!", NamedTextColor.RED));
                return;
            }

            List<ItemStack> extracted = bundleInv.removeItems(emptySlots);

            if (!extracted.isEmpty()) {
                HashMap<Integer, ItemStack> leftovers = targetInv.addItem(extracted.toArray(new ItemStack[0]));
                if (!leftovers.isEmpty()) {
                    bundleInv.addItems(new ArrayList<>(leftovers.values()));
                }
            }

            bundle.update();

            if (extracted.isEmpty()) {
                player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("Emptied " + extracted.size() + " item(s) from the end!", NamedTextColor.GRAY));
                BundleSound.DROP_CONTENTS.play(player);
            }

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    public void handleGuiItemInsertion(Player player, Inventory guiInventory, ItemStack insertItem) {
        if (!(guiInventory.getHolder() instanceof BundleInventoryHolder holder)) return;
        BundleItem bundleItem = holder.getBundleItem();
        if (bundleItem == null) return;

        BundleInventory bundleInv = bundleItem.getInventory();
        if (bundleInv == null) return;

        if (itemFactory.isBlockedItem(insertItem)) {
            player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
            BundleSound.INSERT_FAIL.play(player);
            return;
        }

        ItemStack leftover = bundleInv.addItem(insertItem);

        if (leftover != null) {
            insertItem.setAmount(leftover.getAmount());
        } else {
            insertItem.setAmount(0);
        }

        // Play appropriate sound
        if (leftover != null && leftover.getAmount() == insertItem.getAmount()) {
            BundleSound.INSERT_FAIL.play(player);
        } else {
            BundleSound.INSERT.play(player);
        }

        bundleItem.update();
        guiInventory.setContents(bundleItem.getInventory().getHandle().getContents());
    }

    public void handleGuiSingleItemInsertion(Player player, Inventory guiInventory, ItemStack cursor) {
        if (!(guiInventory.getHolder() instanceof BundleInventoryHolder holder)) return;
        BundleItem bundleItem = holder.getBundleItem();
        if (bundleItem == null) return;

        BundleInventory bundleInv = bundleItem.getInventory();
        if (bundleInv == null) return;

        if (itemFactory.isBlockedItem(cursor)) {
            player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
            BundleSound.INSERT_FAIL.play(player);
            return;
        }

        ItemStack single = cursor.clone();
        single.setAmount(1);

        ItemStack leftover = bundleInv.addItem(single);

        if (leftover != null) {
            cursor.setAmount(cursor.getAmount());
        } else {
            cursor.setAmount(cursor.getAmount() - 1);
        }

        bundleItem.update();
        guiInventory.setContents(bundleItem.getInventory().getHandle().getContents());
        BundleSound.INSERT.play(player);
    }

    public void handleInsertItem(Player player, ItemStack bundleItem, ItemStack insertItem) {
        try {
            BundleItem bundle = new BundleItem(bundleItem);
            BundleInventory bundleInv = bundle.getInventory();
            if (bundleInv == null) return;

            if (itemFactory.isBlockedItem(insertItem)) {
                player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
                BundleSound.INSERT_FAIL.play(player);
                return;
            }

            ItemStack leftover = bundleInv.addItem(insertItem);

            // Update cursor amount (Minecraft behavior)
            if (leftover != null) {
                insertItem.setAmount(leftover.getAmount());
            } else {
                insertItem.setAmount(0);
            }

            // Play appropriate sound
            if (leftover != null && leftover.getAmount() == insertItem.getAmount()) {
                BundleSound.INSERT_FAIL.play(player);
            } else {
                BundleSound.INSERT.play(player);
            }

            bundle.update();

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    public void handleRemoveItem(Player player, ItemStack bundleItem, Inventory clickedInv, int slot) {
        try {
            BundleItem bundle = new BundleItem(bundleItem);
            BundleInventory bundleInv = bundle.getInventory();
            if (bundleInv == null) return;

            ItemStack removed = bundleInv.removeItem();

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

            bundle.update();

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    public void handleRemoveItem(Player player, ItemStack bundleItem) {
        try {
            BundleItem bundle = new BundleItem(bundleItem);
            BundleInventory bundleInv = bundle.getInventory();
            if (bundleInv == null) return;

            ItemStack removed = bundleInv.removeItem();

            if (removed != null) {
                player.setItemOnCursor(removed.clone());
                BundleSound.REMOVE_ONE.play(player);
            } else {
                player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            }

            bundle.update();

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    public void performBundleRemoval(Player player, ItemStack mainHand) {
        try {
            BundleItem bundle = new BundleItem(mainHand);
            BundleInventory bundleInv = bundle.getInventory();
            if (bundleInv == null) return;

            ItemStack removed = bundleInv.removeItem();

            if (removed != null) {
                // Drop exactly like the player pressed Q (vanilla behavior)
                Item dropped = player.getWorld().dropItemNaturally(
                    player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.3)), removed);
                dropped.setVelocity(player.getLocation().getDirection().multiply(0.3));

                BundleSound.DROP_CONTENTS.play(player);
                player.swingMainHand();
            } else {
                player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            }

            bundle.update();

        } catch (IllegalArgumentException e) {
            // Not one of our Bundles — ignore
        }
    }

    private int getEmptySlotsInInventory(Inventory inventory) {
        if (inventory == null) return 0;

        int empty = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                empty++;
            }
        }
        return empty;
    }
}
