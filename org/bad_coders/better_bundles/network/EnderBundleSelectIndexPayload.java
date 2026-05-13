package org.bad_coders.better_bundles.network;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record EnderBundleSelectIndexPayload(int slotId, int index) implements class_8710 {
   public static final class_8710.class_9154<EnderBundleSelectIndexPayload> ID = new class_8710.class_9154(class_2960.method_60655("better_bundles", "ender_select_index"));
   public static final class_9139<class_9129, EnderBundleSelectIndexPayload> CODEC;

   public class_8710.class_9154<? extends class_8710> method_56479() {
      return ID;
   }

   static {
      CODEC = class_9139.method_56435(class_9135.field_48550, EnderBundleSelectIndexPayload::slotId, class_9135.field_48550, EnderBundleSelectIndexPayload::index, EnderBundleSelectIndexPayload::new);
   }
}
