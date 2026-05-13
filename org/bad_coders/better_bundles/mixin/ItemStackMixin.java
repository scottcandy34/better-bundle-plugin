package org.bad_coders.better_bundles.mixin;

import net.minecraft.class_1799;
import net.minecraft.class_2561;
import org.bad_coders.better_bundles.BundleUpgradeTier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1799.class})
public class ItemStackMixin {
   @Inject(
      method = {"method_7964()Lnet/minecraft/class_2561;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void better_bundles$modifyName(CallbackInfoReturnable<class_2561> cir) {
      class_1799 stack = (class_1799)this;
      BundleUpgradeTier tier = BundleUpgradeTier.getTierFromItem(stack.method_7909());
      if (tier != BundleUpgradeTier.NONE) {
         String tierName = tier.getName();
         String var10000 = tierName.substring(0, 1).toUpperCase();
         tierName = var10000 + tierName.substring(1);
         cir.setReturnValue(class_2561.method_43470(tierName + " Bundle"));
      }

   }
}
