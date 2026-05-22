package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BundleItem {

    private final ItemStack item;
    private final ItemFactory itemFactory = new ItemFactory();
    private ItemStack innerShulker;
    private BundleInventory bundleInventory;

    /**
     * Creates a BundleItem wrapper.
     * Automatically extracts the inner ShulkerBox from the item's PersistentDataContainer on creation.
     */
    public BundleItem(ItemStack item) {
        if (item == null || !itemFactory.isOurBundle(item)) {
            throw new IllegalArgumentException("ItemStack is not a valid Bundle item");
        }

        this.item = item;
        extractInnerShulker();
        this.bundleInventory = createBundleInventory();
    }

    /**
     * Extracts the inner ShulkerBox from this Bundle's PersistentDataContainer
     * and stores it in the `innerShulker` field.
     *
     * This is the parameterless counterpart to setInnerShulker().
     */
    private void extractInnerShulker() {
        if (!item.hasItemMeta()) {
            this.innerShulker = null;
            return;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        byte[] data = pdc.get(Constants.INNER_SHULKER_KEY, PersistentDataType.BYTE_ARRAY);

        if (data == null) {
            this.innerShulker = null;
            return;
        }

        try {
            this.innerShulker = ItemStack.deserializeBytes(data);
        } catch (Exception e) {
            this.innerShulker = null;
        }
    }

    /**
     * Saves the current inner ShulkerBox back into this Bundle's PersistentDataContainer.
     * Uses the already-extracted `innerShulker` field.
     */
    private void setInnerShulker() {
        if (innerShulker == null || !item.hasItemMeta()) {
            return;
        }

        byte[] serialized = innerShulker.serializeAsBytes();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Constants.INNER_SHULKER_KEY, PersistentDataType.BYTE_ARRAY, serialized);
        item.setItemMeta(meta);
    }

    private BundleInventory createBundleInventory() {
        if (innerShulker == null) return null;

        if (!(innerShulker.getItemMeta() instanceof BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return null;

        Inventory handle = shulker.getInventory();
        return new BundleInventory(handle);
    }

    /**
     * Returns a BundleInventory wrapper for this bundle's contents.
     * This gives access to addItem, removeItem, repack, weight, etc.
     */
    public BundleInventory getInventory() {
        return bundleInventory;
    }

    /**
     * Saves the contents of a BundleInventory back into this Bundle.
     *
     * This writes the live inventory state into the inner ShulkerBox,
     * then persists the updated ShulkerBox back into the Bundle's
     * PersistentDataContainer via setInnerShulker().
     *
     * @param inventory The BundleInventory that was modified (usually from getInventory())
     */
    public void saveInventory() {
        if (bundleInventory == null || innerShulker == null) {
            return;
        }

        Inventory liveInventory = bundleInventory.getHandle();
        if (liveInventory == null) {
            return;
        }

        if (!(innerShulker.getItemMeta() instanceof BlockStateMeta bsm)) {
            return;
        }
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) {
            return;
        }

        // Copy the live edited contents back into the inner ShulkerBox
        shulker.getInventory().setContents(liveInventory.getContents());
        bsm.setBlockState(shulker);
        innerShulker.setItemMeta(bsm);

        // Persist the updated inner Shulker back into the PLAYER_HEAD
        setInnerShulker();
    }

    /**
     * Updates the lore on this Bundle item based on its current contents.
     * Shows capacity hints and usage instructions.
     */
    private void updateLore() {
        if (!item.hasItemMeta()) return;

        BundleInventory inv = getInventory();
        boolean isEmpty = (inv == null) || inv.isEmpty();

        List<Component> lore = new ArrayList<>();

        if (isEmpty) {
            lore.add(Component.text("Can hold a mixed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("stack of items", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.text("Reinforced with copper.", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("Shift + Left-click to open.", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Shift + Right-click to empty.", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));

        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Updates the durability bar on this Bundle item.
     * The bar visually represents how full the bundle is (inverted).
     * Empty = full bar, Full = almost empty bar.
     */
    private void updateDurability() {
        if (!item.hasItemMeta()) return;

        BundleInventory inv = getInventory();
        if (inv == null) return;

        int weight = inv.getWeight();
        boolean isEmpty = weight == 0;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        damageable.setMaxDamage(Constants.MAX_WEIGHT + 1);

        if (isEmpty) {
            damageable.resetDamage();
        } else {
            int damage = Constants.MAX_WEIGHT + 1 - weight;
            if (damage <= 0) damage = 1;
            damageable.setDamage(damage);
        }

        item.setItemMeta(meta);
    }

    /**
     * Should be called after the player closes this Bundle's GUI.
     * Runs internal cleanup logic (pruning empty Slots, repacking, etc.)
     * so the Bundle stays in a clean state.
     */
    public void repackAfterGuiClose() {
        if (bundleInventory != null) {
            bundleInventory.repackContents();
        }
    }

    /**
     * Updates both the lore and durability bar on this Bundle item.
     * Call this after modifying the bundle's contents (e.g. after saveInventory).
     */
    public void update() {
        updateLore();
        updateDurability();
        saveInventory();
    }

    /**
     * Returns the total weight of all items inside this Bundle,
     * including items stored inside any Slots.
     */
    public int getWeight() {
        BundleInventory inv = getInventory();
        if (inv == null) {
            return 0;
        }
        return inv.getWeight();
    }

    /**
     * Returns true if this Bundle has reached its maximum capacity.
     * Checks both the weight limit (64) and the maximum individual stack count (27 * 12).
     */
    public boolean isFull() {
        BundleInventory inv = getInventory();
        if (inv == null) {
            return true; // Treat as full if we can't access the inventory
        }
        return inv.isFull();
    }

    /**
     * Returns true if this Bundle contains no items at all
     * (including items inside any Slots).
     */
    public boolean isEmpty() {
        BundleInventory inv = getInventory();
        if (inv == null) {
            return true;
        }
        return inv.isEmpty();
    }

    /**
     * Returns the actual Bundle item (the PLAYER_HEAD) that this wrapper represents.
     * This is the ItemStack the player holds in their inventory.
     */
    public ItemStack getBundle() {
        update();
        return item.clone();
    }

    /**
     * Opens this Bundle's inventory GUI for the given player.
     *
     * Uses a temporary inventory backed by BundleInventoryHolder so that
     * BundleListener can reliably identify which specific Bundle was opened
     * and save changes back to the correct ItemStack on close.
     */
    public void open(Player player) {
        if (player == null) {
            return;
        }

        BundleInventory bundleInv = getInventory();
        if (bundleInv == null) {
            return;
        }

        Inventory handle = bundleInv.getHandle();
        if (handle == null) {
            return;
        }

        // Create a fresh inventory view with our holder for proper tracking
        Inventory view = Bukkit.createInventory(
            new BundleInventoryHolder(this),
            27,
            Component.text("Bundle", NamedTextColor.GOLD)
        );

        // Copy current contents from the inner Shulker into the view
        view.setContents(handle.getContents());

        player.openInventory(view);
    }

    /**
     * Clears all items from this Bundle.
     * 
     * This removes everything inside the bundle (including items stored in Slots).
     * After clearing, lore and durability are automatically updated.
     */
    public void clear() {
        BundleInventory inv = getInventory();
        if (inv != null) {
            inv.clear();
        }

        // Update lore + durability so the item reflects the empty state
        update();
    }

    /**
     * Returns the unique ID assigned to this Bundle.
     * Used for safely identifying which specific Bundle to update in the player's inventory.
     */
    public UUID getBundleId() {
        if (!item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String idString = pdc.get(Constants.BUNDLE_UUID_KEY, PersistentDataType.STRING);
        if (idString == null) return null;
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}