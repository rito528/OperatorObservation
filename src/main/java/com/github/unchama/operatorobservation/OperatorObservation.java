package com.github.unchama.operatorobservation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public final class OperatorObservation extends JavaPlugin {

    private static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        getLogger().info("OperatorObservation Enabled.");
        checkOp(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("OperatorObservation Disabled.");
    }

    private static Boolean once = false;

    private static void checkOp(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                //configで指定されているop数とBukkitで登録されているop数が一致しなかった場合
                List<String> operators = config.getStringList("operators");
                if (operators.size() != Bukkit.getOperators().size()) {
                    if (!once) {
                        once = true;
                        removeOperators(plugin);
                        setOperators(plugin);
                    }
                }
                //configで指定されているオペレーターと実際に登録されているプレイヤーが一致しているかどうか
                Bukkit.getOperators().forEach(p -> {
                    if(!operators.contains(p.getUniqueId().toString())) {
                        if (!once) {
                            once = true;
                            removeOperators(plugin);
                            setOperators(plugin);
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(plugin,0,5);
    }

    private static void removeOperators(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOperators().forEach(p -> {
                    p.setOp(false);
                });
            }
        }.runTask(plugin);
    }

    private static void setOperators(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                config.getStringList("operators").forEach(uuid -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    p.setOp(true);
                    if (p.isOnline()) {
                        ((Player) p).sendMessage(ChatColor.YELLOW + "[OP変更検知] "+ ChatColor.AQUA + "OPを初期状態に修正しました。");
                    }
                    once = false;
                });
            }
        }.runTask(plugin);
    }

}
