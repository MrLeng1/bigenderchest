package dev.bigenderchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class EnderChestListener implements Listener {

    private final BigEnderChest plugin;

    public EnderChestListener(BigEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Intercepts right-clicking on a vanilla Ender Chest block and opens
     * the 54-slot version instead.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnderChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("bigenderchest.use")) return;

        // Cancel vanilla ender chest opening
        event.setCancelled(true);

        // Open our 54-slot version
        openBigEnderChest(player);
    }

    /**
     * Saves inventory data when a player closes the BigEnderChest.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        EnderChestManager manager = plugin.getEnderChestManager();

        if (!manager.isBigEnderChest(inv)) return;

        // Only save if this player owns the inventory
        java.util.UUID owner = manager.getOwner(inv);
        if (owner == null) return;

        // Check if another viewer still has it open
        long otherViewers = inv.getViewers().stream()
                .filter(v -> !v.getUniqueId().equals(player.getUniqueId()))
                .count();

        if (otherViewers == 0) {
            manager.saveAndClose(owner);
        }
    }

    /**
     * Saves and removes inventory when a player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEnderChestManager().saveAndClose(event.getPlayer().getUniqueId());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    public static void openBigEnderChest(Player player) {
        EnderChestManager manager = BigEnderChest.getInstance().getEnderChestManager();
        Inventory inv = manager.getEnderChest(player);
        player.openInventory(inv);
    }
}
