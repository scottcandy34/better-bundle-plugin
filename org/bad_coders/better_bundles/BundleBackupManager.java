package org.bad_coders.better_bundles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_5218;
import net.minecraft.class_5537;
import net.minecraft.class_7923;
import net.minecraft.class_9276;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.server.MinecraftServer;

public class BundleBackupManager {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final int SAVE_INTERVAL_TICKS = 6000;
   private static int tickCounter = 0;

   public static void onTick(MinecraftServer server) {
      ++tickCounter;
      if (tickCounter >= 6000) {
         tickCounter = 0;
         saveAllBundles(server);
      }

   }

   public static void saveAllBundles(MinecraftServer server) {
      Path backupDir = getBackupDir(server);
      if (backupDir != null) {
         for(class_3222 player : server.method_3760().method_14571()) {
            savePlayerBundles(player, backupDir);
         }

      }
   }

   public static void savePlayerBundles(class_3222 player, MinecraftServer server) {
      Path backupDir = getBackupDir(server);
      if (backupDir != null) {
         savePlayerBundles(player, backupDir);
      }
   }

   private static Path getBackupDir(MinecraftServer server) {
      Path backupDir = server.method_27050(class_5218.field_24188).resolve("better_bundles_backup");

      try {
         Files.createDirectories(backupDir);
         return backupDir;
      } catch (IOException e) {
         BetterBundles.LOGGER.error("[BetterBundles] Failed to create backup directory", e);
         return null;
      }
   }

   private static void savePlayerBundles(class_3222 player, Path backupDir) {
      JsonObject root = new JsonObject();
      root.addProperty("player_name", player.method_5477().getString());
      root.addProperty("player_uuid", player.method_5845());
      root.addProperty("last_saved", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
      JsonArray bundlesArray = new JsonArray();

      for(int i = 0; i < player.method_31548().method_5439(); ++i) {
         class_1799 stack = player.method_31548().method_5438(i);
         if (!stack.method_7960() && stack.method_7909() instanceof class_5537) {
            String location = slotName(i);
            bundlesArray.add(serializeBundle(stack, location));
         }
      }

      root.add("bundles", bundlesArray);
      Path playerFile = backupDir.resolve(player.method_5845() + ".json");

      try {
         Writer writer = new FileWriter(playerFile.toFile());

         try {
            GSON.toJson(root, writer);
         } catch (Throwable var9) {
            try {
               writer.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         writer.close();
      } catch (IOException e) {
         BetterBundles.LOGGER.error("[BetterBundles] Failed to write backup for {}", player.method_5477().getString(), e);
      }

   }

   private static JsonObject serializeBundle(class_1799 bundleStack, String location) {
      JsonObject obj = new JsonObject();
      obj.addProperty("location", location);
      obj.addProperty("bundle_type", class_7923.field_41178.method_10221(bundleStack.method_7909()).toString());
      obj.addProperty("bundle_tier", BundleUpgradeTier.getTierFromItem(bundleStack.method_7909()).getName());
      class_9276 contents = (class_9276)bundleStack.method_58694(class_9334.field_49650);
      JsonArray items = new JsonArray();
      if (contents != null) {
         for(class_1799 item : contents.method_59708()) {
            items.add(serializeItem(item));
         }
      }

      obj.add("contents", items);
      return obj;
   }

   private static JsonObject serializeItem(class_1799 stack) {
      JsonObject obj = new JsonObject();
      obj.addProperty("id", class_7923.field_41178.method_10221(stack.method_7909()).toString());
      obj.addProperty("count", stack.method_7947());
      if (stack.method_57826(class_9334.field_49631)) {
         class_2561 name = (class_2561)stack.method_58694(class_9334.field_49631);
         if (name != null) {
            obj.addProperty("custom_name", name.getString());
         }
      }

      if (stack.method_7963()) {
         obj.addProperty("damage", stack.method_7919());
         obj.addProperty("max_durability", stack.method_7936());
      }

      class_9304 enchantments = class_1890.method_57532(stack);
      if (!enchantments.method_57543()) {
         JsonArray enchArray = new JsonArray();
         enchantments.method_57534().forEach((entry) -> {
            int level = enchantments.method_57536(entry);
            JsonObject enchObj = new JsonObject();
            entry.method_40230().ifPresent((key) -> enchObj.addProperty("id", key.method_29177().toString()));
            enchObj.addProperty("level", level);
            enchArray.add(enchObj);
         });
         obj.add("enchantments", enchArray);
      }

      return obj;
   }

   private static String slotName(int slot) {
      if (slot < 9) {
         return "hotbar_" + slot;
      } else if (slot < 36) {
         return "inventory_" + slot;
      } else if (slot == 36) {
         return "feet_armor";
      } else if (slot == 37) {
         return "legs_armor";
      } else if (slot == 38) {
         return "chest_armor";
      } else if (slot == 39) {
         return "head_armor";
      } else {
         return slot == 40 ? "offhand" : "slot_" + slot;
      }
   }
}
