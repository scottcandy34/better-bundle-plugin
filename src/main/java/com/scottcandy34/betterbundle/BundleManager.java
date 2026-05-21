package com.scottcandy34.betterbundle;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BundleManager {

    public boolean isOurInventoryView(InventoryView inventoryView) {
        String title = PlainTextComponentSerializer.plainText().serialize(inventoryView.title());
        return !"Bundle".equals(title);
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
}
