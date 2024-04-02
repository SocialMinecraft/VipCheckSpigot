package club.somc.vip;

import io.nats.client.Connection;
import io.nats.client.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;

public class PlayerLogin implements Listener {

    LuckPerms lp;
    Connection nc;

    public PlayerLogin(LuckPerms lp, Connection nc) {
        this.lp = lp;
        this.nc = nc;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Send a request to check the player's vip status.

        //event.getPlayer().hasPermission("group.vip");
        //User user = lp.getPlayerAdapter(Player.class).getUser(event.getPlayer());
        //user.setPrimaryGroup("vip/default");


        try {
            Message m = nc.request("vip.status", null, Duration.ofMillis(300));
        } catch (InterruptedException e) {
            Bukkit.getLogger().warning(e.getMessage());
            return;
        }

    }
}
