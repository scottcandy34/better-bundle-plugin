package org.bad_coders.better_bundles.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_465;
import net.minecraft.class_5537;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import org.bad_coders.better_bundles.BundleSelectionAccessor;
import org.bad_coders.better_bundles.network.EnderBundleSelectIndexPayload;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_465.class})
public abstract class HandledScreenScrollMixin {
   @Shadow
   protected @Nullable class_1735 field_2787;

   @Inject(
      method = {"method_25401(DDDD)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$scrollBundle(double mouseX, double mouseY, double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
      if (vertical != (double)0.0F) {
         if (this.field_2787 != null && this.field_2787.method_7681()) {
            class_1799 stack = this.field_2787.method_7677();
            if (stack.method_7909() instanceof class_5537) {
               class_9276 contents = (class_9276)stack.method_58694(class_9334.field_49650);
               if (contents != null && !contents.method_57429()) {
                  int size = contents.method_61666();
                  if (size > 1) {
                     int current = contents.method_61668();
                     int next = Math.floorMod(current + (vertical > (double)0.0F ? -1 : 1), Math.max(1, size));
                     if (contents instanceof BundleSelectionAccessor) {
                        BundleSelectionAccessor accessor = (BundleSelectionAccessor)contents;
                        accessor.better_bundles$setSelectionOverride(next);
                     }

                     ClientPlayNetworking.send(new EnderBundleSelectIndexPayload(this.field_2787.field_7874, next));
                     cir.setReturnValue(true);
                  }
               }
            }
         }
      }
   }
}
