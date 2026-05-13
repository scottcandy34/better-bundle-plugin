package org.bad_coders.better_bundles.mixin;

import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_5536;
import net.minecraft.class_5537;
import net.minecraft.class_5630;
import net.minecraft.class_5632;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_5537.class})
public abstract class BundleItemMixin extends class_1792 {
   public BundleItemMixin(class_1792.class_1793 settings) {
      super(settings);
   }

   @Inject(
      method = {"method_31565(Lnet/minecraft/class_1799;Lnet/minecraft/class_1735;Lnet/minecraft/class_5536;Lnet/minecraft/class_1657;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$preEnderStackClicked(class_1799 stack, class_1735 slot, class_5536 clickType, class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_31565(Lnet/minecraft/class_1799;Lnet/minecraft/class_1735;Lnet/minecraft/class_5536;Lnet/minecraft/class_1657;)Z"},
      at = {@At("RETURN")}
   )
   private void better_bundles$postEnderStackClicked(class_1799 stack, class_1735 slot, class_5536 clickType, class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      BundleCapacityHolder.reset();
   }

   @Inject(
      method = {"method_31566(Lnet/minecraft/class_1799;Lnet/minecraft/class_1799;Lnet/minecraft/class_1735;Lnet/minecraft/class_5536;Lnet/minecraft/class_1657;Lnet/minecraft/class_5630;)Z"},
      at = {@At("HEAD")}
   )
   private void better_bundles$preEnderClicked(class_1799 stack, class_1799 otherStack, class_1735 slot, class_5536 clickType, class_1657 player, class_5630 cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
      if (stack.method_7909() instanceof class_5537) {
         BundleCapacityHolder.setup(stack);
      } else if (otherStack.method_7909() instanceof class_5537) {
         BundleCapacityHolder.setup(otherStack);
      }

   }

   @Inject(
      method = {"method_31566(Lnet/minecraft/class_1799;Lnet/minecraft/class_1799;Lnet/minecraft/class_1735;Lnet/minecraft/class_5536;Lnet/minecraft/class_1657;Lnet/minecraft/class_5630;)Z"},
      at = {@At("RETURN")}
   )
   private void better_bundles$postEnderClicked(class_1799 stack, class_1799 otherStack, class_1735 slot, class_5536 clickType, class_1657 player, class_5630 cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
      BundleCapacityHolder.reset();
   }

   @Inject(
      method = {"method_32757(Lnet/minecraft/class_1799;Lnet/minecraft/class_1657;)Z"},
      at = {@At("HEAD")}
   )
   private void better_bundles$preEnderDrop(class_1799 stack, class_1657 player, CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"method_32757(Lnet/minecraft/class_1799;Lnet/minecraft/class_1657;)Z"},
      at = {@At("RETURN")}
   )
   private void better_bundles$postEnderDrop(class_1799 stack, class_1657 player, CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"method_32346(Lnet/minecraft/class_1799;)Ljava/util/Optional;"},
      at = {@At("HEAD")}
   )
   private void better_bundles$captureCapacityOnGetTooltipData(class_1799 stack, CallbackInfoReturnable<Optional<class_5632>> cir) {
      if (stack.method_7909() instanceof class_5537) {
         BundleCapacityHolder.setup(stack);
      }

   }

   @Inject(
      method = {"method_31561(Lnet/minecraft/class_1799;)F"},
      at = {@At("HEAD")}
   )
   private static void better_bundles$preGetAmountFilled(class_1799 stack, CallbackInfoReturnable<Float> cir) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_31571(Lnet/minecraft/class_1799;)I"},
      at = {@At("HEAD")}
   )
   private void better_bundles$preGetItemBarColor(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_31571(Lnet/minecraft/class_1799;)I"},
      at = {@At("RETURN")}
   )
   private void better_bundles$postGetItemBarColor(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.pop();
   }

   @Inject(
      method = {"method_31569(Lnet/minecraft/class_1799;)I"},
      at = {@At("HEAD")}
   )
   private void better_bundles$preGetItemBarStep(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_31569(Lnet/minecraft/class_1799;)I"},
      at = {@At("RETURN")}
   )
   private void better_bundles$postGetItemBarStep(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.pop();
   }

   @Inject(
      method = {"method_32346(Lnet/minecraft/class_1799;)Ljava/util/Optional;"},
      at = {@At("RETURN")}
   )
   private void better_bundles$releaseCapacityOnGetTooltipData(class_1799 stack, CallbackInfoReturnable<Optional<class_5632>> cir) {
      BundleCapacityHolder.reset();
   }
}
