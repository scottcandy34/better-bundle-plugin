package org.bad_coders.better_bundles.mixin;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_5537;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import org.apache.commons.lang3.math.Fraction;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.bad_coders.better_bundles.BundleSelectionAccessor;
import org.bad_coders.better_bundles.BundleUpgradeTier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_9276.class})
public abstract class BundleContentsComponentMixin implements BundleSelectionAccessor {
   @Shadow
   @Final
   private Fraction field_49295;
   @Shadow
   @Final
   private static Fraction field_49292;
   @Shadow
   @Final
   private int field_52592;
   private int better_bundles$capacity;
   @Unique
   private int better_bundles$selectionOverride = -1;

   public void better_bundles$setSelectionOverride(int index) {
      this.better_bundles$selectionOverride = index;
   }

   public int better_bundles$getSelectionOverride() {
      return this.better_bundles$selectionOverride;
   }

   @Inject(
      method = {"method_61668()I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$getOverrideSelection(CallbackInfoReturnable<Integer> cir) {
      if (this.better_bundles$selectionOverride != -1) {
         cir.setReturnValue(this.better_bundles$selectionOverride);
      }

   }

   @Inject(
      method = {"<init>(Ljava/util/List;Lorg/apache/commons/lang3/math/Fraction;I)V"},
      at = {@At("RETURN")}
   )
   private void better_bundles$initCapacity(List stacks, Fraction occupancy, int selectedStackIndex, CallbackInfo ci) {
      this.better_bundles$capacity = BundleCapacityHolder.get();
   }

   @Inject(
      method = {"method_61667(Lnet/minecraft/class_1799;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void better_bundles$preventNesting(class_1799 stack, CallbackInfoReturnable<Boolean> cir) {
      if (stack.method_7909() instanceof class_5537) {
         cir.setReturnValue(false);
      }

   }

   @Overwrite
   public Fraction method_57428() {
      int cap = this.better_bundles$capacity;
      if (BundleCapacityHolder.hasContext()) {
         cap = BundleCapacityHolder.get();
         this.better_bundles$capacity = cap;
      }

      if (cap <= 0) {
         cap = 64;
      }

      return cap > 64 ? this.field_49295.multiplyBy(Fraction.getFraction(64, cap)) : this.field_49295;
   }

   @Overwrite
   public static DataResult<class_9276> method_65061(List<class_1799> stacks) {
      List<class_1799> fixedStacks = new ArrayList();

      for(class_1799 stack : stacks) {
         if (!stack.method_7960() && stack.method_7947() > stack.method_7914()) {
            int count = stack.method_7947();

            int toAdd;
            for(int max = stack.method_7914(); count > 0; count -= toAdd) {
               toAdd = Math.min(count, max);
               fixedStacks.add(stack.method_46651(toAdd));
            }
         } else {
            fixedStacks.add(stack);
         }
      }

      return DataResult.success(new class_9276(fixedStacks));
   }

   @Overwrite
   public static Fraction method_57425(List<class_1799> stacks) {
      Fraction total = Fraction.ZERO;

      for(class_1799 stack : stacks) {
         try {
            Fraction itemOcc = method_57423(stack);
            Fraction stackOcc = itemOcc.multiplyBy(Fraction.getFraction(stack.method_7947(), 1));
            total = total.add(stackOcc);
         } catch (ArithmeticException var6) {
            total = Fraction.getFraction(1000, 1);
         }
      }

      return total;
   }

   @Overwrite
   public static Fraction method_57423(class_1799 stack) {
      class_9276 bundleContentsComponent = (class_9276)stack.method_58694(class_9334.field_49650);
      if (bundleContentsComponent != null) {
         BundleCapacityHolder.push(64 + BundleUpgradeTier.getTierFromItem(stack.method_7909()).totalExtraSlots);

         Fraction var2;
         try {
            var2 = bundleContentsComponent.method_57428();
         } finally {
            BundleCapacityHolder.pop();
         }

         return var2;
      } else {
         return stack.method_57826(class_9334.field_49624) ? Fraction.ONE : Fraction.getFraction(1, Math.max(1, stack.method_7914()));
      }
   }
}
