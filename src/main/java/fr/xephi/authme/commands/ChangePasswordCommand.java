package fr.xephi.authme.commands;

import java.security.NoSuchAlgorithmException;

import me.muizers.Notifications.Notification;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.Settings;

public class ChangePasswordCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private DataSource database;
    public AuthMe plugin;

    public ChangePasswordCommand(DataSource database, AuthMe plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label,
            String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!plugin.authmePermissible(sender, "authme." + label.toLowerCase())) {
            m.send(sender, "no_perm");
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName().toLowerCase();
        if (!PlayerCache.getInstance().isAuthenticated(name)) {
            m.send(player, "not_logged_in");
            return true;
        }

        if (args.length != 2) {
            m.send(player, "usage_changepassword");
            return true;
        }

        String lowpass = args[1].toLowerCase();
        if ((lowpass.contains("delete") || lowpass.contains("where") || lowpass.contains("insert") || lowpass.contains("modify") || lowpass.contains("from") || lowpass.contains("select") || lowpass.contains(";") || lowpass.contains("null")) || !lowpass.matches(Settings.getPassRegex) || lowpass.equalsIgnoreCase(name)) {
            m.send(player, "password_error");
            return true;
        }
        if (lowpass.length() < Settings.getPasswordMinLen || lowpass.length() > Settings.passwordMaxLength) {
            m.send(player, "pass_len");
            return true;
        }
        if (!Settings.unsafePasswords.isEmpty()) {
            if (Settings.unsafePasswords.contains(lowpass)) {
                m.send(player, "password_error");
                return true;
            }
        }
        try {
            String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, args[1], name);

            if (PasswordSecurity.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash(), player.getName())) {
                PlayerAuth auth = PlayerCache.getInstance().getAuth(name);
                auth.setHash(hashnew);
                if (PasswordSecurity.userSalt.containsKey(name) && PasswordSecurity.userSalt.get(name) != null)
                    auth.setSalt(PasswordSecurity.userSalt.get(name));
                else auth.setSalt("");
                if (!database.updatePassword(auth)) {
                    m.send(player, "error");
                    return true;
                }
                database.updateSalt(auth);
                PlayerCache.getInstance().updatePlayer(auth);
                m.send(player, "pwd_changed");
                ConsoleLogger.info(player.getName() + " changed his password");
                if (plugin.notifications != null) {
                    plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " change his password!"));
                }
            } else {
                m.send(player, "wrong_pwd");
            }
        } catch (NoSuchAlgorithmException ex) {
            ConsoleLogger.showError(ex.getMessage());
            m.send(sender, "error");
        }
        return true;
    }
}
