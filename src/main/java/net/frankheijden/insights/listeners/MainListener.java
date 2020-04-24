package net.frankheijden.insights.listeners;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.builders.Scanner;
import net.frankheijden.insights.config.Limit;
import net.frankheijden.insights.config.RegionBlocks;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.enums.LogType;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.events.*;
import net.frankheijden.insights.managers.*;
import net.frankheijden.insights.tasks.UpdateCheckerTask;
import net.frankheijden.insights.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.*;

public class MainListener implements Listener {

    private static final Insights plugin = Insights.getInstance();
    private static final SelectionManager selectionManager = SelectionManager.getInstance();
    private final InteractListener interactListener;
    private final List<Location> blockLocations;

    public MainListener(InteractListener interactListener) {
        this.interactListener = interactListener;
        this.blockLocations = new ArrayList<>();
    }

    public InteractListener getInteractListener() {
        return interactListener;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String name = block.getType().name();

        if (selectionManager.isSelecting(player.getUniqueId())) {
            selectionManager.setPos1(player.getUniqueId(), block.getLocation(), true);
            event.setCancelled(true);
        }

        if (blockLocations.contains(block.getLocation())) {
            event.setCancelled(true);
            LogManager.log(LogType.WARNING, "Player " + player.getPlayerListName()
                    + " removed block '"
                    + name + "' too fast in "
                    + LocationUtils.toString(block.getLocation()) + ".");
            return;
        }

        Limit limit = InsightsAPI.getLimit(player.getWorld(), name);
        if (limit != null && !isPassiveForPlayer(player, "block")) {
            sendBreakMessage(player, event.getBlock().getChunk(), limit);
        } else if (TileUtils.isTile(event.getBlock()) && !isPassiveForPlayer(player, "tile")) {
            int generalLimit = plugin.getConfiguration().GENERAL_LIMIT;
            if (plugin.getConfiguration().GENERAL_ALWAYS_SHOW_NOTIFICATION || generalLimit > -1) {
                int current = event.getBlock().getLocation().getChunk().getTileEntities().length - 1;
                tryNotifyRealtime(player, current, generalLimit);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (selectionManager.isSelecting(player.getUniqueId())) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            selectionManager.setPos2(player.getUniqueId(), block.getLocation(), true);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    private void handlePistonEvent(Cancellable cancellable, List<Block> blocks) {
        for (Block block : blocks) {
            if (blockLocations.contains(block.getLocation())) {
                cancellable.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (blockLocations.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    private void sendBreakMessage(Player player, Chunk chunk, Limit limit) {
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = ChunkUtils.getAmountInChunk(chunk, chunkSnapshot, limit) - 1;
                sendMessage(player, limit.getName(), current, limit.getLimit());
            }
        }.runTaskAsynchronously(plugin);
    }

    private void sendMessage(Player player, String name, int current, int limit) {
        if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
            double progress = ((double) current)/((double) limit);
            if (progress > 1 || progress < 0) progress = 1;
            MessageUtils.sendSpecialMessage(player, "messages.realtime_check_custom", progress,
                    "%count%", NumberFormat.getIntegerInstance().format(current),
                    "%material%", StringUtils.capitalizeName(name),
                    "%limit%", NumberFormat.getIntegerInstance().format(limit));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEntityPlace(PlayerEntityPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        handleEntityPlace(event, player, entity.getLocation().getChunk(), entity.getType().name());
    }

    private void handleEntityPlace(Cancellable cancellable, Player player, Chunk chunk, String name) {
        if (!canPlaceInRegion(player, name) && !player.hasPermission("insights.regions.bypass." + name)) {
            cancellable.setCancelled(true);
            if (!isPassiveForPlayer(player, "region")) {
                MessageUtils.sendMessage(player, "messages.region_disallowed_block");
            }
            return;
        }

        Limit limit = InsightsAPI.getLimit(player, name);
        if (limit == null) return;
        int l = limit.getLimit();
        if (l < 0) return;
        int current = getEntityCount(chunk, name) + 1;

        if (current > l && !player.hasPermission(limit.getPermission())) {
            cancellable.setCancelled(true);
            if (!isPassiveForPlayer(player, "entity")) {
                MessageUtils.sendMessage(player, "messages.limit_reached_custom",
                        "%limit%", NumberFormat.getIntegerInstance().format(l),
                        "%material%", StringUtils.capitalizeName(limit.getName()));
            }
            return;
        }

        if (!isPassiveForPlayer(player, "entity")) {
            sendMessage(player, limit.getName(), current, l);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEntityDestroy(PlayerEntityDestroyEvent event) {
        handleEntityDestroy(event.getPlayer(), event.getEntity());
    }

    public void handleEntityDestroy(Player player, Entity entity) {
        if (isPassiveForPlayer(player, "entity")) return;
        String name = entity.getType().name();

        Limit limit = InsightsAPI.getLimit(player, name);
        if (limit == null) return;
        int l = limit.getLimit();
        if (l < 0) return;
        int current = getEntityCount(entity.getLocation().getChunk(), name) - 1;

        sendMessage(player, limit.getName(), current, l);
    }

    private int getEntityCount(Chunk chunk, String entityType) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType().name().equals(entityType)) count++;
        }
        return count;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (HookManager.getInstance().shouldCancel(block)) return;

        Player player = event.getPlayer();
        String name = block.getType().name();

        if (isNextToForbiddenLocation(block.getLocation())) {
            event.setCancelled(true);
            LogManager.log(LogType.WARNING, "Player " + player.getPlayerListName()
                    + " placed block '"
                    + name + "' too fast in "
                    + LocationUtils.toString(block.getLocation()) + ".");
            return;
        }

        if (!canPlaceInRegion(player, name) && !player.hasPermission("insights.regions.bypass." + name)) {
            event.setCancelled(true);
            if (!isPassiveForPlayer(player, "region")) {
                MessageUtils.sendMessage(player, "messages.region_disallowed_block");
            }
            return;
        }

        Limit limit = InsightsAPI.getLimit(player.getWorld(), name);
        if (limit != null) {
            handleBlockPlace(event, player, block, event.getItemInHand(), limit);
        } else if (TileUtils.isTile(event.getBlockPlaced())) {
            int current = event.getBlock().getLocation().getChunk().getTileEntities().length + 1;
            int generalLimit = plugin.getConfiguration().GENERAL_LIMIT;
            if (generalLimit > -1 && current >= generalLimit) {
                if (!player.hasPermission("insights.bypass")) {
                    event.setCancelled(true);
                    if (!isPassiveForPlayer(player, "tile")) {
                        MessageUtils.sendMessage(player, "messages.limit_reached",
                                "%limit%", NumberFormat.getIntegerInstance().format(generalLimit));
                    }
                }
            }

            if (plugin.getConfiguration().GENERAL_ALWAYS_SHOW_NOTIFICATION || generalLimit > -1) {
                if (!isPassiveForPlayer(player, "tile")) {
                    tryNotifyRealtime(player, current, generalLimit);
                }
            }
        }
    }

    private boolean canNotifyRealtime(Player player) {
        return player.hasPermission("insights.check.realtime")
                && plugin.getSqLite().hasRealtimeCheckEnabled(player);
    }

    private void tryNotifyRealtime(Player player, int current, int limit) {
        if (!canNotifyRealtime(player)) return;

        double progress = ((double) current)/((double) limit);
        if (progress > 1 || progress < 0) progress = 1;

        if (limit > -1) {
            MessageUtils.sendSpecialMessage(player, "messages.realtime_check", progress,
                    "%tile_count%", NumberFormat.getIntegerInstance().format(current),
                    "%limit%", NumberFormat.getIntegerInstance().format(limit));
        } else {
            MessageUtils.sendSpecialMessage(player, "messages.realtime_check_no_limit", progress,
                    "%tile_count%", NumberFormat.getIntegerInstance().format(current));
        }
    }

    private boolean isNextToForbiddenLocation(Location location) {
        for (Location loc : blockLocations) {
            if (isEqual(loc, location, -1, 0, 0)
                    || isEqual(loc, location, 1, 0, 0)
                    || isEqual(loc, location, 0, -1, 0)
                    || isEqual(loc, location, 0, 1, 0)
                    || isEqual(loc, location, 0, 0, -1)
                    || isEqual(loc, location, 0, 0, 1)) return true;
        }
        return false;
    }

    private boolean isEqual(Location loc1, Location loc2, int x, int y, int z) {
        return loc1.clone().add(x, y, z).equals(loc2);
    }

    private boolean canPlaceInRegion(Player player, String str) {
        WorldGuardManager worldGuardManager = WorldGuardManager.getInstance();
        if (worldGuardManager != null) {
            ProtectedRegion region = worldGuardManager.getRegionWithLimitedBlocks(player.getLocation());
            if (region != null) {
                return canPlaceInRegion(region.getId(), str);
            }
        }
        return true;
    }

    private boolean canPlaceInRegion(String region, String str) {
        List<RegionBlocks> regionBlocks = plugin.getConfiguration().GENERAL_REGION_BLOCKS;

        RegionBlocks matchedRegion = null;
        for (RegionBlocks rg : regionBlocks) {
            if (region.matches(rg.getRegex())) {
                matchedRegion = rg;
                break;
            }
        }

        if (matchedRegion != null) {
            List<String> strs = matchedRegion.getBlocks();
            if (matchedRegion.isWhitelist()) return strs.contains(str);
            else return !strs.contains(str);
        }
        return false;
    }

    private void handleBlockPlace(Cancellable event, Player player, Block block, ItemStack itemInHand, Limit limit) {
        ChunkSnapshot chunkSnapshot = block.getChunk().getChunkSnapshot();

        boolean async = plugin.getConfiguration().GENERAL_SCAN_ASYNC;
        if (async) {
            ItemStack itemStack = new ItemStack(itemInHand);
            itemStack.setAmount(1);

            if (!player.hasPermission(limit.getPermission())) {
                blockLocations.add(block.getLocation());
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    handleBlockPlace(event, player, block, chunkSnapshot, itemStack, async, limit);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            handleBlockPlace(event, player, block, chunkSnapshot, null, async, limit);
        }
    }

    private void handleBlockPlace(Cancellable event, Player player, Block block, ChunkSnapshot chunkSnapshot, ItemStack itemStack, boolean async, Limit limit) {
        int current = ChunkUtils.getAmountInChunk(block.getChunk(), chunkSnapshot, limit);
        int l = limit.getLimit();
        if (current > l) {
            if (!player.hasPermission(limit.getPermission())) {
                if (!isPassiveForPlayer(player, "block")) {
                    MessageUtils.sendMessage(player, "messages.limit_reached_custom",
                            "%limit%", NumberFormat.getIntegerInstance().format(l),
                            "%material%", StringUtils.capitalizeName(limit.getName()));
                }
                if (async) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.getInventory().addItem(itemStack);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(Material.AIR);
                            blockLocations.remove(block.getLocation());
                        }
                    }.runTask(plugin);
                } else {
                    blockLocations.remove(block.getLocation());
                    event.setCancelled(true);
                }
                return;
            }
        }
        if (!isPassiveForPlayer(player, "block")) {
            sendMessage(player, limit.getName(), current, l);
        }
        blockLocations.remove(block.getLocation());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        BossBarManager bossBarManager = BossBarManager.getInstance();
        if (bossBarManager != null) {
            bossBarManager.refreshPersistentBossBar(player);
        }

        if (plugin.getConfiguration().GENERAL_UPDATES_CHECK) {
            if (player.hasPermission("insights.notification.update")) {
                UpdateCheckerTask.start(player);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk != toChunk) {
            PlayerChunkMoveEvent chunkEnterEvent = new PlayerChunkMoveEvent(event.getPlayer(), fromChunk, toChunk);
            Bukkit.getPluginManager().callEvent(chunkEnterEvent);
            if (chunkEnterEvent.isCancelled()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPlayerChunkMove(PlayerChunkMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String string = plugin.getSqLite().getAutoscan(player);
        Integer type = plugin.getSqLite().getAutoscanType(player);
        if (string != null && type != null) {
            List<String> strs = Arrays.asList(string.split(","));

            Chunk chunk = event.getToChunk();

            if (type == 0) {
                ScanOptions scanOptions = new ScanOptions();
                scanOptions.setScanType(ScanType.CUSTOM);
                scanOptions.setEntityTypes(strs);
                scanOptions.setMaterials(strs);
                scanOptions.setWorld(chunk.getWorld());
                scanOptions.setPartialChunks(Collections.singletonList(PartialChunk.from(chunk)));

                Scanner.create(scanOptions)
                        .scan()
                        .whenComplete((ev, err) -> handleAutoScan(player, ev));
            } else {
                ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Limit limit = plugin.getConfiguration().getLimits().getLimit(string);
                        int count = ChunkUtils.getAmountInChunk(chunk, chunkSnapshot, limit);

                        double progress = ((double) count)/((double) limit.getLimit());
                        if (progress > 1 || progress < 0) progress = 1;
                        MessageUtils.sendSpecialMessage(player, "messages.autoscan.limit_entry", progress,
                                "%key%", StringUtils.capitalizeName(limit.getName()),
                                "%count%", NumberFormat.getInstance().format(count),
                                "%limit%", NumberFormat.getInstance().format(limit.getLimit()));
                    }
                }.runTask(plugin);
            }
        }
    }

    private void handleAutoScan(Player player, ScanCompleteEvent event) {
        TreeMap<String, Integer> counts = event.getScanResult().getCounts();

        if (counts.size() == 1) {
            Map.Entry<String, Integer> entry = counts.firstEntry();
            MessageUtils.sendSpecialMessage(player, "messages.autoscan.single_entry", 1.0,
                    "%key%", StringUtils.capitalizeName(entry.getKey()),
                    "%count%", NumberFormat.getInstance().format(entry.getValue()));
        } else {
            MessageUtils.sendMessage(player, "messages.autoscan.multiple_entries.header");
            for (String str : counts.keySet()) {
                MessageUtils.sendMessage(player, "messages.autoscan.multiple_entries.format",
                        "%entry%", StringUtils.capitalizeName(str),
                        "%count%", NumberFormat.getInstance().format(counts.get(str)));
            }
            MessageUtils.sendMessage(player, "messages.autoscan.multiple_entries.footer");
        }
    }

    private boolean isPassiveForPlayer(Player player, String what) {
        if (plugin.getConfiguration().GENERAL_NOTIFICATION_PASSIVE.contains(what)) {
            return !player.hasPermission("insights.check.passive." + what);
        }
        return false;
    }
}
