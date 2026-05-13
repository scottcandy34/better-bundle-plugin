package org.bad_coders.better_bundles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_10741;
import net.minecraft.class_18;
import net.minecraft.class_26;
import net.minecraft.class_3218;
import net.minecraft.class_4284;
import net.minecraft.class_9276;
import net.minecraft.server.MinecraftServer;

public class EnderBundleState extends class_18 {
   private final Map<UUID, class_9276> playerBundles;
   public static final Codec<EnderBundleState> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.unboundedMap(Codec.STRING, class_9276.field_49290).fieldOf("Bundles").forGetter((state) -> {
         Map<String, class_9276> stringMap = new HashMap();
         state.playerBundles.forEach((uuid, contents) -> stringMap.put(uuid.toString(), contents));
         return stringMap;
      })).apply(instance, (stringMap) -> {
         Map<UUID, class_9276> uuidMap = new HashMap();
         stringMap.forEach((s, contents) -> uuidMap.put(UUID.fromString(s), contents));
         return new EnderBundleState(uuidMap);
      }));

   public EnderBundleState() {
      this(new HashMap());
   }

   public EnderBundleState(Map<UUID, class_9276> playerBundles) {
      this.playerBundles = playerBundles;
   }

   public class_9276 getContents(UUID uuid) {
      return (class_9276)this.playerBundles.getOrDefault(uuid, class_9276.field_49289);
   }

   public void setContents(UUID uuid, class_9276 contents) {
      this.playerBundles.put(uuid, contents);
      this.method_80();
   }

   public static EnderBundleState getServerState(MinecraftServer server) {
      class_26 persistentStateManager = server.method_3847(class_3218.field_25179).method_17983();
      class_10741<EnderBundleState> type = new class_10741("better_bundles_ender_bundles", EnderBundleState::new, CODEC, class_4284.field_19212);
      return (EnderBundleState)persistentStateManager.method_17924(type);
   }
}
