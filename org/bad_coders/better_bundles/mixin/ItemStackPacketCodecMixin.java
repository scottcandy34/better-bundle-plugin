package org.bad_coders.better_bundles.mixin;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_6880;
import net.minecraft.class_9129;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
   targets = {"net/minecraft/class_1799$1"}
)
public class ItemStackPacketCodecMixin {
   @Inject(
      method = {"method_56099(Lnet/minecraft/class_9129;)Lnet/minecraft/class_1799;"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_9139;decode(Ljava/lang/Object;)Ljava/lang/Object;",
   ordinal = 1
)},
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void better_bundles$setCapacityContextOnDecode(class_9129 registryByteBuf, CallbackInfoReturnable<class_1799> cir, int count, class_6880<class_1792> itemEntry) {
      BundleCapacityHolder.setup((class_1792)itemEntry.comp_349());
   }

   @Inject(
      method = {"method_56099(Lnet/minecraft/class_9129;)Lnet/minecraft/class_1799;"},
      at = {@At("RETURN")}
   )
   private void better_bundles$resetCapacityContextOnDecode(class_9129 registryByteBuf, CallbackInfoReturnable<class_1799> cir) {
      BundleCapacityHolder.reset();
   }

   @Inject(
      method = {"method_56100(Lnet/minecraft/class_9129;Lnet/minecraft/class_1799;)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_9139;encode(Ljava/lang/Object;Ljava/lang/Object;)V",
   ordinal = 1
)}
   )
   private void better_bundles$setCapacityContextOnEncode(class_9129 registryByteBuf, class_1799 stack, CallbackInfo ci) {
      BundleCapacityHolder.setup(stack);
   }

   @Inject(
      method = {"method_56100(Lnet/minecraft/class_9129;Lnet/minecraft/class_1799;)V"},
      at = {@At("RETURN")}
   )
   private void better_bundles$resetCapacityContextOnEncode(class_9129 registryByteBuf, class_1799 stack, CallbackInfo ci) {
      BundleCapacityHolder.reset();
   }
}
