package org.bad_coders.better_bundles.mixin;

import java.util.function.Consumer;
import net.minecraft.class_10712;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_5537;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.bad_coders.better_bundles.BundleUpgradeTier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1792.class})
public class BundleItemTooltipMixin {
   @Inject(
      method = {"method_67187(Lnet/minecraft/class_1799;Lnet/minecraft/class_1792$class_9635;Lnet/minecraft/class_10712;Ljava/util/function/Consumer;Lnet/minecraft/class_1836;)V"},
      at = {@At("HEAD")}
   )
   private void better_bundles$setContextOnAppendTooltip(class_1799 stack, class_1792.class_9635 context, class_10712 configuration, Consumer<class_2561> tooltip, class_1836 type, CallbackInfo ci) {
      if (stack.method_7909() instanceof class_5537) {
         BundleCapacityHolder.setup(stack);
      }

   }

   @Inject(
      method = {"method_67187(Lnet/minecraft/class_1799;Lnet/minecraft/class_1792$class_9635;Lnet/minecraft/class_10712;Ljava/util/function/Consumer;Lnet/minecraft/class_1836;)V"},
      at = {@At("RETURN")}
   )
   private void better_bundles$resetContextOnAppendTooltip(class_1799 stack, class_1792.class_9635 context, class_10712 configuration, Consumer<class_2561> tooltip, class_1836 type, CallbackInfo ci) {
      if (stack.method_7909() instanceof class_5537) {
         BundleCapacityHolder.reset();
      }

   }

   @Inject(
      method = {"method_67187(Lnet/minecraft/class_1799;Lnet/minecraft/class_1792$class_9635;Lnet/minecraft/class_10712;Ljava/util/function/Consumer;Lnet/minecraft/class_1836;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$customBundleDescription(class_1799 stack, class_1792.class_9635 context, class_10712 configuration, Consumer<class_2561> tooltip, class_1836 type, CallbackInfo ci) {
      BundleUpgradeTier tier = BundleUpgradeTier.getTierFromItem(stack.method_7909());
      if (tier != BundleUpgradeTier.NONE) {
         ci.cancel();
         class_9276 c = (class_9276)stack.method_58694(class_9334.field_49650);
         boolean empty = c == null || c.method_57429();
         if (empty) {
            int totalCapacity = 64 + tier.totalExtraSlots;
            tooltip.accept(class_2561.method_43469("item.better_bundles.bundle.description", new Object[]{totalCapacity}).method_27692(class_124.field_1080));
         }
      }

   }
}
