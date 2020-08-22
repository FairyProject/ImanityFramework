package org.imanity.framework.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HiddenUtil {

    public static void hidePlayerFromAnySide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.hidePlayer(player1);
                player1.hidePlayer(player);
            }
        }
    }

    public static void showPlayerFromAnySide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.showPlayer(player1);
                player1.showPlayer(player);
            }
        }
    }

    public static void hidePlayerFromThirdSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player1.hidePlayer(player);
            }
        }
    }

    public static void showPlayerFromThirdSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player1.showPlayer(player);
            }
        }
    }

    public static void hidePlayerFromFirstSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.hidePlayer(player1);
            }
        }
    }

    public static void showPlayerFromFirstSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.showPlayer(player1);
            }
        }
    }

}
