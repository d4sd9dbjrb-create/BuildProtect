package com.novysvet.buildprotect;

import org.bukkit.block.Block;

import java.util.Set;
import java.util.UUID;

/**
 * Интерфейс для хранения данных о защищённых блоках
 */
public interface BlockDatabase {
    
    /**
     * Установить владельца блока
     */
    void setOwner(Block block, UUID owner);
    
    /**
     * Получить владельца блока
     * @return UUID владельца или null если блок ничей
     */
    UUID getOwner(Block block);
    
    /**
     * Удалить блок из базы
     */
    void removeBlock(Block block);
    
    /**
     * Добавить игрока в доверенные
     */
    void addTrusted(UUID owner, UUID trusted);
    
    /**
     * Убрать игрока из доверенных
     */
    void removeTrusted(UUID owner, UUID trusted);
    
    /**
     * Проверить доверенный ли игрок
     */
    boolean isTrusted(UUID owner, UUID player);
    
    /**
     * Получить список доверенных игроков
     */
    Set<UUID> getTrustedPlayers(UUID owner);
    
    /**
     * Получить количество защищённых блоков игрока
     */
    int getBlockCount(UUID owner);
    
    /**
     * Сохранить данные
     */
    void save();
}
