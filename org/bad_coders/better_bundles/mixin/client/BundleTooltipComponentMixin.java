package org.bad_coders.better_bundles.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_5682;
import net.minecraft.class_9276;
import org.apache.commons.lang3.math.Fraction;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_5682.class})
public class BundleTooltipComponentMixin {
   @Shadow
   private class_9276 field_49537;
   private int better_bundles$capacity = 64;

   @Inject(
      method = {"<init>(Lnet/minecraft/class_9276;)V"},
      at = {@At("RETURN")}
   )
   private void better_bundles$captureCapacity(class_9276 contents, CallbackInfo ci) {
      if (BundleCapacityHolder.hasContext()) {
         this.better_bundles$capacity = BundleCapacityHolder.get();
      }

   }

   @Inject(
      method = {"method_32661(Lnet/minecraft/class_327;)I"},
      at = {@At("HEAD")}
   )
   private void better_bundles$setContextHeight(class_327 textRenderer, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.push(this.better_bundles$capacity);
   }

   @Inject(
      method = {"method_32661(Lnet/minecraft/class_327;)I"},
      at = {@At("RETURN")}
   )
   private void better_bundles$resetContextHeight(class_327 textRenderer, CallbackInfoReturnable<Integer> cir) {
      BundleCapacityHolder.pop();
   }

   @Inject(
      method = {"method_32666(Lnet/minecraft/class_327;IIIILnet/minecraft/class_332;)V"},
      at = {@At("HEAD")}
   )
   private void better_bundles$setContextDraw(class_327 textRenderer, int x, int y, int width, int height, class_332 context, CallbackInfo ci) {
      BundleCapacityHolder.push(this.better_bundles$capacity);
   }

   @Inject(
      method = {"method_32666(Lnet/minecraft/class_327;IIIILnet/minecraft/class_332;)V"},
      at = {@At("RETURN")}
   )
   private void better_bundles$resetContextDraw(class_327 textRenderer, int x, int y, int width, int height, class_332 context, CallbackInfo ci) {
      BundleCapacityHolder.pop();
   }

   @Overwrite
   private int method_62020() {
      BundleCapacityHolder.push(this.better_bundles$capacity);

      int var1;
      try {
         var1 = class_3532.method_15340(class_3532.method_59515(this.field_49537.method_57428(), 94), 0, 94);
      } finally {
         BundleCapacityHolder.pop();
      }

      return var1;
   }

   @Overwrite
   private class_2561 method_62021() {
      if (this.field_49537.method_57429()) {
         return class_2561.method_43471("item.minecraft.bundle.empty");
      } else {
         BundleCapacityHolder.push(this.better_bundles$capacity);

         class_5250 var1;
         try {
            if (this.field_49537.method_57428().compareTo(Fraction.ONE) < 0) {
               return null;
            }

            var1 = class_2561.method_43471("item.minecraft.bundle.full");
         } finally {
            BundleCapacityHolder.pop();
         }

         return var1;
      }
   }

   @Overwrite
   private int method_33290() {
      return class_3532.method_38788(this.method_62019(), 4);
   }

   @Overwrite
   private int method_62019() {
      return Math.min(96, this.field_49537.method_57426());
   }
}
