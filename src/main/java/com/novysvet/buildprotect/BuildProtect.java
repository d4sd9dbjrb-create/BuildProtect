package com.novysvet.buildprotect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildProtect extends JavaPlugin implements Listener {
    
    // Режим защиты для каждого игрока
    private Map<UUID, Boolean> protectMode = new HashMap<>();
    
    // База данных блоков
    private BlockDatabase database;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Инициализация БД
        String dbType = getConfig().getString("database.type", "json");
        if (dbType.equals("sqlite")) {
            database = new SQLiteDatabase(this);
        } else {
            database = new JSONDatabase(this);
        }
        
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("p").setExecutor(new ProtectCommand(this));
        
        getLogger().info("BuildProtect включён! Используйте /p для защиты блоков.");
    }
    
    @Override
    public void onDisable() {
        if (database != null) {
            database.save();
        }
        getLogger().info("BuildProtect выключен!");
    }
    
    // Установка блока
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Проверить режим защиты
        if (isProtectModeEnabled(player)) {
            Block block = event.getBlock();
            database.setOwner(block, player.getUniqueId());
            
            // Уведомление (опционально)
            if (getConfig().getBoolean("notify-on-place", false)) {
                player.sendMessage("§a[✓] Блок защищён!");
            }
        }
    }
    
    // Попытка сломать блок
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        UUID owner = database.getOwner(block);
        
        // Если блок ничей — можно ломать
        if (owner == null) {
            return;
        }
        
        // Если игрок — владелец
        if (owner.equals(player.getUniqueId())) {
            database.removeBlock(block);
            return;
        }
        
        // Если игрок в списке доверенных
        if (database.isTrusted(owner, player.getUniqueId())) {
            database.removeBlock(block);
            return;
        }
        
        // ЗАПРЕТИТЬ
        event.setCancelled(true);
        
        if (getConfig().getBoolean("notify-on-break-attempt", true)) {
            String ownerName = getServer().getOfflinePlayer(owner).getName();
            player.sendMessage("§c[✗] Этот блок принадлежит игроку " + ownerName + "!");
            
            // Звук
            if (getConfig().getBoolean("sounds.enable", true)) {
                try {
                    String soundName = getConfig().getString("sounds.break-denied", "ENTITY_VILLAGER_NO");
                    player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
                } catch (Exception e) {
                    // Игнорировать если звук не найден
                }
            }
        }
    }
    
    // Взаимодействие (сундуки, двери, кнопки)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        
        // Проверить только контейнеры и механизмы
        if (!isProtectable(block)) {
            return;
        }
        
        UUID owner = database.getOwner(block);
        if (owner == null) return;
        
        // Владелец или доверенный — разрешить
        if (owner.equals(player.getUniqueId()) || 
            database.isTrusted(owner, player.getUniqueId())) {
            return;
        }
        
        // ЗАПРЕТИТЬ
        event.setCancelled(true);
        String ownerName = getServer().getOfflinePlayer(owner).getName();
        player.sendMessage("§c[✗] Этот блок принадлежит игроку " + ownerName + "!");
    }
    
    // Проверка типа блока
    private boolean isProtectable(Block block) {
        String type = block.getType().name();
        return type.contains("CHEST") || 
               type.contains("BARREL") ||
               type.contains("SHULKER") ||
               type.contains("DOOR") || 
               type.contains("BUTTON") ||
               type.contains("LEVER") ||
               type.contains("TRAPDOOR") ||
               type.contains("FURNACE") ||
               type.contains("HOPPER") ||
               type.contains("DISPENSER") ||
               type.contains("DROPPER");
    }
    
    // Режим защиты
    public boolean isProtectModeEnabled(Player player) {
        return protectMode.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setProtectMode(Player player, boolean enabled) {
        protectMode.put(player.getUniqueId(), enabled);
    }
    
    public BlockDatabase getDatabase() {
        return database;
    }
}
