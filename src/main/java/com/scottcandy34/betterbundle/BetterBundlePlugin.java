package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BetterBundlePlugin extends JavaPlugin implements Listener {

    private NamespacedKey bundleKey;
    private NamespacedKey contentsKey;

    private static final int MAX_WEIGHT = 64;
    private static final NamespacedKey RECIPE_KEY = new NamespacedKey("betterbundle", "bundle_recipe");

    @Override
    public void onEnable() {
        bundleKey = new NamespacedKey(this, "is_bundle");
        contentsKey = new NamespacedKey(this, "bundle_contents");

        getServer().getPluginManager().registerEvents(this, this);

        registerBundleRecipe();

        getLogger().info("BetterBundle plugin enabled for Paper 26.1.2! Recreated from bundles_mod.");
        getLogger().info("Craft a Bundle with 5 Rabbit Hide + 2 String (shaped recipe).");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterBundle disabled.");
    }

    private void registerBundleRecipe() {
        ItemStack bundle = createBundleItem(1);

        ShapedRecipe recipe = new ShapedRecipe(RECIPE_KEY, bundle);
        recipe.shape(
            " S ",
            "HHH",
            " H "
        );
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.RABBIT_HIDE);

        Bukkit.addRecipe(recipe);
        getLogger().info("Registered custom Bundle crafting recipe.");
    }

    /**
     * Creates a new Bundle item stack with proper meta and PDC marker.
     */
    public ItemStack createBundleItem(int amount) {
        // Use LEATHER instead of BUNDLE to avoid vanilla bundle behavior
        ItemStack item = new ItemStack(Material.LEATHER, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A portable storage pouch.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("Right-click to add/remove items.", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift + Right-click to empty.", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            // Mark as our custom bundle
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(bundleKey, PersistentDataType.BYTE, (byte) 1);
            pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, new byte[0]);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isOurBundle(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER || !item.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(bundleKey, PersistentDataType.BYTE);
    }

    private byte[] serializeItems(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return new byte[0];
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeInt(items.size());
            for (ItemStack item : items) {
                boos.writeObject(item);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            getLogger().warning("Failed to serialize bundle contents: " + e.getMessage());
            return new byte[0];
        }
    }

    @SuppressWarnings("unchecked")
    private List<ItemStack> deserializeItems(byte[] data) {
        List<ItemStack> items = new ArrayList<>();
        if (data == null || data.length == 0) {
            return items;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            int size = bois.readInt();
            for (int i = 0; i < size; i++) {
                Object obj = bois.readObject();
                if (obj instanceof ItemStack) {
                    items.add((ItemStack) obj);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            getLogger().warning("Failed to deserialize bundle contents: " + e.getMessage());
        }
        return items;
    }

    private int calculateWeight(List<ItemStack> items) {
        int weight = 0;
        for (ItemStack item : items) {
            if (item == null) continue;
            weight += getItemWeight(item);
        }
        return weight;
    }

    private int getItemWeight(ItemStack item) {
        Material type = item.getType();
        if (isBlockedItem(type)) {
            return Integer.MAX_VALUE;
        }
        if (type.name().contains("SHULKER") || type.name().contains("CHEST") ||
            type == Material.ENDER_CHEST || type == Material.BUNDLE ||
            type.name().endsWith("_AXE") || type.name().endsWith("_PICKAXE") ||
            type.name().endsWith("_SHOVEL") || type.name().endsWith("_HOE") ||
            type.name().endsWith("_SWORD") || type == Material.BOW || type == Material.CROSSBOW ||
            type.name().contains("HELMET") || type.name().contains("CHESTPLATE") ||
            type.name().contains("LEGGINGS") || type.name().contains("BOOTS") ||
            type == Material.ENDER_PEARL || type == Material.TOTEM_OF_UNDYING) {
            return 4;
        }
        return 1;
    }

    private boolean isBlockedItem(Material type) {
        return type.name().contains("SHULKER") || type == Material.CHEST ||
               type == Material.ENDER_CHEST || type == Material.BUNDLE ||
               type.name().contains("BARREL");
    }

    private void updateBundleLore(ItemStack bundle, List<ItemStack> contents) {
        if (!bundle.hasItemMeta()) return;
        ItemMeta meta = bundle.getItemMeta();
        int currentWeight = calculateWeight(contents);
        int percent = (int) ((currentWeight / (double) MAX_WEIGHT) * 100);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("A portable storage pouch.", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("Weight: " + currentWeight + "/" + MAX_WEIGHT + " (" + percent + "%)",
            currentWeight > MAX_WEIGHT * 0.8 ? NamedTextColor.RED : NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Right-click: Add/Remove | Shift+Right: Empty", NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        bundle.setItemMeta(meta);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!isOurBundle(item)) return;

        event.setCancelled(true);

        var player = event.getPlayer();
        boolean isSneaking = player.isSneaking();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        byte[] data = pdc.getOrDefault(contentsKey, PersistentDataType.BYTE_ARRAY, new byte[0]);
        List<ItemStack> contents = deserializeItems(data);

        if (isSneaking) {
            if (!contents.isEmpty()) {
                for (ItemStack contentItem : contents) {
                    if (contentItem != null && contentItem.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), contentItem);
                    }
                }
                contents.clear();
                pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeItems(contents));
                item.setItemMeta(meta);
                updateBundleLore(item, contents);
                player.sendMessage(Component.text("Bundle emptied!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Bundle is already empty.", NamedTextColor.GRAY));
            }
            return;
        }

        if (offHand != null && offHand.getType() != Material.AIR) {
            int itemWeight = getItemWeight(offHand);
            int currentWeight = calculateWeight(contents);

            if (isBlockedItem(offHand.getType())) {
                player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
                return;
            }

            if (currentWeight + itemWeight > MAX_WEIGHT) {
                player.sendMessage(Component.text("Not enough space in the Bundle! (" + currentWeight + "/" + MAX_WEIGHT + ")", NamedTextColor.RED));
                return;
            }

            int canAdd = Math.min(offHand.getAmount(), (MAX_WEIGHT - currentWeight) / Math.max(1, itemWeight));
            if (canAdd > 0) {
                ItemStack added = offHand.clone();
                added.setAmount(canAdd);
                contents.add(added);
                offHand.setAmount(offHand.getAmount() - canAdd);
                if (offHand.getAmount() <= 0) {
                    player.getInventory().setItemInOffHand(null);
                }

                pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeItems(contents));
                item.setItemMeta(meta);
                updateBundleLore(item, contents);
                player.sendMessage(Component.text("Added " + canAdd + "x " + added.getType().name().toLowerCase().replace('_', ' ') + " to Bundle.", NamedTextColor.GREEN));
            }
        } else {
            if (!contents.isEmpty()) {
                ItemStack lastItem = contents.remove(contents.size() - 1);
                if (player.getInventory().addItem(lastItem).isEmpty()) {
                    player.sendMessage(Component.text("Removed 1x " + lastItem.getType().name().toLowerCase().replace('_', ' ') + " from Bundle (LIFO).", NamedTextColor.YELLOW));
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), lastItem);
                    player.sendMessage(Component.text("Inventory full! Dropped item.", NamedTextColor.RED));
                }
                pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeItems(contents));
                item.setItemMeta(meta);
                updateBundleLore(item, contents);
            } else {
                player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();      // The Bundle (in cursor)
        ItemStack current = event.getCurrentItem(); // Item in the clicked slot

        if (!isOurBundle(cursor)) return;

        event.setCancelled(true); // Prevent vanilla behavior

        ItemMeta meta = cursor.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        List<ItemStack> contents = deserializeItems(
                pdc.getOrDefault(contentsKey, PersistentDataType.BYTE_ARRAY, new byte[0])
        );

        // === LEFT CLICK: Add item from slot into Bundle ===
        if (event.getClick().isLeftClick() && current != null && current.getType() != Material.AIR) {
            int itemWeight = getItemWeight(current);
            int currentWeight = calculateWeight(contents);

            if (isBlockedItem(current.getType())) {
                player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
                return;
            }

            if (currentWeight + itemWeight > MAX_WEIGHT) {
                player.sendMessage(Component.text("Not enough space! (" + currentWeight + "/" + MAX_WEIGHT + ")", NamedTextColor.RED));
                return;
            }

            int canAdd = Math.min(current.getAmount(), (MAX_WEIGHT - currentWeight) / Math.max(1, itemWeight));
            if (canAdd > 0) {
                ItemStack added = current.clone();
                added.setAmount(canAdd);
                contents.add(added);

                current.setAmount(current.getAmount() - canAdd);

                pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeItems(contents));
                cursor.setItemMeta(meta);
                updateBundleLore(cursor, contents);

                player.sendMessage(Component.text("Added " + canAdd + "x " + added.getType().name().toLowerCase().replace('_', ' ') + " to Bundle.", NamedTextColor.GREEN));
            }
        }

        // === RIGHT CLICK on empty slot: Remove last item from Bundle and place it in the clicked slot ===
        else if (event.getClick().isRightClick() && (current == null || current.getType() == Material.AIR)) {
            if (!contents.isEmpty()) {
                ItemStack lastItem = contents.remove(contents.size() - 1);

                // Place directly into the clicked slot (best UX)
                var clickedInventory = event.getClickedInventory();
                int slot = event.getSlot();

                if (clickedInventory != null) {
                    clickedInventory.setItem(slot, lastItem);
                    player.sendMessage(Component.text("Removed 1x " + lastItem.getType().name().toLowerCase().replace('_', ' ') + " from Bundle.", NamedTextColor.YELLOW));
                } else {
                    // Fallback if something goes wrong
                    player.getInventory().addItem(lastItem);
                }

                pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeItems(contents));
                cursor.setItemMeta(meta);
                updateBundleLore(cursor, contents);
            } else {
                player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            }
        }

        else if (event.isLeftClick() && current.getType() == Material.AIR) {
            event.setCancelled(false);
        }
    }
}