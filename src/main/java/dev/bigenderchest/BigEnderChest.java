package dev.bigenderchest;

import org.bukkit.plugin.java.JavaPlugin;

public class BigEnderChest extends JavaPlugin {

    private static BigEnderChest instance;
    private EnderChestManager enderChestManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize manager
        enderChestManager = new EnderChestManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new EnderChestListener(this), this);

        // Register commands
        BigEnderChestCommand command = new BigEnderChestCommand(this);
        getCommand("bigenderchest").setExecutor(command);
        getCommand("bigenderchest").setTabCompleter(command);

        getLogger().info("BigEnderChest v" + getDescription().getVersion() + " etkinleştirildi! (54 slot)");
    }

    @Override
    public void onDisable() {
        if (enderChestManager != null) {
            enderChestManager.saveAll();
        }
        getLogger().info("BigEnderChest devre dışı bırakıldı. Tüm veriler kaydedildi.");
    }

    public static BigEnderChest getInstance() {
        return instance;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }
}
