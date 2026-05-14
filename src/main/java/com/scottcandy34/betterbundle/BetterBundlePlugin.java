package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class BetterBundlePlugin extends JavaPlugin implements Listener {

    private NamespacedKey bundleKey;

    private static final int MAX_WEIGHT = 64;
    private static final NamespacedKey RECIPE_KEY = new NamespacedKey("betterbundle", "bundle_recipe");

    @Override
    public void onEnable() {
        bundleKey = new NamespacedKey(this, "is_bundle");

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

    public ItemStack createBundleItem(int amount) {
        ItemStack item = new ItemStack(Material.SHULKER_BOX, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof BlockStateMeta bsm) {
            ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
            shulker.getInventory().clear();

            meta.setItemModel(NamespacedKey.minecraft("bundle"));

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

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(bundleKey, PersistentDataType.BYTE, (byte) 1);

            bsm.setBlockState(shulker);
            item.setItemMeta(meta);

            // Start as completely empty (stack 16, no damage)
            updateBundle(item);
        }
        return item;
    }

    public boolean isOurBundle(ItemStack item) {
        if (item == null || item.getType() != Material.SHULKER_BOX || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(bundleKey, PersistentDataType.BYTE);
    }

    private Inventory getBundleInventory(ItemStack bundle) {
        if (!(bundle.getItemMeta() instanceof BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return null;
        return shulker.getInventory();
    }

    private void saveBundleInventory(ItemStack bundle, Inventory inventory) {
        if (!(bundle.getItemMeta() instanceof BlockStateMeta bsm)) return;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return;

        shulker.getInventory().setContents(inventory.getContents());
        bsm.setBlockState(shulker);
        bundle.setItemMeta(bsm);
    }

    private int calculateWeight(Inventory inventory) {
        int weight = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            weight += getItemWeight(item);
        }
        return weight;
    }

    private int getItemWeight(ItemStack item) {
        Material type = item.getType();
        if (isBlockedItem(type)) return Integer.MAX_VALUE;

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

    private void updateBundleLore(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return;

        int currentWeight = calculateWeight(inv);
        int percent = (int) ((currentWeight / (double) MAX_WEIGHT) * 100);

        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

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

    private void updateBundleStackAndDurability(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return;

        int weight = calculateWeight(inv);
        boolean isEmpty = weight == 0;

        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        if (isEmpty) {
            // Empty bundle: stackable + NO durability bar
            meta.setMaxStackSize(16);
            if (meta instanceof Damageable damageable) {
                damageable.resetDamage();
            }
            // Do not set any Damageable values when empty
        } else {
            // Filled bundle: unstackable + durability increases with weight
            meta.setMaxStackSize(1);
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(MAX_WEIGHT);
                getLogger().info("weight: " + weight);
                damageable.setDamage(MAX_WEIGHT - weight); // exactly the weight (increases when adding items)
            }
        }

        bundle.setItemMeta(meta);
    }

    private void updateBundle(ItemStack bundle) {
        updateBundleLore(bundle);
        updateBundleStackAndDurability(bundle);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isOurBundle(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot place this Bundle as a block!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (!isOurBundle(cursor)) return;

        // Skip Creative mode
        if (event.getView().getType() == InventoryType.CREATIVE) {
            return;
        }

        event.setCancelled(true);

        Inventory bundleInv = getBundleInventory(cursor);
        if (bundleInv == null) return;

        // LEFT CLICK: Add item
        if (event.getClick().isLeftClick() && current != null && current.getType() != Material.AIR) {
            int itemWeight = getItemWeight(current);
            int currentWeight = calculateWeight(bundleInv);

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

                // Add to first available slot in the real Shulker inventory
                bundleInv.addItem(added);

                current.setAmount(current.getAmount() - canAdd);

                saveBundleInventory(cursor, bundleInv);
                updateBundle(cursor);   // ← updates lore + stack size

                player.sendMessage(Component.text("Added " + canAdd + "x " + added.getType().name().toLowerCase().replace('_', ' ') + " to Bundle.", NamedTextColor.GREEN));
            }

            // Fix creative inventory for being out of async
            if (event.getView().getType() == InventoryType.CREATIVE) {
                InventoryView inventory = player.getOpenInventory();
                
                inventory.setCursor(cursor);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    player.updateInventory();
                }, 1L);
            }
        }

        // RIGHT CLICK on empty slot: Remove last item (LIFO style)
        else if (event.getClick().isRightClick() && (current == null || current.getType() == Material.AIR)) {
            ItemStack[] contents = bundleInv.getContents();
            for (int i = contents.length - 1; i >= 0; i--) {
                ItemStack slotItem = contents[i];
                if (slotItem != null && slotItem.getType() != Material.AIR) {
                    bundleInv.setItem(i, null);

                    var clickedInventory = event.getClickedInventory();
                    int slot = event.getSlot();

                    if (clickedInventory != null) {
                        clickedInventory.setItem(slot, slotItem);
                    } else {
                        player.getInventory().addItem(slotItem);
                    }

                    saveBundleInventory(cursor, bundleInv);
                    updateBundle(cursor);   // ← updates lore + stack size

                    player.sendMessage(Component.text("Removed 1x " + slotItem.getType().name().toLowerCase().replace('_', ' ') + " from Bundle.", NamedTextColor.YELLOW));
                    return;
                }
            }
            player.sendMessage(Component.text("The Bundle is empty.", NamedTextColor.GRAY));
            
            // Fix creative inventory for being out of async
            if (event.getView().getType() == InventoryType.CREATIVE) {
                InventoryView inventory = player.getOpenInventory();
                
                inventory.setCursor(cursor);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    player.updateInventory();
                }, 1L);
            }
        }

        else if (event.getAction() == InventoryAction.PLACE_ALL && current.getType() == Material.AIR) {
            event.setCancelled(false);
        }
    }
}