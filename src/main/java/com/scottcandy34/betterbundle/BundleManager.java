package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BundleManager {

    private final BetterBundlePlugin plugin;
    private final BundleRepackService repackService;

    public BundleManager(BetterBundlePlugin plugin) {
        this.plugin = plugin;
        this.repackService = new BundleRepackService(plugin);
    }

    public BundleRepackService getRepackService() {
        return repackService;
    }

    public void repackBundle(Inventory bundleInv, int addition) {
        repackService.repackBundle(bundleInv, addition);
    }

    public Inventory getBundleInventory(ItemStack bundle) {
        ItemStack innerShulker = plugin.getItemFactory().getInnerShulker(bundle);
        if (innerShulker == null) return null;

        if (!(innerShulker.getItemMeta() instanceof BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return null;
        return shulker.getInventory();
    }

    public void saveBundleInventory(ItemStack bundle, Inventory inventory) {
        ItemStack innerShulker = plugin.getItemFactory().getInnerShulker(bundle);
        if (innerShulker == null) return;

        if (!(innerShulker.getItemMeta() instanceof BlockStateMeta bsm)) return;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return;

        shulker.getInventory().setContents(inventory.getContents());
        bsm.setBlockState(shulker);
        innerShulker.setItemMeta(bsm);

        // Put the updated inner shulker back into the head
        plugin.getItemFactory().setInnerShulker(bundle, innerShulker);
    }

    public int calculateWeight(Inventory inventory) {
        int weight = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            weight += getItemWeight(item);
        }
        return weight;
    }

    public int getItemWeight(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        if (isBlockedItem(item)) return Integer.MAX_VALUE;

        if (plugin.getItemFactory().isOurSlot(item)) {
            if (item.getItemMeta() instanceof BundleMeta meta) {
                int totalWeight = 0;
                for (ItemStack content : meta.getItems()) {
                    if (content != null && content.getType() != Material.AIR) {
                        totalWeight += getItemWeight(content);
                    }
                }
                return totalWeight;
            }
            return 0;
        }

        int amount = item.getAmount();
        int maxStack = item.getMaxStackSize();
        return (amount * 64 + maxStack - 1) / maxStack;
    }

    public int getSingleItemWeight(Material type) {
        if (type == null || type == Material.AIR) return 0;
        ItemStack temp = new ItemStack(type, 1);
        if (isBlockedItem(temp)) return Integer.MAX_VALUE;
        int maxStack = temp.getMaxStackSize();
        return (64 + maxStack - 1) / maxStack;
    }

    public boolean isBlockedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (plugin.getItemFactory().isOurSlot(item)) return false;
        Material type = item.getType();
        return type.name().contains("SHULKER") || type == Material.CHEST ||
               type == Material.ENDER_CHEST || type == Material.BUNDLE ||
               type.name().contains("BARREL");
    }

    public boolean hasItemInInventory(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return false;
        return calculateWeight(inv) > 0;
    }

    public void updateBundleLore(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return;
        int currentWeight = calculateWeight(inv);
        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        List<Component> lore = new ArrayList<>();
        if (currentWeight == 0) {
            lore.add(Component.text("Can hold a mixed", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("stack of items", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text("Reinforced with copper.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("Shift + Left-click to open.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Shift + Right-click to empty.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        bundle.setItemMeta(meta);
    }

    public void updateBundleDurability(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return;
        int weight = calculateWeight(inv);
        boolean isEmpty = weight == 0;
        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        if (isEmpty) {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(Constants.MAX_WEIGHT + 1);
                damageable.resetDamage();
            }
        } else {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(Constants.MAX_WEIGHT + 1);
                int damage = Constants.MAX_WEIGHT + 1 - weight;
                if (damage <= 0) damage = 1;
                damageable.setDamage(damage);
            }
        }
        bundle.setItemMeta(meta);
    }

    public void updateBundle(ItemStack bundle) {
        updateBundleLore(bundle);
        updateBundleDurability(bundle);
    }

    public boolean isOurInventoryView(InventoryView inventoryView) {
        String title = PlainTextComponentSerializer.plainText().serialize(inventoryView.title());
        return !"Bundle".equals(title);
    }

    public ItemStack removeLastItemFromBundle(Inventory bundleInv) {
        if (bundleInv == null) return null;
        List<ItemStack> extracted = extractLastItemsFromBundle(bundleInv, 1);
        return extracted.isEmpty() ? null : extracted.get(0);
    }

    public int getEmptySlotsInInventory(Inventory inventory) {
        if (inventory == null) return 0;

        int empty = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                empty++;
            }
        }
        return empty;
    }

    public List<ItemStack> extractLastItemsFromBundle(Inventory bundleInv, int maxToTake) {
        List<ItemStack> extracted = new ArrayList<>();
        if (bundleInv == null || maxToTake <= 0) return extracted;
        List<ItemStack> allItems = repackService.getAllItemsFromBundle(bundleInv);
        List<ItemStack> leftovers = new ArrayList<>();
        Collections.reverse(allItems);
        for (ItemStack item : allItems) {
            if (!plugin.getItemFactory().isOurSlot(item) && item != null && item.getType() != Material.AIR) {
                if (extracted.size() < maxToTake) extracted.add(item.clone());
                else leftovers.add(item.clone());
            }
        }
        bundleInv.clear();
        Collections.reverse(leftovers);
        Inventory rebuilt = repackService.repackBundle(leftovers);
        bundleInv.setContents(rebuilt.getContents());
        return extracted;
    }

    /**
     * Smartly adds as much as possible of the given item to the Bundle.
     * Uses normal 27 slots first, then existing Slots (respecting 12-unique limit),
     * then creates new Slots at the end if needed.
     * 
     * @return leftover ItemStack (with remaining amount) or null if everything was added
     */
    public ItemStack addItemToBundle(Inventory bundleInv, ItemStack toAdd) {
        if (toAdd == null || toAdd.getType() == Material.AIR) return null;
        int singleItemWeight = getSingleItemWeight(toAdd.getType());
        int currentWeight = calculateWeight(bundleInv);
        int remainingWeight = Constants.MAX_WEIGHT - currentWeight;
        if (remainingWeight < singleItemWeight) return toAdd.clone();

        int canAdd = Math.min(toAdd.getAmount(), remainingWeight / singleItemWeight);
        ItemStack portion = toAdd.clone();
        portion.setAmount(canAdd);
        HashMap<Integer, ItemStack> leftovers = bundleInv.addItem(portion);
        if (!leftovers.isEmpty()) {
            ItemStack remaining = leftovers.values().iterator().next();
            if (!repackService.tryAddToExistingSlot(bundleInv, remaining)) repackService.addNewSlotToBundle(bundleInv, remaining);
        }

        if (canAdd >= toAdd.getAmount()) {
            return null;
        } else {
            ItemStack leftover = toAdd.clone();
            leftover.setAmount(toAdd.getAmount() - canAdd);
            return leftover;
        }
    }

    public void performBundleRemoval(Player player, ItemStack mainHand) {
        Inventory bundleInv = getBundleInventory(mainHand);
        if (bundleInv == null) return;
        ItemStack removed = removeLastItemFromBundle(bundleInv);
        if (removed != null) {
            Item dropped = player.getWorld().dropItemNaturally(
                player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.3)), removed);
            dropped.setVelocity(player.getLocation().getDirection().multiply(0.3));
            BundleSound.DROP_CONTENTS.play(player);
            player.swingMainHand();
        } else {
            player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
        }
        saveBundleInventory(mainHand, bundleInv);
        updateBundle(mainHand);
    }
}
