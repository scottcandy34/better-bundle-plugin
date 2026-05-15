package com.scottcandy34.betterbundle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class BetterBundlePlugin extends JavaPlugin implements Listener {

    private NamespacedKey bundleKey;
    private NamespacedKey slotKey;

    private static final int MAX_WEIGHT = 64;
    private static final NamespacedKey RECIPE_KEY = new NamespacedKey("betterbundle", "bundle_recipe");

    @Override
    public void onEnable() {
        bundleKey = new NamespacedKey(this, "is_bundle");
        slotKey = new NamespacedKey(this, "is_slot");

        getServer().getPluginManager().registerEvents(this, this);

        registerBundleRecipe();

        getCommand("betterbundle").setExecutor(this);

        getLogger().info("BetterBundle plugin enabled for Paper 26.1.2! Recreated from bundles_mod.");
        getLogger().info("Craft a Bundle with 5 Rabbit Hide + 2 String (shaped recipe).");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("betterbundle")) return false;

        if (!sender.hasPermission("betterbundle.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("Usage: /betterbundle give <bundle|slot> [amount] [player]", NamedTextColor.GRAY));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /betterbundle give <bundle|slot> [amount] [player]", NamedTextColor.GRAY));
            return true;
        }

        String type = args[1].toLowerCase();
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1) amount = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount!", NamedTextColor.RED));
                return true;
            }
        }

        Player target = null;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("You must specify a player when using console.", NamedTextColor.RED));
            return true;
        }

        ItemStack item;
        String itemName;
        if (type.equals("bundle")) {
            item = createBundleItem(amount);
            itemName = "Bundle";
        } else if (type.equals("slot")) {
            item = createSlotItem(amount);
            itemName = "Slot";
        } else {
            sender.sendMessage(Component.text("Unknown item type! Use 'bundle' or 'slot'.", NamedTextColor.RED));
            return true;
        }

        target.getInventory().addItem(item);
        target.sendMessage(Component.text("You received " + amount + "x " + itemName + "!", NamedTextColor.GREEN));
        if (!target.equals(sender)) {
            sender.sendMessage(Component.text("Gave " + amount + "x " + itemName + " to " + target.getName(), NamedTextColor.GREEN));
        }

        return true;
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterBundle disabled.");
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
            
            shulker.customName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.setItemModel(NamespacedKey.minecraft("bundle"));

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
        if (item == null || item.getType() == Material.AIR) return 0;
        if (isBlockedItem(item)) return Integer.MAX_VALUE;

        // Slot items get a fixed weight of 4 (like tools/armor)
        if (isOurSlot(item)) return 4;

        int amount = item.getAmount();
        int maxStack = item.getMaxStackSize();

        // Correct percentage formula (full stack = 64 weight)
        return (amount * 64 + maxStack - 1) / maxStack;
    }

    private boolean tryAddToExistingSlot(Inventory bundleInv, ItemStack toAdd) {
        if (toAdd == null || toAdd.getType() == Material.AIR) return false;

        for (ItemStack content : bundleInv.getContents()) {
            if (isOurSlot(content)) {
                if (!(content.getItemMeta() instanceof BundleMeta meta)) continue;

                // Get current items and filter out any null or AIR slots
                java.util.List<ItemStack> currentItems = new java.util.ArrayList<>();
                for (ItemStack item : meta.getItems()) {
                    if (item != null && item.getType() != Material.AIR) {
                        currentItems.add(item.clone());
                    }
                }

                // Use temporary inventory so Bukkit handles stacking correctly
                Inventory tempInv = Bukkit.createInventory(null, 27);
                tempInv.setContents(currentItems.toArray(new ItemStack[0]));
                tempInv.addItem(toAdd.clone());

                // Filter again before setting back to BundleMeta (this is the key fix)
                java.util.List<ItemStack> finalItems = new java.util.ArrayList<>();
                for (ItemStack item : tempInv.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        finalItems.add(item);
                    }
                }

                meta.setItems(finalItems);
                content.setItemMeta(meta);
                return true;
            }
        }
        return false;
    }

    private boolean addNewSlotToBundle(Inventory bundleInv, ItemStack toAdd) {
        int lastSlotIndex = bundleInv.getSize() - 1;
        ItemStack previousItem = bundleInv.getItem(lastSlotIndex);

        // Place the new Slot in the last position
        bundleInv.setItem(lastSlotIndex, createSlotItem(1));

        // Move the previous item (if any) into the new Slot
        if (previousItem != null && previousItem.getType() != Material.AIR) {
            tryAddToExistingSlot(bundleInv, previousItem.clone());
        }

        // Add the new overflowing item into the Slot
        if (toAdd != null && toAdd.getType() != Material.AIR) {
            tryAddToExistingSlot(bundleInv, toAdd.clone());
        }

        return true; // always succeeds now
    }

    private int getSingleItemWeight(Material type) {
        if (type == null || type == Material.AIR) return 0;

        ItemStack temp = new ItemStack(type, 1);
        if (isBlockedItem(temp)) return Integer.MAX_VALUE;

        int maxStack = temp.getMaxStackSize();
        return (64 + maxStack - 1) / maxStack;
    }

    private boolean isBlockedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (isOurSlot(item)) return false; // Slots are allowed as upgrades inside the Bundle

        Material type = item.getType();
        return type.name().contains("SHULKER") || type == Material.CHEST ||
               type == Material.ENDER_CHEST || type == Material.BUNDLE ||
               type.name().contains("BARREL");
    }

    private boolean hasItemInInventory(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);

        if (inv == null) return false;

        int currentWeight = calculateWeight(inv);
        if (currentWeight > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void updateBundleLore(ItemStack bundle) {
        Inventory inv = getBundleInventory(bundle);
        if (inv == null) return;

        int currentWeight = calculateWeight(inv);

        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        List<Component> lore = new ArrayList<>();
        if (currentWeight == 0) {
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
        } else {
            // Filled bundle: unstackable + durability bar ALWAYS visible
            meta.setMaxStackSize(1);
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(MAX_WEIGHT + 1);   // increased max damage (65)
                
                int damage = MAX_WEIGHT + 1 - weight;      // accurate count
                
                // Force the damage indicator to stay visible even when full
                if (damage <= 0) {
                    damage = 1;   // tiny visible damage when bundle is completely full
                }
                
                damageable.setDamage(damage);
            }
        }

        bundle.setItemMeta(meta);
    }

    private void updateBundle(ItemStack bundle) {
        updateBundleLore(bundle);
        updateBundleStackAndDurability(bundle);
    }

    private boolean isOurInventoryView(InventoryView inventoryView) {
        // Convert title to string so we can do normal string check
        String title = PlainTextComponentSerializer.plainText().serialize(inventoryView.title());
        
        if (!"Bundle".equals(title)) {
            return true;
        } else {
            return false;
        }
    }

    private int getUniqueItemCount(ItemStack slot) {
        if (!isOurSlot(slot) || !(slot.getItemMeta() instanceof BundleMeta meta)) return 0;

        java.util.Set<Material> unique = new java.util.HashSet<>();
        for (ItemStack content : meta.getItems()) {
            if (content != null && content.getType() != Material.AIR) {
                unique.add(content.getType());
            }
        }
        return unique.size();
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
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Skip Creative mode
        if (event.getView().getType() == InventoryType.CREATIVE) {
            return;
        }

        if (event.getClick() == ClickType.SHIFT_RIGHT && isOurSlot(cursor)) {
            event.setCancelled(true);

            if (!(cursor.getItemMeta() instanceof BundleMeta meta)) return;

            // Get only valid items
            java.util.List<ItemStack> validItems = new java.util.ArrayList<>();
            for (ItemStack item : meta.getItems()) {
                if (item != null && item.getType() != Material.AIR) {
                    validItems.add(item.clone());
                }
            }

            // Clear the Slot
            meta.setItems(java.util.Collections.emptyList());
            cursor.setItemMeta(meta);

            // Add as much as possible to player's inventory
            java.util.HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(
                validItems.toArray(new ItemStack[0])
            );

            // Put any items that didn't fit back into the Slot
            if (!leftovers.isEmpty()) {
                java.util.List<ItemStack> toPutBack = new java.util.ArrayList<>(leftovers.values());
                meta.setItems(toPutBack);
                cursor.setItemMeta(meta);
            }

            // Refresh display (uses same update method as bundle since it only touches lore/durability for Slot)
            updateBundle(cursor);

            // Feedback
            if (leftovers.isEmpty()) {
                player.sendMessage(Component.text("Slot emptied into inventory!", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("Slot partially emptied (inventory full)", NamedTextColor.YELLOW));
            }

            // Fix creative inventory if needed
            if (event.getView().getType() == InventoryType.CREATIVE) {
                Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 1L);
            }
            return;
        }

        else if (isOurSlot(current) || isOurSlot(cursor)) {
            // Only restrict when actually trying to INSERT a new item into the Slot
            boolean isInsertionAttempt = event.getClick().isLeftClick() &&
                                        ((isOurSlot(current) && cursor != null && cursor.getType() != Material.AIR) ||
                                        (isOurSlot(cursor) && current != null && current.getType() != Material.AIR));

            if (!isInsertionAttempt) {
                event.setCancelled(false); // allow normal movement, pickup, placement, right-click, etc.
                return;
            }

            event.setCancelled(true);

            ItemStack slotItem = isOurSlot(current) ? current : cursor;
            int currentUnique = getUniqueItemCount(slotItem);

            // Determine what type is being added
            ItemStack adding = isOurSlot(current) ? cursor : current;
            Material addingType = (adding != null) ? adding.getType() : Material.AIR;

            // Check if this type is already present in the slot
            boolean alreadyPresent = false;
            if (addingType != Material.AIR && slotItem.getItemMeta() instanceof BundleMeta meta) {
                for (ItemStack existing : meta.getItems()) {
                    if (existing != null && existing.getType() == addingType) {
                        alreadyPresent = true;
                        break;
                    }
                }
            }

            // Allow if under limit OR this type already exists
            if (currentUnique < 12 || alreadyPresent) {
                event.setCancelled(false); // let original vanilla bundle logic handle the insertion
                return;
            }

            // Otherwise block new unique type
            player.sendMessage(Component.text("Slot can only hold 12 different items!", NamedTextColor.RED));
            return;
        }

        if (!isOurBundle(cursor) && !isOurBundle(current)) return;

        // Shift + Left Click on the Bundle (works even when completely empty)
        if (event.getClick() == ClickType.SHIFT_LEFT && isOurBundle(current)) {
            event.setCancelled(true);
            updateBundle(current); // ← ensures empty bundles have fresh BlockStateMeta before opening
            Inventory bundleInv = getBundleInventory(current);
            if (bundleInv != null) {
                player.openInventory(bundleInv);
            }
            return;
        }

        // Shift + Right Click on the Bundle
        else if (event.getClick() == ClickType.SHIFT_RIGHT && isOurBundle(cursor)) {
            event.setCancelled(true);

            Inventory bundleInv = getBundleInventory(cursor);
            if (bundleInv == null) return;

            // Get only valid items (fixes the null crash)
            java.util.List<ItemStack> validItems = new java.util.ArrayList<>();
            for (ItemStack item : bundleInv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    validItems.add(item.clone());
                }
            }

            // Clear the bundle
            bundleInv.clear();

            // Add as much as possible to player's inventory
            java.util.HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(
                validItems.toArray(new ItemStack[0])
            );

            // Put any items that didn't fit back into the bundle
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    if (leftover != null && leftover.getType() != Material.AIR) {
                        bundleInv.addItem(leftover);
                    }
                }
            }

            saveBundleInventory(cursor, bundleInv);
            updateBundle(cursor);

            // Feedback
            if (leftovers.isEmpty()) {
                player.sendMessage(Component.text("Bundle emptied into inventory!", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("Bundle partially emptied (inventory full)", NamedTextColor.YELLOW));
            }

            // Fix creative inventory if needed
            if (event.getView().getType() == InventoryType.CREATIVE) {
                Bukkit.getScheduler().runTaskLater(this, player::updateInventory, 1L);
            }
            return;
        }

        event.setCancelled(true);

        // allow bundle stacking
        if (isOurBundle(current) && isOurBundle(cursor)) {
            event.setCancelled(false);
            return;
        }

        // First LEFT CLICK block (Bundle in current) – identical logic
        else if (event.getClick().isLeftClick() && isOurBundle(current) && cursor != null && cursor.getType() != Material.AIR) {
            Inventory bundleInv = getBundleInventory(current);
            if (bundleInv == null) return;
            
            int singleItemWeight = getSingleItemWeight(cursor.getType());
            int currentWeight = calculateWeight(bundleInv);

            if (isBlockedItem(cursor)) {
                player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
                return;
            }

            int remainingWeight = MAX_WEIGHT - currentWeight;
            if (remainingWeight < singleItemWeight) {
                player.sendMessage(Component.text("Not enough space! (" + currentWeight + "/" + MAX_WEIGHT + ")", NamedTextColor.RED));
                return;
            }

            int canAdd = Math.min(cursor.getAmount(), remainingWeight / singleItemWeight);

            ItemStack toAdd = cursor.clone();
            toAdd.setAmount(canAdd);

            // Add to normal 27 slots first
            java.util.HashMap<Integer, ItemStack> leftovers = bundleInv.addItem(toAdd);

            if (!leftovers.isEmpty()) {
                ItemStack remaining = leftovers.values().iterator().next();

                // Route leftovers to Slot system
                if (!tryAddToExistingSlot(bundleInv, remaining)) {
                    if (!addNewSlotToBundle(bundleInv, remaining)) {
                        player.sendMessage(Component.text("Bundle is completely full!", NamedTextColor.RED));
                        cursor.setAmount(remaining.getAmount());
                        return;
                    }
                }
                cursor.setAmount(0);
            } else {
                cursor.setAmount(cursor.getAmount() - canAdd);
            }

            saveBundleInventory(current, bundleInv);
            updateBundle(current);

            if (event.getView().getType() == InventoryType.CREATIVE) {
                InventoryView inventory = player.getOpenInventory();
                inventory.setCursor(cursor);
                Bukkit.getScheduler().runTaskLater(this, () -> player.updateInventory(), 1L);
            }
        }

        // Second LEFT CLICK block (Bundle in cursor) – identical logic
        else if (event.getClick().isLeftClick() && isOurBundle(cursor) && current != null && current.getType() != Material.AIR) {
            Inventory bundleInv = getBundleInventory(cursor);
            if (bundleInv == null) return;
            
            int singleItemWeight = getSingleItemWeight(current.getType());
            int currentWeight = calculateWeight(bundleInv);

            if (isBlockedItem(current)) {
                player.sendMessage(Component.text("This item cannot be stored in the Bundle.", NamedTextColor.RED));
                return;
            }

            int remainingWeight = MAX_WEIGHT - currentWeight;
            if (remainingWeight < singleItemWeight) {
                player.sendMessage(Component.text("Not enough space! (" + currentWeight + "/" + MAX_WEIGHT + ")", NamedTextColor.RED));
                return;
            }

            int canAdd = Math.min(current.getAmount(), remainingWeight / singleItemWeight);

            ItemStack toAdd = current.clone();
            toAdd.setAmount(canAdd);

            java.util.HashMap<Integer, ItemStack> leftovers = bundleInv.addItem(toAdd);

            if (!leftovers.isEmpty()) {
                ItemStack remaining = leftovers.values().iterator().next();

                if (!tryAddToExistingSlot(bundleInv, remaining)) {
                    if (!addNewSlotToBundle(bundleInv, remaining)) {
                        player.sendMessage(Component.text("Bundle is completely full!", NamedTextColor.RED));
                        current.setAmount(remaining.getAmount());
                        return;
                    }
                }
                current.setAmount(0);
            } else {
                current.setAmount(current.getAmount() - canAdd);
            }

            saveBundleInventory(cursor, bundleInv);
            updateBundle(cursor);

            if (event.getView().getType() == InventoryType.CREATIVE) {
                InventoryView inventory = player.getOpenInventory();
                inventory.setCursor(cursor);
                Bukkit.getScheduler().runTaskLater(this, () -> player.updateInventory(), 1L);
            }
        }

        // RIGHT CLICK on empty slot: Remove last item (LIFO style)
        else if (event.getClick().isRightClick() && isOurBundle(cursor) && (current == null || current.getType() == Material.AIR) && hasItemInInventory(cursor)) {
            Inventory bundleInv = getBundleInventory(cursor);
            if (bundleInv == null) return;
            
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
                    updateBundle(cursor);
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
        } else {
            event.setCancelled(false);
        }
    }

    // When player closes the bundle GUI, refresh lore + durability bar on all bundles
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (!isOurInventoryView(event.getView())) {
            return;
        }

        // Save + refresh EVERY bundle the player is carrying
        // This ensures changes made inside the opened ShulkerBox GUI are persisted back into the Bundle item
        for (ItemStack item : player.getInventory().getContents()) {
            if (isOurBundle(item)) {
                Inventory inv = getBundleInventory(item);
                if (inv != null) {
                    saveBundleInventory(item, inv);
                }
                updateBundle(item);
            }
        }
        // Also handle cursor (in case they closed while holding a bundle)
        ItemStack cursor = player.getItemOnCursor();
        if (isOurBundle(cursor)) {
            Inventory inv = getBundleInventory(cursor);
            if (inv != null) {
                saveBundleInventory(cursor, inv);
            }
            updateBundle(cursor);
        }
    }
}