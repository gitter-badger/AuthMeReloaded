package fr.xephi.authme.process.login;

import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.listener.AuthMePlayerListener;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.security.RandomString;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.task.MessageTask;
import me.muizers.Notifications.Notification;

public class AsyncronousLogin {

    protected Player player;
    protected String name;
    protected String realName;
    protected String password;
    protected boolean forceLogin;
    private AuthMe plugin;
    private DataSource database;
    private static RandomString rdm = new RandomString(Settings.captchaLength);
    private Messages m = Messages.getInstance();

    public AsyncronousLogin(Player player, String password, boolean forceLogin,
            AuthMe plugin, DataSource data) {
        this.player = player;
        this.password = password;
        name = player.getName().toLowerCase();
        realName = player.getName();
        this.forceLogin = forceLogin;
        this.plugin = plugin;
        this.database = data;
    }

    protected String getIP() {
        return plugin.getIP(player);
    }

    protected boolean needsCaptcha() {
        if (Settings.useCaptcha) {
            if (!plugin.captcha.containsKey(name)) {
                plugin.captcha.put(name, 1);
            } else {
                int i = plugin.captcha.get(name) + 1;
                plugin.captcha.remove(name);
                plugin.captcha.put(name, i);
            }
            if (plugin.captcha.containsKey(name) && plugin.captcha.get(name) >= Settings.maxLoginTry) {
                plugin.cap.put(name, rdm.nextString());
                for (String s : m.send("usage_captcha")) {
                    player.sendMessage(s.replace("THE_CAPTCHA", plugin.cap.get(name)).replace("<theCaptcha>", plugin.cap.get(name)));
                }
                return true;
            } else
                if (plugin.captcha.containsKey(name) && plugin.captcha.get(name) >= Settings.maxLoginTry) {
                try {
                    plugin.captcha.remove(name);
                    plugin.cap.remove(name);
                } catch (NullPointerException npe) {
                }
            }
        }
        return false;
    }

    /**
     * Checks the precondition for authentication (like user known) and returns
     * the playerAuth-State
     */
    protected PlayerAuth preAuth() {
        if (PlayerCache.getInstance().isAuthenticated(name)) {
            m.send(player, "logged_in");
            return null;
        }
        if (!database.isAuthAvailable(name)) {
            m.send(player, "user_unknown");
            if (LimboCache.getInstance().hasLimboPlayer(name)) {
                LimboCache.getInstance().getLimboPlayer(name).getMessageTaskId().cancel();
                String[] msg;
                if (Settings.emailRegistration) {
                    msg = m.send("reg_email_msg");
                } else {
                    msg = m.send("reg_msg");
                }
                BukkitTask msgT = Bukkit.getScheduler().runTaskAsynchronously(plugin, new MessageTask(plugin, name, msg, Settings.getWarnMessageInterval));
                LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(msgT);
            }
            return null;
        }
        if (Settings.getMaxLoginPerIp > 0 && !plugin.authmePermissible(player, "authme.allow2accounts") && !getIP().equalsIgnoreCase("127.0.0.1") && !getIP().equalsIgnoreCase("localhost")) {
            if (plugin.isLoggedIp(name, getIP())) {
                m.send(player, "logged_in");
                return null;
            }
        }
        PlayerAuth pAuth = database.getAuth(name);
        if (pAuth == null) {
            m.send(player, "user_unknown");
            return null;
        }
        if (!Settings.getMySQLColumnGroup.isEmpty() && pAuth.getGroupId() == Settings.getNonActivatedGroup) {
            m.send(player, "vb_nonActiv");
            return null;
        }
        return pAuth;
    }

