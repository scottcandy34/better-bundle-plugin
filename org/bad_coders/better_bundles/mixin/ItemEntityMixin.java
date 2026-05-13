package org.bad_coders.better_bundles.mixin;

import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1542.class})
public abstract class ItemEntityMixin {
   @Inject(
      method = {"method_5753()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$makeBundleFireImmune(CallbackInfoReturnable<Boolean> cir) {
      class_1542 entity = (class_1542)this;
      class_1799 stack = entity.method_6983();
      class_9279 nbtComponent = (class_9279)stack.method_58694(class_9334.field_49628);
      if (nbtComponent != null) {
         nbtComponent.method_57451((nbt) -> {
            if (nbt.method_10545("BetterBundles")) {
               class_2487 betterBundlesNbt = (class_2487)nbt.method_10562("BetterBundles").orElse(new class_2487());
               if ((Boolean)betterBundlesNbt.method_10577("IsFireproof").orElse(false)) {
                  cir.setReturnValue(true);
               }
            }

         });
      }

   }
}
