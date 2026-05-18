package com.scottcandy34.betterbundle;

import org.bukkit.entity.Player;

public enum BundleSound {

    /** When an item is successfully inserted into the bundle */
    INSERT("item.bundle.insert"),

    /** When an item fails to insert (blocked item or full) */
    INSERT_FAIL("item.bundle.insert_fail"),

    /** When removing a single item (right-click) */
    REMOVE_ONE("item.bundle.remove_one"),

    /** When dropping contents or emptying via shift-right click */
    DROP_CONTENTS("item.bundle.drop_contents");

    private final String soundKey;

    BundleSound(String soundKey) {
        this.soundKey = soundKey;
    }

    /**
     * Plays the sound at the player's location.
     */
    public void play(Player player) {
        if (player != null) {
            player.playSound(player.getLocation(), soundKey, 0.8f, 1.0f);
        }
    }
}