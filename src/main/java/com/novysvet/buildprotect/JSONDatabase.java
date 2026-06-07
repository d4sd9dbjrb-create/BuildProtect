package com.novysvet.buildprotect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.block.Block;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация базы данных через JSON файлы
 */
public class JSONDatabase implements BlockDatabase {
    
    private final BuildProtect plugin;
    private final File dataFile;
    private final Gson gson;
    
    // Хранилище: "world:x:y:z" -> UUID владельца
    private Map<String, UUID> blocks;
    
    // Доверенные игроки: UUID владельца -> Set<UUID доверенных>
    private Map<UUID, Set<UUID>> trustedPlayers;
    
    public JSONDatabase(BuildProtect plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Создать папку если нет
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        this.dataFile = new File(dataFolder, "blocks.json");
        this.blocks = new ConcurrentHashMap<>();
        this.trustedPlayers = new ConcurrentHashMap<>();
        
        load();
    }
    
    @Override
    public void setOwner(Block block, UUID owner) {
        String key = getBlockKey(block);
        blocks.put(key, owner);
    }
    
    @Override
    public UUID getOwner(Block block) {
        String key = getBlockKey(block);
        return blocks.get(key);
    }
    
    @Override
    public void removeBlock(Block block) {
        String key = getBlockKey(block);
        blocks.remove(key);
    }
    
    @Override
    public void addTrusted(UUID owner, UUID trusted) {
        trustedPlayers.computeIfAbsent(owner, k -> new HashSet<>()).add(trusted);
        save();
    }
    
    @Override
    public void removeTrusted(UUID owner, UUID trusted) {
        Set<UUID> set = trustedPlayers.get(owner);
        if (set != null) {
            set.remove(trusted);
            save();
        }
    }
    
    @Override
    public boolean isTrusted(UUID owner, UUID player) {
        Set<UUID> set = trustedPlayers.get(owner);
        return set != null && set.contains(player);
    }
    
    @Override
    public Set<UUID> getTrustedPlayers(UUID owner) {
        return trustedPlayers.getOrDefault(owner, new HashSet<>());
    }
    
    @Override
    public int getBlockCount(UUID owner) {
        return (int) blocks.values().stream()
            .filter(uuid -> uuid.equals(owner))
            .count();
    }
    
    @Override
    public void save() {
        try {
            // Создать объект для сохранения
            DataContainer data = new DataContainer();
            data.blocks = blocks;
            data.trustedPlayers = trustedPlayers;
            
            // Записать в файл
            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            }
            
            plugin.getLogger().info("Данные сохранены: " + blocks.size() + " блоков");
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void load() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("Файл данных не найден, создаём новый.");
            return;
        }
        
        try (Reader reader = new FileReader(dataFile)) {
            DataContainer data = gson.fromJson(reader, DataContainer.class);
            
            if (data != null) {
                if (data.blocks != null) {
                    blocks = new ConcurrentHashMap<>(data.blocks);
                }
                if (data.trustedPlayers != null) {
                    trustedPlayers = new ConcurrentHashMap<>(data.trustedPlayers);
                }
            }
            
            plugin.getLogger().info("Загружено " + blocks.size() + " защищённых блоков");
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка загрузки данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getBlockKey(Block block) {
        return block.getWorld().getName() + ":" + 
               block.getX() + ":" + 
               block.getY() + ":" + 
               block.getZ();
    }
    
    /**
     * Контейнер для сохранения в JSON
     */
    private static class DataContainer {
        Map<String, UUID> blocks;
        Map<UUID, Set<UUID>> trustedPlayers;
    }
}