    public void process() {
        PlayerAuth pAuth = preAuth();
        if (pAuth == null || needsCaptcha())
            return;

        String hash = pAuth.getHash();
        String email = pAuth.getEmail();
        boolean passwordVerified = true;
        if (!forceLogin)
            try {
                passwordVerified = PasswordSecurity.comparePasswordWithHash(password, hash, realName);
            } catch (Exception ex) {
                ConsoleLogger.showError(ex.getMessage());
                m.send(player, "error");
                return;
            }
        if (passwordVerified && player.isOnline()) {
            PlayerAuth auth = new PlayerAuth(name, hash, getIP(), new Date().getTime(), email);
            database.updateSession(auth);

            if (Settings.useCaptcha) {
                if (plugin.captcha.containsKey(name)) {
                    plugin.captcha.remove(name);
                }
                if (plugin.cap.containsKey(name)) {
                    plugin.cap.remove(name);
                }
            }

            player.setNoDamageTicks(0);
            m.send(player, "login");

            displayOtherAccounts(auth, player);
            
            if (Settings.recallEmail) {
                if (email == null || email.isEmpty() || email.equalsIgnoreCase("your@email.com"))
                    m.send(player, "add_email");
            }


            if (!Settings.noConsoleSpam)
                ConsoleLogger.info(player.getName() + " logged in!");

            if (plugin.notifications != null) {
                plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " logged in!"));
            }

            // makes player isLoggedin via API
            PlayerCache.getInstance().addPlayer(auth);
            database.setLogged(name);
            plugin.otherAccounts.addPlayer(player.getUniqueId());

            // As the scheduling executes the Task most likely after the current
            // task, we schedule it in the end
            // so that we can be sure, and have not to care if it might be
            // processed in other order.
            ProcessSyncronousPlayerLogin syncronousPlayerLogin = new ProcessSyncronousPlayerLogin(player, plugin, database);
            if (syncronousPlayerLogin.getLimbo() != null) {
                if (syncronousPlayerLogin.getLimbo().getTimeoutTaskId() != null)
                    syncronousPlayerLogin.getLimbo().getTimeoutTaskId().cancel();
                if (syncronousPlayerLogin.getLimbo().getMessageTaskId() != null)
                    syncronousPlayerLogin.getLimbo().getMessageTaskId().cancel();
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, syncronousPlayerLogin);
        } else if (player.isOnline()) {
            if (!Settings.noConsoleSpam)
                ConsoleLogger.info(player.getName() + " used the wrong password");
            if (Settings.isKickOnWrongPasswordEnabled) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        if (AuthMePlayerListener.gameMode != null && AuthMePlayerListener.gameMode.containsKey(name)) {
                            player.setGameMode(AuthMePlayerListener.gameMode.get(name));
                        }
                        player.kickPlayer(m.send("wrong_pwd")[0]);
                    }
                });
            } else {
                m.send(player, "wrong_pwd");
                return;
            }
        } else {
            ConsoleLogger.showError("Player " + name + " wasn't online during login process, aborted... ");
        }
    }

    public void displayOtherAccounts(PlayerAuth auth, Player p) {
        if (!Settings.displayOtherAccounts) {
            return;
        }
        if (auth == null) {
            return;
        }
        List<String> auths = this.database.getAllAuthsByName(auth);
        // List<String> uuidlist =
        // plugin.otherAccounts.getAllPlayersByUUID(player.getUniqueId());
        if (auths.isEmpty() || auths == null) {
            return;
        }
        if (auths.size() == 1) {
            return;
        }
        String message = "[AuthMe] ";
        // String uuidaccounts =
        // "[AuthMe] PlayerNames has %size% links to this UUID : ";
        int i = 0;
        for (String account : auths) {
            i++;
            message = message + account;
            if (i != auths.size()) {
                message = message + ", ";
            } else {
                message = message + ".";
            }
        }
        /*
         * TODO: Active uuid system i = 0; for (String account : uuidlist) {
         * i++; uuidaccounts = uuidaccounts + account; if (i != auths.size()) {
         * uuidaccounts = uuidaccounts + ", "; } else { uuidaccounts =
         * uuidaccounts + "."; } }
         */
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.authmePermissible(player, "authme.seeOtherAccounts")) {
                player.sendMessage("[AuthMe] The player " + auth.getNickname() + " has " + auths.size() + " accounts");
                player.sendMessage(message);
                // player.sendMessage(uuidaccounts.replace("%size%",
                // ""+uuidlist.size()));
            }
        }
    }
}
