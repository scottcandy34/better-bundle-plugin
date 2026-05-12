package com.grok.betterbundles;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum BundleTier {
    BUNDLE(64, "Bundle", ChatColor.WHITE, 10001, Material.BUNDLE),
    IRON(256, "Iron Bundle", ChatColor.GRAY, 10002, Material.BUNDLE),
    GOLD(512, "Gold Bundle", ChatColor.GOLD, 10003, Material.BUNDLE),
    DIAMOND(768, "Diamond Bundle", ChatColor.AQUA, 10004, Material.BUNDLE),
    NETHERITE(1024, "Netherite Bundle", ChatColor.DARK_PURPLE, 10005, Material.BUNDLE);

    private final int capacity;
    private final String displayName;
    private final ChatColor color;
    private final int customModelData;
    private final Material material;

    BundleTier(int capacity, String displayName, ChatColor color, int customModelData, Material material) {
        this.capacity = capacity;
        this.displayName = displayName;
        this.color = color;
        this.customModelData = customModelData;
        this.material = material;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Material getMaterial() {
        return material;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public static BundleTier fromString(String name) {
        for (BundleTier tier : values()) {
            if (tier.name().equalsIgnoreCase(name) || tier.getDisplayName().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return BUNDLE;
    }
}