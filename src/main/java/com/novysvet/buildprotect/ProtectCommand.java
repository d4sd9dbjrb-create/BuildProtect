package com.novysvet.buildprotect;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class ProtectCommand implements CommandExecutor {
    
    private final BuildProtect plugin;
    
    public ProtectCommand(BuildProtect plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // /p (toggle)
        if (args.length == 0) {
            boolean current = plugin.isProtectModeEnabled(player);
            plugin.setProtectMode(player, !current);
            
            if (!current) {
                player.sendMessage("§a[✓] Режим защиты ВКЛЮЧЁН! Все блоки что ставишь — твои.");
                playSound(player, "protect-on");
            } else {
                player.sendMessage("§c[✗] Режим защиты ВЫКЛЮЧЕН.");
                playSound(player, "protect-off");
            }
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "on":
                plugin.setProtectMode(player, true);
                player.sendMessage("§a[✓] Режим защиты ВКЛЮЧЁН!");
                playSound(player, "protect-on");
                break;
                
            case "off":
                plugin.setProtectMode(player, false);
                player.sendMessage("§c[✗] Режим защиты ВЫКЛЮЧЕН.");
                playSound(player, "protect-off");
                break;
                
            case "status":
                boolean enabled = plugin.isProtectModeEnabled(player);
                player.sendMessage("§6Режим защиты: " + (enabled ? "§aВКЛ" : "§cВЫКЛ"));
                
                // Показать количество защищённых блоков
                int count = plugin.getDatabase().getBlockCount(player.getUniqueId());
                player.sendMessage("§6Защищённых блоков: §e" + count);
                break;
                
            case "trust":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /p trust <ник>");
                    return true;
                }
                
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                    player.sendMessage("§cИгрок не найден!");
                    return true;
                }
                
                plugin.getDatabase().addTrusted(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§a[✓] Игрок §e" + args[1] + "§a добавлен в доверенные!");
                player.sendMessage("§7Теперь он может ломать/открывать твои блоки.");
                break;
                
            case "untrust":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /p untrust <ник>");
                    return true;
                }
                
                OfflinePlayer targetUntrust = Bukkit.getOfflinePlayer(args[1]);
                if (targetUntrust == null) {
                    player.sendMessage("§cИгрок не найден!");
                    return true;
                }
                
                plugin.getDatabase().removeTrusted(player.getUniqueId(), targetUntrust.getUniqueId());
                player.sendMessage("§c[✗] Игрок §e" + args[1] + "§c убран из доверенных!");
                break;
                
            case "trustlist":
            case "list":
                Set<UUID> trusted = plugin.getDatabase().getTrustedPlayers(player.getUniqueId());
                if (trusted.isEmpty()) {
                    player.sendMessage("§6У тебя нет доверенных игроков.");
                } else {
                    player.sendMessage("§6=== Доверенные игроки ===");
                    for (UUID uuid : trusted) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                        player.sendMessage("§e- " + p.getName());
                    }
                }
                break;
                
            case "info":
                player.sendMessage("§6[i] Кликни по блоку чтобы узнать владельца!");
                player.sendMessage("§7(Функция в разработке)");
                break;
                
            case "help":
            default:
                player.sendMessage("§6=== BuildProtect ===");
                player.sendMessage("§e/p §7- вкл/выкл режим защиты");
                player.sendMessage("§e/p on §7- включить");
                player.sendMessage("§e/p off §7- выключить");
                player.sendMessage("§e/p status §7- проверить статус");
                player.sendMessage("§e/p trust <ник> §7- дать доступ");
                player.sendMessage("§e/p untrust <ник> §7- забрать доступ");
                player.sendMessage("§e/p trustlist §7- список доверенных");
                player.sendMessage("§e/p info §7- узнать владельца блока");
        }
        
        return true;
    }
    
    private void playSound(Player player, String soundKey) {
        if (!plugin.getConfig().getBoolean("sounds.enable", true)) {
            return;
        }
        
        try {
            String soundName = plugin.getConfig().getString("sounds." + soundKey);
            if (soundName != null) {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
            }
        } catch (Exception e) {
            // Игнорировать если звук не найден
        }
    }
}
