package org.bad_coders.better_bundles;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1852;
import net.minecraft.class_1865;
import net.minecraft.class_1937;
import net.minecraft.class_5537;
import net.minecraft.class_7225;
import net.minecraft.class_7710;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import net.minecraft.class_9694;

public class OreBundleUpgradeRecipe extends class_1852 {
   public OreBundleUpgradeRecipe(class_7710 category) {
      super(category);
   }

   private BundleUpgradeTier getNextTier(BundleUpgradeTier currentTier) {
      if (currentTier == BundleUpgradeTier.NONE) {
         return BundleUpgradeTier.IRON;
      } else if (currentTier == BundleUpgradeTier.IRON) {
         return BundleUpgradeTier.GOLD;
      } else {
         return currentTier == BundleUpgradeTier.GOLD ? BundleUpgradeTier.DIAMOND : null;
      }
   }

   private BundleUpgradeTier getCurrentTier(class_1799 stack) {
      return BundleUpgradeTier.getTierFromItem(stack.method_7909());
   }

   public boolean matches(class_9694 inv, class_1937 world) {
      if (inv.method_59991() == 3 && inv.method_59992() == 3) {
         class_1799 bundleStack = inv.method_59984(4);
         class_1792 item = bundleStack.method_7909();
         if (!(item instanceof class_5537) && item != BetterBundles.IRON_BUNDLE && item != BetterBundles.GOLD_BUNDLE && item != BetterBundles.DIAMOND_BUNDLE && item != BetterBundles.NETHERITE_BUNDLE) {
            return false;
         } else {
            BundleUpgradeTier currentTier = this.getCurrentTier(bundleStack);
            BundleUpgradeTier nextTier = this.getNextTier(currentTier);
            if (nextTier == null) {
               return false;
            } else {
               for(int i = 0; i < inv.method_59983(); ++i) {
                  if (i != 4) {
                     class_1799 stack = inv.method_59984(i);
                     if (stack.method_7960() || !stack.method_31574(nextTier.upgradeItem)) {
                        return false;
                     }
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public class_1799 craft(class_9694 inv, class_7225.class_7874 lookup) {
      class_1799 oldBundle = inv.method_59984(4);
      BundleUpgradeTier currentTier = this.getCurrentTier(oldBundle);
      BundleUpgradeTier nextTier = this.getNextTier(currentTier);
      if (nextTier == null) {
         return class_1799.field_8037;
      } else {
         class_1799 newBundle = new class_1799(nextTier.getResultItem());
         if (oldBundle.method_57826(class_9334.field_49650)) {
            class_9276 oldContents = (class_9276)oldBundle.method_58694(class_9334.field_49650);
            if (oldContents != null) {
               class_9276.class_9277 builder = new class_9276.class_9277(class_9276.field_49289);
               BundleCapacityHolder.set(64 + nextTier.totalExtraSlots);

               try {
                  for(class_1799 s : oldContents.method_57421()) {
                     builder.method_57432(s);
                  }

                  newBundle.method_57379(class_9334.field_49650, builder.method_57435());
               } finally {
                  BundleCapacityHolder.reset();
               }
            }
         }

         return newBundle;
      }
   }

   public class_1865<OreBundleUpgradeRecipe> method_8119() {
      return BetterBundles.ORE_BUNDLE_UPGRADE;
   }
}
