package club.somc.vip;

import club.somc.protos.vip.GetRequest;
import club.somc.protos.vip.GetResponse;
import com.google.protobuf.InvalidProtocolBufferException;
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
import java.util.Date;

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
        User user = lp.getPlayerAdapter(Player.class).getUser(event.getPlayer());
        Bukkit.getLogger().info(event.getPlayer().getName() + " primary group is " + user.getPrimaryGroup());
        //user.setPrimaryGroup("vip/default");


        GetRequest req = GetRequest.newBuilder()
                .setMinecraftUuid(event.getPlayer().getUniqueId().toString())
                .build();


        Message m;
        try {
            m = nc.request("vip.get", req.toByteArray(), Duration.ofMillis(300));
        } catch (InterruptedException e) {
            Bukkit.getLogger().warning(e.getMessage());
            return;
        }

        GetResponse res;
        try {
             res = GetResponse.parseFrom(m.getData());
        } catch (InvalidProtocolBufferException e) {
            Bukkit.getLogger().warning(e.getMessage());
            return;
        }

        // do they have a membership that is not expired?
        if (res.hasMembership() && new Date(res.getMembership().getExpire()).before(new Date())) {
            // set their vip to true.
            if (!user.getPrimaryGroup().equals("vip")) {
                user.setPrimaryGroup("vip");
                Bukkit.getLogger().info(event.getPlayer().getName() + " primary group changed to " + user.getPrimaryGroup());
            }
            return;
        }

        // Set their vip to false
        if (!user.getPrimaryGroup().equals("default")) {
            user.setPrimaryGroup("default");
            Bukkit.getLogger().info(event.getPlayer().getName() + " primary group changed to " + user.getPrimaryGroup());
        }


    }
}
