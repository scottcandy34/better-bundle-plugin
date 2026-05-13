package org.bad_coders.better_bundles;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;
import net.minecraft.class_9276;

public record EnderBundleSyncPayload(class_9276 contents) implements class_8710 {
   public static final class_8710.class_9154<EnderBundleSyncPayload> ID = new class_8710.class_9154(class_2960.method_60655("better_bundles", "ender_bundle_sync"));
   public static final class_9139<class_9129, EnderBundleSyncPayload> CODEC;

   public class_8710.class_9154<? extends class_8710> method_56479() {
      return ID;
   }

   static {
      CODEC = class_9139.method_56434(class_9276.field_49291, EnderBundleSyncPayload::contents, EnderBundleSyncPayload::new);
   }
}
