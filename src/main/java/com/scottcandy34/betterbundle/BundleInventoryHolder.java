package com.scottcandy34.betterbundle;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import org.jetbrains.annotations.Nullable;

/**
 * Custom InventoryHolder implementation that ties an opened Bundle GUI
 * back to its originating BundleItem.
 *
 * This allows BundleListener to reliably identify which specific Bundle
 * was opened when the inventory is closed, so changes can be saved back
 * to the correct ItemStack instead of guessing.
 */
public class BundleInventoryHolder implements InventoryHolder {

    private final BundleItem bundleItem;

    /**
     * Creates a new holder linked to the given BundleItem.
     *
     * @param bundleItem The BundleItem that owns this inventory view
     */
    public BundleInventoryHolder(BundleItem bundleItem) {
        this.bundleItem = bundleItem;
    }

    /**
     * Returns the BundleItem associated with this inventory holder.
     *
     * @return The originating BundleItem
     */
    public BundleItem getBundleItem() {
        return bundleItem;
    }

    /**
     * Returns null because this holder does not manage a persistent
     * inventory on its own — it is only used as a marker to identify
     * which Bundle the player has open.
     */
    @Override
    public @Nullable Inventory getInventory() {
        return null;
    }

    /**
     * Copies the current contents of the GUI inventory the player was viewing
     * back into this Bundle's internal inventory handle, then calls update()
     * so that lore, durability bar, and the PersistentDataContainer are refreshed.
     */
    public void syncFromGuiInventory(Inventory guiInventory) {
        if (bundleItem == null || guiInventory == null) {
            return;
        }

        Inventory handle = bundleItem.getInventory().getHandle();
        if (handle != null) {
            handle.setContents(guiInventory.getContents());
        }

        // This triggers saveInventory(bundleInventory) + lore/durability refresh
        bundleItem.update();
    }
}