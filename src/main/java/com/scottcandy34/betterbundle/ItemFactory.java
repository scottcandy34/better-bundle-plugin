package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ItemFactory {
    
    /**
     * Checks if an item is blocked from being stored in a Bundle.
     * Blocked items are containers like shulkers, chests, barrels, ender chests, and other bundles.
     * Our own Slot items are explicitly allowed.
     */
    public boolean isBlockedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (isOurBundleSlot(item)) return false;
        Material type = item.getType();
        return type.name().contains("SHULKER") || type == Material.CHEST ||
               type == Material.ENDER_CHEST || type == Material.BUNDLE ||
               type.name().contains("BARREL");
    }
    
    public ItemStack createBundleSlotItem(int amount) {
        ItemStack item = new ItemStack(Material.BUNDLE, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setItemModel(NamespacedKey.minecraft("shulker_spawn_egg"));

            meta.displayName(Component.text("Slot", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Special slot item", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(Constants.SLOT_KEY, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isOurBundleSlot(ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(Constants.SLOT_KEY, PersistentDataType.BYTE);
    }

    public ItemStack createBundleItem(int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        ItemMeta meta = head.getItemMeta();

        if (meta != null) {
            // Apply custom player head texture (modern Paper way)
            if (meta instanceof SkullMeta skullMeta) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", Constants.BUNDLE_HEAD_TEXTURE));
                skullMeta.setPlayerProfile(profile);
            }

            // Use original vanilla Bundle model (so it looks like a classic bundle in inventory/hotbar)
            meta.setItemModel(NamespacedKey.minecraft("bundle"));

            meta.displayName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.setMaxStackSize(1);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(Constants.BUNDLE_KEY, PersistentDataType.BYTE, (byte) 1);

            UUID bundleId = UUID.randomUUID();
            pdc.set(Constants.BUNDLE_UUID_KEY, PersistentDataType.STRING, bundleId.toString());

            // Create and store inner ShulkerBox
            ItemStack innerShulker = createInnerShulker();
            byte[] serialized = innerShulker.serializeAsBytes();
            if (serialized != null) {
                pdc.set(Constants.INNER_SHULKER_KEY, PersistentDataType.BYTE_ARRAY, serialized);
            }


            // Initialize lore + durability on the head
            List<Component> lore = new ArrayList<>();

            lore.add(Component.text("Can hold a mixed", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("stack of items", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Reinforced with copper.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("Shift + Left-click to open.", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift + Right-click to empty.", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(Constants.MAX_WEIGHT + 1);
                damageable.resetDamage();
            }

            head.setItemMeta(meta);
        }
        return head;
    }

    public boolean isOurBundle(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(Constants.BUNDLE_KEY, PersistentDataType.BYTE);
    }

    /**
     * Creates a clean inner ShulkerBox used for storage.
     */
    private ItemStack createInnerShulker() {
        ItemStack shulkerItem = new ItemStack(Material.SHULKER_BOX);
        if (shulkerItem.getItemMeta() instanceof BlockStateMeta bsm) {
            ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
            shulker.getInventory().clear();
            shulker.customName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            bsm.setBlockState(shulker);
            shulkerItem.setItemMeta(bsm);
        }
        return shulkerItem;
    }

    public boolean isOriginalBundle(ItemStack item) {
        return Constants.ORIGINAL_BUNDLES.contains(item.getType());
    }


}
