package org.bad_coders.better_bundles.mixin;

import net.minecraft.class_1799;
import net.minecraft.class_9129;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
   targets = {"net/minecraft/class_1799$3"}
)
public class ItemStackExtraValidationMixin {
   @Inject(
      method = {"method_59694(Lnet/minecraft/class_9129;)Lnet/minecraft/class_1799;"},
      at = {@At(
   value = "INVOKE",
   target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
)},
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void better_bundles$pushCapacityDuringValidation(class_9129 buf, CallbackInfoReturnable<class_1799> cir, class_1799 stack) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_59694(Lnet/minecraft/class_9129;)Lnet/minecraft/class_1799;"},
      at = {@At("RETURN")}
   )
   private void better_bundles$popCapacityAfterValidation(class_9129 buf, CallbackInfoReturnable<class_1799> cir) {
      BundleCapacityHolder.reset();
   }
}
