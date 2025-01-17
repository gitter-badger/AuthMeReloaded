package fr.xephi.authme.listener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.Utils;
import fr.xephi.authme.api.API;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.cache.limbo.LimboPlayer;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.plugin.manager.CombatTagComunicator;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.Settings;

public class AuthMePlayerListener implements Listener {

    public static GameMode gm = GameMode.SURVIVAL;
    public static ConcurrentHashMap<String, GameMode> gameMode = new ConcurrentHashMap<String, GameMode>();
    public static ConcurrentHashMap<String, String> joinMessage = new ConcurrentHashMap<String, String>();
    private Utils utils = Utils.getInstance();
    private Messages m = Messages.getInstance();
    public AuthMe plugin;
    private DataSource data;
    public static ConcurrentHashMap<String, Boolean> causeByAuthMe = new ConcurrentHashMap<String, Boolean>();
    private List<String> antibot = new ArrayList<String>();

    public AuthMePlayerListener(AuthMe plugin, DataSource data) {
        this.plugin = plugin;
        this.data = data;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        if (!data.isAuthAvailable(name))
            if (!Settings.isForcedRegistrationEnabled)
                return;

        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("/worldedit cui"))
            return;

        String cmd = msg.split(" ")[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register") || cmd.equalsIgnoreCase("/passpartu") || cmd.equalsIgnoreCase("/l") || cmd.equalsIgnoreCase("/reg") || cmd.equalsIgnoreCase("/email") || cmd.equalsIgnoreCase("/captcha"))
            return;
        if (Settings.useEssentialsMotd && cmd.equalsIgnoreCase("/motd"))
            return;
        if (Settings.allowCommands.contains(cmd))
            return;

        event.setMessage("/notloggedin");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerNormalChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
                return;
            } else {
                m.send(player, "reg_msg");
                return;
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHighChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
                return;
            } else {
                m.send(player, "reg_msg");
                return;
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
                return;
            } else {
                m.send(player, "reg_msg");
                return;
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHighestChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
                return;
            } else {
                m.send(player, "reg_msg");
                return;
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEarlyChat(final AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
                return;
            } else {
                m.send(player, "reg_msg");
                return;
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLowChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        final Player player = event.getPlayer();
        final String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        String cmd = event.getMessage().split(" ")[0];

        if (data.isAuthAvailable(name)) {
            m.send(player, "login_msg");
        } else {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
            if (Settings.emailRegistration) {
                m.send(player, "reg_email_msg");
            } else {
                m.send(player, "reg_msg");
            }
        }

        if (!Settings.isChatAllowed && !(Settings.allowCommands.contains(cmd))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (plugin.getCitizensCommunicator().isNPC(player, plugin) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!Settings.isForcedRegistrationEnabled) {
            if (!data.isAuthAvailable(name))
                return;
        }

        if (!Settings.isMovementAllowed) {
            event.setTo(event.getFrom());
            return;
        }

        if (Settings.getMovementRadius == 0) {
            return;
        }

        int radius = Settings.getMovementRadius;
        Location spawn = plugin.getSpawnLocation(player);

        if (spawn != null && spawn.getWorld() != null)
            if (!event.getPlayer().getWorld().equals(spawn.getWorld())) {
                event.getPlayer().teleport(spawn);
                return;
            }
        if ((spawn.distance(player.getLocation()) > radius) && spawn.getWorld() != null) {
            event.getPlayer().teleport(spawn);
            return;
        }
    }

    private void checkAntiBotMod(final Player player) {
        if (plugin.delayedAntiBot || plugin.antibotMod)
            return;
        if (plugin.authmePermissible(player, "authme.bypassantibot"))
            return;
        if (antibot.size() > Settings.antiBotSensibility) {
            plugin.switchAntiBotMod(true);
            for (String s : m.send("antibot_auto_enabled"))
                Bukkit.broadcastMessage(s);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    if (plugin.antibotMod) {
                        plugin.switchAntiBotMod(false);
                        antibot.clear();
                        for (String s : m.send("antibot_auto_disabled"))
                            Bukkit.broadcastMessage(s.replace("%m", "" + Settings.antiBotDuration));
                    }
                }
            }, Settings.antiBotDuration * 1200);
            return;
        }
        antibot.add(player.getName().toLowerCase());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                antibot.remove(player.getName().toLowerCase());
            }
        }, 300);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        if (player == null)
            return;
        final String name = player.getName().toLowerCase();

        if (plugin.getCitizensCommunicator().isNPC(player, plugin) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (Settings.enablePasspartu && !Settings.countriesBlacklist.isEmpty()) {
            String code = plugin.getCountryCode(event.getAddress().getHostAddress());
            if (((code == null) || (Settings.countriesBlacklist.contains(code) && !API.isRegistered(name))) && !plugin.authmePermissible(player, "authme.bypassantibot")) {
                event.setKickMessage(m.send("country_banned")[0]);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }
        if (Settings.enableProtection && !Settings.countries.isEmpty()) {
            String code = plugin.getCountryCode(event.getAddress().getHostAddress());
            if (((code == null) || (!Settings.countries.contains(code) && !API.isRegistered(name))) && !plugin.authmePermissible(player, "authme.bypassantibot")) {
                event.setKickMessage(m.send("country_banned")[0]);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }

        if (Settings.isKickNonRegisteredEnabled) {
            if (!data.isAuthAvailable(name)) {
                event.setKickMessage(m.send("reg_only")[0]);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }

        if (player.isOnline() && Settings.isForceSingleSessionEnabled) {
            event.setKickMessage(m.send("same_nick")[0]);
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }

        if (data.isAuthAvailable(name) && LimboCache.getInstance().hasLimboPlayer(name))
            if (Settings.isSessionsEnabled)
                if (PlayerCache.getInstance().isAuthenticated(name))
                    if (!Settings.sessionExpireOnIpChange)
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                            @Override
                            public void run() {
                                LimboCache.getInstance().deleteLimboPlayer(name);
                            }
                        });

        // Check if forceSingleSession is set to true, so kick player that has
        // joined with same nick of online player
        if (player.isOnline() && Settings.isForceSingleSessionEnabled) {
            event.setKickMessage(m.send("same_nick")[0]);
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                @Override
                public void run() {
                    LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(player.getName().toLowerCase());
                    if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                        Utils.getInstance().addNormal(player, limbo.getGroup());
                        LimboCache.getInstance().deleteLimboPlayer(player.getName().toLowerCase());
                    }
                }

            });
            return;
        }

        int min = Settings.getMinNickLength;
        int max = Settings.getMaxNickLength;
        String regex = Settings.getNickRegex;

        if (name.length() > max || name.length() < min) {

            event.setKickMessage(m.send("name_len")[0]);
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }
        try {
            if (!player.getName().matches(regex) || name.equals("Player")) {
                try {
                    event.setKickMessage(m.send("regex")[0].replace("REG_EX", regex));
                    event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                } catch (Exception exc) {
                    event.setKickMessage("allowed char : " + regex);
                    event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                }
                return;
            }
        } catch (PatternSyntaxException pse) {
            if (regex == null || regex.isEmpty()) {
                event.setKickMessage("Your nickname do not match");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
            try {
                event.setKickMessage(m.send("regex")[0].replace("REG_EX", regex));
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            } catch (Exception exc) {
                event.setKickMessage("allowed char : " + regex);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            }
            return;
        }

        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            checkAntiBotMod(player);
            if (Settings.bungee) {
                final ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                try {
                    out.writeUTF("IP");
                } catch (IOException e) {
                }
                player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            }
            return;
        }
        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL)
            return;
        if (player.isBanned())
            return;
        if (!plugin.authmePermissible(player, "authme.vip")) {
            event.setKickMessage(m.send("kick_fullserver")[0]);
            event.setResult(PlayerLoginEvent.Result.KICK_FULL);
            return;
        }

        int playersOnline = 0;
        try {
            if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class)
                playersOnline = ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0])).size();
            else playersOnline = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0])).length;
        } catch (NoSuchMethodException ex) {
        } // can never happen
        catch (InvocationTargetException ex) {
        } // can also never happen
        catch (IllegalAccessException ex) {
        } // can still never happen
        if (playersOnline > plugin.getServer().getMaxPlayers()) {
            event.allow();
            return;
        } else {
            final Player pl = plugin.generateKickPlayer(plugin.getServer().getOnlinePlayers());
            if (pl != null) {
                pl.kickPlayer(m.send("kick_forvip")[0]);
                event.allow();
                return;
            } else {
                ConsoleLogger.info("The player " + player.getName() + " wants to join, but the server is full");
                event.setKickMessage(m.send("kick_fullserver")[0]);
                event.setResult(PlayerLoginEvent.Result.KICK_FULL);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        plugin.management.performJoin(player);

        // Remove the join message while the player isn't logging in
        if (Settings.enableProtection || Settings.delayJoinMessage) {
            joinMessage.put(name, event.getJoinMessage());
            event.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        plugin.management.performQuit(player);

        if (data.getAuth(name) != null && !PlayerCache.getInstance().isAuthenticated(name) && Settings.enableProtection)
            event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        if ((!Settings.isForceSingleSessionEnabled) && (event.getReason().contains(m.getString("same_nick")))) {
            event.setCancelled(true);
            return;
        }

        plugin.management.performQuit(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR)
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;
        Player player = (Player) event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
        player.closeInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || event.getWhoClicked() == null)
            return;
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setResult(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (plugin.getCitizensCommunicator().isNPC(player, plugin) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
        }

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled() || event.getPlayer() == null || event == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }
        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer() == null || event == null) {
            return;
        }

        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player))
            return;

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        if (!data.isAuthAvailable(name))
            if (!Settings.isForcedRegistrationEnabled)
                return;

        Location spawn = plugin.getSpawnLocation(player);
        if (Settings.isSaveQuitLocationEnabled && data.isAuthAvailable(name)) {
            final PlayerAuth auth = new PlayerAuth(name, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getWorld().getName());
            try {
                data.updateQuitLoc(auth);
            } catch (NullPointerException npe) {
            }
        }
        if (spawn != null && spawn.getWorld() != null)
            event.setRespawnLocation(spawn);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer() == null || event == null)
            return;
        if (!Settings.isForceSurvivalModeEnabled)
            return;

        Player player = event.getPlayer();

        if (plugin.authmePermissible(player, "authme.bypassforcesurvival"))
            return;

        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player))
            return;

        if (plugin.getCitizensCommunicator().isNPC(player, plugin))
            return;

        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        if (!data.isAuthAvailable(name))
            if (!Settings.isForcedRegistrationEnabled)
                return;

        if (causeByAuthMe.containsKey(name) && causeByAuthMe.get(name))
            return;
        event.setCancelled(true);
    }
}
