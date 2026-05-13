package org.bad_coders.better_bundles;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1856;
import net.minecraft.class_1865;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_7225;
import net.minecraft.class_8059;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import net.minecraft.class_9697;
import net.minecraft.class_9887;

public class NetheriteBundleSmithingRecipe implements class_8059 {
   public boolean method_61702(class_9697 input, class_1937 world) {
      return input.comp_2677().method_31574(class_1802.field_41946) && input.comp_2678().method_31574(BetterBundles.DIAMOND_BUNDLE) && input.comp_2679().method_31574(class_1802.field_22020);
   }

   public class_1799 craft(class_9697 input, class_7225.class_7874 lookup) {
      class_1799 oldBundle = input.comp_2678();
      class_1799 newBundle = new class_1799(BetterBundles.NETHERITE_BUNDLE);
      if (oldBundle.method_57826(class_9334.field_49650)) {
         class_9276 oldContents = (class_9276)oldBundle.method_58694(class_9334.field_49650);
         if (oldContents != null) {
            class_9276.class_9277 builder = new class_9276.class_9277(class_9276.field_49289);
            BundleCapacityHolder.set(64 + BundleUpgradeTier.NETHERITE.totalExtraSlots);

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

   public Optional<class_1856> method_64722() {
      return Optional.of(class_1856.method_8091(new class_1935[]{class_1802.field_41946}));
   }

   public class_1856 method_64723() {
      return class_1856.method_8091(new class_1935[]{BetterBundles.DIAMOND_BUNDLE});
   }

   public Optional<class_1856> method_64724() {
      return Optional.of(class_1856.method_8091(new class_1935[]{class_1802.field_22020}));
   }

   public class_1865<NetheriteBundleSmithingRecipe> method_8119() {
      return BetterBundles.NETHERITE_BUNDLE_SMITHING;
   }

   public class_9887 method_61671() {
      return class_9887.method_61683(List.of(this.method_64722(), Optional.of(this.method_64723()), this.method_64724()));
   }
}
