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

    private final NamespacedKey innerShulkerKey;
    private final BetterBundlePlugin plugin;
    private final NamespacedKey bundleKey;
    private final NamespacedKey slotKey;

    public ItemFactory(BetterBundlePlugin plugin, NamespacedKey bundleKey, NamespacedKey slotKey) {
        this.plugin = plugin;
        this.bundleKey = bundleKey;
        this.slotKey = slotKey;
        this.innerShulkerKey = new NamespacedKey(plugin, "inner_shulker");
    }
    
    public ItemStack createSlotItem(int amount) {
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
            pdc.set(slotKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isOurSlot(ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(slotKey, PersistentDataType.BYTE);
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

            meta.displayName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.setMaxStackSize(1);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(bundleKey, PersistentDataType.BYTE, (byte) 1);

            // Create and store inner ShulkerBox
            ItemStack innerShulker = createInnerShulker();
            byte[] serialized = serializeItemStack(innerShulker);
            if (serialized != null) {
                pdc.set(innerShulkerKey, PersistentDataType.BYTE_ARRAY, serialized);
            }

            head.setItemMeta(meta);
            plugin.updateBundle(head);
        }
        return head;
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

    /**
     * Serializes an ItemStack into a byte array for storage in PDC.
     */
    private byte[] serializeItemStack(ItemStack item) {
        if (item == null) return null;
        return item.serializeAsBytes();
    }

    /**
     * Deserializes a byte array back into an ItemStack.
     */
    private ItemStack deserializeItemStack(byte[] data) {
        if (data == null) return null;
        try {
            return ItemStack.deserializeBytes(data);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize inner shulker: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the inner ShulkerBox from a Bundle head.
     */
    public ItemStack getInnerShulker(ItemStack bundleHead) {
        if (!isOurBundle(bundleHead) || !bundleHead.hasItemMeta()) return null;

        PersistentDataContainer pdc = bundleHead.getItemMeta().getPersistentDataContainer();
        byte[] data = pdc.get(innerShulkerKey, PersistentDataType.BYTE_ARRAY);
        return deserializeItemStack(data);
    }

    /**
     * Stores an updated inner ShulkerBox back into the Bundle head.
     */
    public void setInnerShulker(ItemStack bundleHead, ItemStack innerShulker) {
        if (!isOurBundle(bundleHead) || innerShulker == null || !bundleHead.hasItemMeta()) return;

        ItemMeta meta = bundleHead.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        byte[] serialized = serializeItemStack(innerShulker);
        if (serialized != null) {
            pdc.set(innerShulkerKey, PersistentDataType.BYTE_ARRAY, serialized);
            bundleHead.setItemMeta(meta);
        }
    }

    

    public boolean isOurBundle(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(bundleKey, PersistentDataType.BYTE);
    }
}
