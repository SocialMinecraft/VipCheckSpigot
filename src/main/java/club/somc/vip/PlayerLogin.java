package club.somc.vip;

import club.somc.protos.vip.GetRequest;
import club.somc.protos.vip.GetResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import io.nats.client.Connection;
import io.nats.client.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.DemotionResult;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.util.Date;

public class PlayerLogin implements Listener {

    Connection nc;

    public PlayerLogin(Connection nc) {
        this.nc = nc;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {


        // So I could have used bungee plugin with the following...
        //LuckPermsProvider.get()

        LuckPerms lp;
        try {
            lp = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            Bukkit.getLogger().info("LuckPerms is not found.");
            return;
        }

        User user = lp.getPlayerAdapter(Player.class).getUser(event.getPlayer());
        Bukkit.getLogger().info(event.getPlayer().getName() + " primary group is " + user.getPrimaryGroup());


        // Send a request to check the player's vip status.
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

        // stop if they are a mod...
        if (user.getPrimaryGroup().equals("mod")) {
            return;
        }

        // do they have a membership that is not expired?
        if (res.hasMembership() && new Date((long)res.getMembership().getExpire()*1000).after(new Date())) {
            // set their vip to true.
            if (!user.getPrimaryGroup().equals("vip")) {
                Track track = lp.getTrackManager().getTrack("user");
                PromotionResult re = track.promote(user, lp.getContextManager().getStaticContext());
                Bukkit.getLogger().info(re.toString());
                lp.getUserManager().saveUser(user);
                Bukkit.getLogger().info(event.getPlayer().getName() + " primary group changed to " + user.getPrimaryGroup());
            }
            return;
        }

        // Set their vip to false
        if (!user.getPrimaryGroup().equals("default")) {
            Track track = lp.getTrackManager().getTrack("user");
            DemotionResult re = track.demote(user, lp.getContextManager().getStaticContext());
            Bukkit.getLogger().info(re.toString());
            lp.getUserManager().saveUser(user);
            Bukkit.getLogger().info(event.getPlayer().getName() + " primary group changed to " + user.getPrimaryGroup());

        }


    }
}
