package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import net.kyori.adventure.text.minimessage.MiniMessage;

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
        if (bundleInventory != null) {
            bundleInventory.setMaxWeight(getMaxWeight());
        }
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

        // === Show last 4 items (most recently added) at the top of the lore ===
        if (!isEmpty && inv != null) {
            List<ItemStack> contents = inv.getContents();
            int start = Math.max(0, contents.size() - 4);
            List<ItemStack> recentItems = contents.subList(start, contents.size());

            // Loop backwards so the newest item is added to lore first (appears at top)
            for (int i = recentItems.size() - 1; i >= 0; i--) {
                ItemStack recent = recentItems.get(i);
                if (recent == null || recent.getType() == Material.AIR) continue;

                String itemName = recent.getType().name().toLowerCase().replace('_', ' ');
                String line = itemName + " x" + recent.getAmount();

                lore.add(Component.text(line, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true));
            }
        }

        if (isEmpty) {
            int stackCount = Math.max(1, getMaxWeight() / Constants.DEFAULT_MAX_WEIGHT);
            String stacksWord = (stackCount == 1) ? "stack" : "stacks";

            lore.add(Component.text("Can hold " + stackCount + " mixed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(stacksWord + " of items", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }

        // === Weight Progress Bar (always visible, even when empty) ===
        int currentWeight = (inv != null) ? inv.getWeight() : 0;
        lore.add(createWeightBar(currentWeight, getMaxWeight()));

        lore.add(Component.text("Reinforced with copper.", NamedTextColor.GRAY)
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

        damageable.setMaxDamage(getMaxWeight() + 1);

        if (isEmpty) {
            damageable.resetDamage();
        } else {
            int damage = getMaxWeight() + 1 - weight;
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
            getInventory().repackContents();
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
     * Returns the maximum weight capacity of this Bundle.
     * This value is stored on the item itself for easy customization.
     */
    public int getMaxWeight() {
        if (this.item == null || !this.item.hasItemMeta()) {
            return Constants.DEFAULT_MAX_WEIGHT;
        }
        PersistentDataContainer pdc = this.item.getItemMeta().getPersistentDataContainer();
        Integer weight = pdc.get(Constants.BUNDLE_MAX_WEIGHT_KEY, PersistentDataType.INTEGER);
        return weight != null ? weight : Constants.DEFAULT_MAX_WEIGHT;
    }

    /**
     * Sets a new maximum weight capacity for this Bundle.
     */
    public void setMaxWeight(int maxWeight) {
        if (this.item == null || !this.item.hasItemMeta()) return;
        
        if (maxWeight % 64 != 0) {
            throw new IllegalArgumentException("Max weight must be a multiple of 64");
        }

        ItemMeta meta = this.item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Constants.BUNDLE_MAX_WEIGHT_KEY, PersistentDataType.INTEGER, maxWeight);
        this.item.setItemMeta(meta);

        // Keep the live BundleInventory in sync
        if (bundleInventory != null) {
            bundleInventory.setMaxWeight(maxWeight);
        }
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
     * Creates a visual weight progress bar using square characters.
     * Built with Adventure Components for better styling control.
     * Blue when not full, Red when completely full.
     */
    private Component createWeightBar(int current, int max) {
        int barLength = 16;
        double ratio = max > 0 ? Math.min(1.0, (double) current / max) : 0;
        int filled = (int) Math.round(barLength * ratio);

        // Guarantee at least one blue █ if the bundle has any weight
        if (current > 0 && filled == 0) {
            filled = 1;
        }

        StringBuilder sb = new StringBuilder();

        // Filled part
        String filledChar = (current >= max) ? "<red>█</red>" : "<blue>█</blue>";
        sb.append(filledChar.repeat(filled));

        // Empty part
        sb.append("<dark_gray>░</dark_gray>".repeat(barLength - filled));

        return MiniMessage.miniMessage().deserialize(sb.toString());
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