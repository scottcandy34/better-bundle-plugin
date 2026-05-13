package org.bad_coders.better_bundles;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public enum BundleUpgradeTier {
   NONE(0, (class_1792)null, (BundleUpgradeTier)null, "bundle"),
   IRON(192, class_1802.field_8620, NONE, "iron_bundle"),
   GOLD(448, class_1802.field_8695, IRON, "gold_bundle"),
   DIAMOND(704, class_1802.field_8477, GOLD, "diamond_bundle"),
   NETHERITE(960, class_1802.field_22020, DIAMOND, "netherite_bundle"),
   ENDER(704, (class_1792)null, DIAMOND, "ender_bundle");

   public final int totalExtraSlots;
   public final class_1792 upgradeItem;
   public final BundleUpgradeTier previousTier;
   private final String resultItemId;

   private BundleUpgradeTier(int slots, class_1792 upgradeItem, BundleUpgradeTier previousTier, String resultItemId) {
      this.totalExtraSlots = slots;
      this.upgradeItem = upgradeItem;
      this.previousTier = previousTier;
      this.resultItemId = resultItemId;
   }

   public class_1792 getResultItem() {
      return (class_1792)class_7923.field_41178.method_63535(class_2960.method_60655("better_bundles", this.resultItemId));
   }

   public String getName() {
      return this.name().toLowerCase();
   }

   public static BundleUpgradeTier getTierFromItem(class_1792 item) {
      class_2960 id = class_7923.field_41178.method_10221(item);
      if (!id.method_12836().equals("better_bundles") && item != class_1802.field_27023) {
         return NONE;
      } else {
         for(BundleUpgradeTier tier : values()) {
            if (tier.resultItemId.equals(id.method_12832())) {
               return tier;
            }
         }

         return NONE;
      }
   }

   public static BundleUpgradeTier fromName(String name) {
      for(BundleUpgradeTier tier : values()) {
         if (tier.getName().equalsIgnoreCase(name)) {
            return tier;
         }
      }

      return NONE;
   }

   // $FF: synthetic method
   private static BundleUpgradeTier[] $values() {
      return new BundleUpgradeTier[]{NONE, IRON, GOLD, DIAMOND, NETHERITE, ENDER};
   }
}
