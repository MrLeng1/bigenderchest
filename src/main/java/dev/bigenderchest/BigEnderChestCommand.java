package dev.bigenderchest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BigEnderChestCommand implements CommandExecutor, TabCompleter {

    private final BigEnderChest plugin;

    public BigEnderChestCommand(BigEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /bigenderchest (kendi envanteri)
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cBu komutu sadece oyuncular kullanabilir!");
                return true;
            }

            if (!player.hasPermission("bigenderchest.use")) {
                player.sendMessage("§cBu komutu kullanma iznin yok!");
                return true;
            }

            EnderChestListener.openBigEnderChest(player);
            return true;
        }

        // /bigenderchest <oyuncu> (başka oyuncunun envanteri)
        if (args.length == 1) {
            if (!sender.hasPermission("bigenderchest.others")) {
                sender.sendMessage("§cBaşka bir oyuncunun Ender Sandığını açma iznin yok!");
                return true;
            }

            String targetName = args[0];
            Player onlineTarget = Bukkit.getPlayer(targetName);

            if (onlineTarget != null) {
                // Çevrimiçi oyuncu
                Inventory inv = plugin.getEnderChestManager().getEnderChest(onlineTarget);

                if (sender instanceof Player adminPlayer) {
                    adminPlayer.openInventory(inv);
                    adminPlayer.sendMessage("§a" + onlineTarget.getName() + " §7adlı oyuncunun Ender Sandığı açıldı.");
                } else {
                    sender.sendMessage("§a" + onlineTarget.getName() + " adlı oyuncunun Ender Sandığı konsoldan açılamaz.");
                }
            } else {
                // Çevrimdışı oyuncu kontrolü
                @SuppressWarnings("deprecation")
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);

                if (!offlineTarget.hasPlayedBefore()) {
                    sender.sendMessage("§c'" + targetName + "' adlı oyuncu bulunamadı!");
                    return true;
                }

                UUID uuid = offlineTarget.getUniqueId();
                Inventory inv = plugin.getEnderChestManager().getEnderChestByUUID(uuid, targetName);

                if (sender instanceof Player adminPlayer) {
                    adminPlayer.openInventory(inv);
                    adminPlayer.sendMessage("§a" + targetName + " §7(çevrimdışı) adlı oyuncunun Ender Sandığı açıldı.");
                } else {
                    sender.sendMessage("§cKonsoldan çevrimdışı oyuncu envanteri açılamaz.");
                }
            }

            return true;
        }

        sender.sendMessage("§cKullanım: /bigenderchest [oyuncu]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("bigenderchest.others")) {
            String partial = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    completions.add(p.getName());
                }
            }
        }

        return completions;
    }
}
