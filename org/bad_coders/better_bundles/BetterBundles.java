package org.bad_coders.better_bundles;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1865;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_5537;
import net.minecraft.class_7706;
import net.minecraft.class_7710;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_9129;
import net.minecraft.class_9139;
import net.minecraft.class_9276;
import net.minecraft.class_9334;
import org.bad_coders.better_bundles.network.EnderBundleSelectIndexPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterBundles implements ModInitializer {
   public static final String MOD_ID = "better_bundles";
   public static final Logger LOGGER = LoggerFactory.getLogger("better_bundles");
   public static final class_1792 IRON_BUNDLE = registerBundle("iron_bundle");
   public static final class_1792 GOLD_BUNDLE = registerBundle("gold_bundle");
   public static final class_1792 DIAMOND_BUNDLE = registerBundle("diamond_bundle");
   public static final class_1792 NETHERITE_BUNDLE = registerBundle("netherite_bundle");
   public static final class_1865<OreBundleUpgradeRecipe> ORE_BUNDLE_UPGRADE;
   public static final class_1865<NetheriteBundleSmithingRecipe> NETHERITE_BUNDLE_SMITHING;

   private static class_1792 registerBundle(String name) {
      class_2960 id = class_2960.method_60655("better_bundles", name);
      class_5321<class_1792> key = class_5321.method_29179(class_7924.field_41197, id);
      class_1792.class_1793 settings = (new class_1792.class_1793()).method_63686(key).method_7889(1).method_57349(class_9334.field_49650, class_9276.field_49289);
      if (name.equals("netherite_bundle")) {
         settings = settings.method_24359();
      }

      return (class_1792)class_2378.method_39197(class_7923.field_41178, key, new class_5537(settings));
   }

   public void onInitialize() {
      PayloadTypeRegistry.playC2S().register(EnderBundleSelectIndexPayload.ID, EnderBundleSelectIndexPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(EnderBundleSelectIndexPayload.ID, (payload, ctx) -> ctx.server().execute(() -> {
            class_1703 handler = ctx.player().field_7512;
            if (payload.slotId() >= 0 && payload.slotId() < handler.field_7761.size()) {
               class_1735 slot = handler.method_7611(payload.slotId());
               class_1799 stack = slot.method_7677();
               if (stack.method_7909() instanceof class_5537) {
                  class_9276 contents = (class_9276)stack.method_58694(class_9334.field_49650);
                  if (contents != null && !contents.method_57429()) {
                     int size = contents.method_61666();
                     int idx = Math.floorMod(payload.index(), Math.max(1, size));
                     class_9276.class_9277 b = new class_9276.class_9277(contents);
                     b.method_64662(idx);
                     stack.method_57379(class_9334.field_49650, b.method_57435());
                  }
               }
            }

         }));
      ItemGroupEvents.modifyEntriesEvent(class_7706.field_41060).register((ItemGroupEvents.ModifyEntries)(entries) -> {
         entries.method_45421(IRON_BUNDLE);
         entries.method_45421(GOLD_BUNDLE);
         entries.method_45421(DIAMOND_BUNDLE);
         entries.method_45421(NETHERITE_BUNDLE);
      });
      ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayConnectionEvents.Disconnect)(handler, server) -> BundleBackupManager.savePlayerBundles(handler.field_14140, server));
      ServerLifecycleEvents.SERVER_STOPPING.register(BundleBackupManager::saveAllBundles);
      ServerTickEvents.END_SERVER_TICK.register(BundleBackupManager::onTick);
      LOGGER.info("Better Bundles initialized!");
   }

   static {
      ORE_BUNDLE_UPGRADE = (class_1865)class_2378.method_10230(class_7923.field_41189, class_2960.method_60655("better_bundles", "ore_bundle_upgrade"), new class_1865<OreBundleUpgradeRecipe>() {
         public MapCodec<OreBundleUpgradeRecipe> method_53736() {
            return MapCodec.unit(() -> new OreBundleUpgradeRecipe(class_7710.field_40251));
         }

         public class_9139<class_9129, OreBundleUpgradeRecipe> method_56104() {
            return class_9139.method_56431(new OreBundleUpgradeRecipe(class_7710.field_40251));
         }
      });
      NETHERITE_BUNDLE_SMITHING = (class_1865)class_2378.method_10230(class_7923.field_41189, class_2960.method_60655("better_bundles", "netherite_bundle_smithing"), new class_1865<NetheriteBundleSmithingRecipe>() {
         public MapCodec<NetheriteBundleSmithingRecipe> method_53736() {
            return MapCodec.unit(NetheriteBundleSmithingRecipe::new);
         }

         public class_9139<class_9129, NetheriteBundleSmithingRecipe> method_56104() {
            return class_9139.method_56431(new NetheriteBundleSmithingRecipe());
         }
      });
   }
}
