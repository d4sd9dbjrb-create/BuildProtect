package com.novysvet.buildprotect;

import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Заглушка для SQLite (пока не реализовано)
 * Использует JSONDatabase внутри
 */
public class SQLiteDatabase implements BlockDatabase {
    
    private final JSONDatabase jsonDb;
    
    public SQLiteDatabase(BuildProtect plugin) {
        plugin.getLogger().warning("SQLite пока не реализован, используется JSON");
        this.jsonDb = new JSONDatabase(plugin);
    }
    
    @Override
    public void setOwner(Block block, UUID owner) {
        jsonDb.setOwner(block, owner);
    }
    
    @Override
    public UUID getOwner(Block block) {
        return jsonDb.getOwner(block);
    }
    
    @Override
    public void removeBlock(Block block) {
        jsonDb.removeBlock(block);
    }
    
    @Override
    public void addTrusted(UUID owner, UUID trusted) {
        jsonDb.addTrusted(owner, trusted);
    }
    
    @Override
    public void removeTrusted(UUID owner, UUID trusted) {
        jsonDb.removeTrusted(owner, trusted);
    }
    
    @Override
    public boolean isTrusted(UUID owner, UUID player) {
        return jsonDb.isTrusted(owner, player);
    }
    
    @Override
    public Set<UUID> getTrustedPlayers(UUID owner) {
        return jsonDb.getTrustedPlayers(owner);
    }
    
    @Override
    public int getBlockCount(UUID owner) {
        return jsonDb.getBlockCount(owner);
    }
    
    @Override
    public void save() {
        jsonDb.save();
    }
}
