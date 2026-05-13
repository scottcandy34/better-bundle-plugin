package org.bad_coders.better_bundles.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_3218;
import net.minecraft.class_9276;
import org.bad_coders.better_bundles.EnderBundleState;
import org.bad_coders.better_bundles.EnderBundleStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({class_1657.class})
public abstract class PlayerEntityMixin implements EnderBundleStorage {
   @Unique
   private class_9276 better_bundles$enderBundleContents;

   public PlayerEntityMixin() {
      this.better_bundles$enderBundleContents = class_9276.field_49289;
   }

   public class_9276 better_bundles$getEnderBundleContents() {
      class_1657 player = (class_1657)this;
      return player.method_73183() instanceof class_3218 ? EnderBundleState.getServerState(player.method_73183().method_8503()).getContents(player.method_5667()) : this.better_bundles$enderBundleContents;
   }

   public void better_bundles$setEnderBundleContents(class_9276 contents) {
      this.better_bundles$enderBundleContents = contents;
      class_1657 player = (class_1657)this;
      if (player.method_73183() instanceof class_3218) {
         EnderBundleState.getServerState(player.method_73183().method_8503()).setContents(player.method_5667(), contents);
      }

   }
}
