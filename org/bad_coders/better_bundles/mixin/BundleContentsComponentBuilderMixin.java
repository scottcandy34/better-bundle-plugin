package org.bad_coders.better_bundles.mixin;

import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import org.apache.commons.lang3.math.Fraction;
import org.bad_coders.better_bundles.BundleCapacityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net/minecraft/class_9276$class_9277"}
)
public abstract class BundleContentsComponentBuilderMixin {
   @Shadow
   private Fraction field_49297;
   @Shadow
   @Final
   private List<class_1799> field_49296;

   @Shadow
   private int method_57436(class_1799 stack) {
      return 0;
   }

   @Inject(
      method = {"method_57436(Lnet/minecraft/class_1799;)I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void better_bundles$getDynamicMaxAllowed(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      int capacity = BundleCapacityHolder.get();
      if (capacity > 64) {
         Fraction limit = Fraction.getFraction(capacity, 64);
         Fraction remaining = limit.subtract(this.field_49297);
         class_9276 inner = (class_9276)stack.method_58694(class_9334.field_49650);
         Fraction itemOcc;
         if (inner != null) {
            itemOcc = Fraction.getFraction(1, 16).add(inner.method_57428());
         } else if (stack.method_57826(class_9334.field_49624)) {
            itemOcc = Fraction.ONE;
         } else {
            itemOcc = Fraction.getFraction(1, Math.max(1, stack.method_7914()));
         }

         if (itemOcc.equals(Fraction.ZERO)) {
            cir.setReturnValue(64);
         } else {
            int allowed = remaining.divideBy(itemOcc).intValue();
            cir.setReturnValue(Math.max(0, allowed));
         }
      }

   }

   @Overwrite
   public int method_57432(class_1799 stack) {
      if (!class_9276.method_61667(stack)) {
         return 0;
      } else {
         int totalCanAdd = this.method_57436(stack);
         totalCanAdd = Math.min(stack.method_7947(), totalCanAdd);
         if (totalCanAdd <= 0) {
            return 0;
         } else {
            class_9276 inner = (class_9276)stack.method_58694(class_9334.field_49650);
            Fraction itemOcc;
            if (inner != null) {
               itemOcc = Fraction.getFraction(1, 16).add(inner.method_57428());
            } else if (stack.method_57826(class_9334.field_49624)) {
               itemOcc = Fraction.ONE;
            } else {
               itemOcc = Fraction.getFraction(1, Math.max(1, stack.method_7914()));
            }

            int toAddInThisChunk;
            for(int remaining = totalCanAdd; remaining > 0; remaining -= toAddInThisChunk) {
               int insertionIndex = -1;
               if (stack.method_7946()) {
                  for(int i = 0; i < this.field_49296.size(); ++i) {
                     class_1799 s = (class_1799)this.field_49296.get(i);
                     if (class_1799.method_31577(s, stack) && s.method_7947() < s.method_7914()) {
                        insertionIndex = i;
                        break;
                     }
                  }
               }

               if (insertionIndex != -1) {
                  class_1799 existing = (class_1799)this.field_49296.remove(insertionIndex);
                  int space = existing.method_7914() - existing.method_7947();
                  toAddInThisChunk = Math.min(remaining, space);
                  class_1799 newStack = existing.method_46651(existing.method_7947() + toAddInThisChunk);
                  this.field_49296.add(0, newStack);
                  stack.method_7934(toAddInThisChunk);
               } else {
                  toAddInThisChunk = Math.min(remaining, stack.method_7914());
                  this.field_49296.add(0, stack.method_7971(toAddInThisChunk));
               }

               this.field_49297 = this.field_49297.add(itemOcc.multiplyBy(Fraction.getFraction(toAddInThisChunk, 1)));
            }

            return totalCanAdd;
         }
      }
   }
}
