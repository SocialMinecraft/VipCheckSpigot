package club.somc.vip;

import club.somc.protos.vip.GetRequest;
import club.somc.protos.vip.GetResponse;
import club.somc.protos.vip.Membership;
import com.google.protobuf.InvalidProtocolBufferException;
import io.nats.client.Connection;
import io.nats.client.Message;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.util.Date;
import java.util.logging.Logger;

public class PlayerLogin implements Listener {

    Connection nc;
    Logger logger;
    LuckPerms lp;

    public PlayerLogin(Connection nc, Logger logger) {

        this.nc = nc;
        this.logger = logger;

        setupLuckPerms();
    }

    private void setupLuckPerms() {
        try {
            lp = LuckPermsProvider.get();
        } catch (NoClassDefFoundError ex) {
            logger.warning("LuckPerms not found");
            // should really crash.
        }
    }


    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {


        Membership membership = getMembership(event.getPlayer());
        if (membership == null) {
            logger.info(event.getPlayer().getName() + " has no membership");
        }

        lp.getUserManager().modifyUser(event.getPlayer().getUniqueId(), user -> {
            InheritanceNode node = InheritanceNode.builder("vip").build();

            if (membership != null && membershipActive(membership)) {
                logger.info(event.getPlayer().getName() + " has active vip");
                user.data().add(node);
            } else {
                logger.info(event.getPlayer().getName() + " does not have active vip");
                user.data().remove(node);
            }
        });
    }

    private boolean membershipActive(Membership membership) {
        return  (new Date((long)membership.getExpire()*1000).after(new Date()));
    }

    private Membership getMembership(Player player) {
        // Send a request to check the player's vip status.
        GetRequest req = GetRequest.newBuilder()
                .setMinecraftUuid(player.getUniqueId().toString())
                .build();


        Message m;
        try {
            m = nc.request("vip.get", req.toByteArray(), Duration.ofMillis(300));
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
            return null;
        }

        GetResponse res;
        try {
            res = GetResponse.parseFrom(m.getData());
            if (res.hasMembership())
                return res.getMembership();
            else
                return null;
        } catch (InvalidProtocolBufferException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }
}
