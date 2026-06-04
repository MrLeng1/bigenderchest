package dev.bigenderchest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestManager {

    private final BigEnderChest plugin;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final File dataFolder;

    public EnderChestManager(BigEnderChest plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "enderchests");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Gets or creates the 54-slot ender chest inventory for a player.
     */
    public Inventory getEnderChest(Player player) {
        UUID uuid = player.getUniqueId();

        if (openInventories.containsKey(uuid)) {
            return openInventories.get(uuid);
        }

        // Load from disk
        Inventory inv = loadInventory(uuid, player.getName());
        openInventories.put(uuid, inv);
        return inv;
    }

    /**
     * Gets an offline player's ender chest inventory by UUID.
     */
    public Inventory getEnderChestByUUID(UUID uuid, String name) {
        if (openInventories.containsKey(uuid)) {
            return openInventories.get(uuid);
        }
        Inventory inv = loadInventory(uuid, name);
        openInventories.put(uuid, inv);
        return inv;
    }

    /**
     * Saves the inventory when a player closes it.
     */
    public void saveAndClose(UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            saveInventory(uuid, openInventories.get(uuid));
            openInventories.remove(uuid);
        }
    }

    /**
     * Saves all currently open inventories (called on disable).
     */
    public void saveAll() {
        for (Map.Entry<UUID, Inventory> entry : openInventories.entrySet()) {
            saveInventory(entry.getKey(), entry.getValue());
        }
        openInventories.clear();
        plugin.getLogger().info("Tüm EnderChest verileri kaydedildi. (" + openInventories.size() + " envanter)");
    }

    /**
     * Checks if a given inventory is a BigEnderChest inventory.
     */
    public boolean isBigEnderChest(Inventory inv) {
        return openInventories.containsValue(inv);
    }

    /**
     * Returns the UUID owner of an open inventory, or null if not found.
     */
    public UUID getOwner(Inventory inv) {
        for (Map.Entry<UUID, Inventory> entry : openInventories.entrySet()) {
            if (entry.getValue().equals(inv)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private Inventory loadInventory(UUID uuid, String playerName) {
        String title = plugin.getConfig().getString("inventory-title", "§5§lEnder Sandık §8[54]");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) return inv;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for (int i = 0; i < 54; i++) {
            if (cfg.contains("slot." + i)) {
                ItemStack item = cfg.getItemStack("slot." + i);
                if (item != null) {
                    inv.setItem(i, item);
                }
            }
        }

        return inv;
    }

    private void saveInventory(UUID uuid, Inventory inv) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration cfg = new YamlConfiguration();

        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                cfg.set("slot." + i, contents[i]);
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("EnderChest verisi kaydedilemedi: " + uuid + " - " + e.getMessage());
        }
    }
}
