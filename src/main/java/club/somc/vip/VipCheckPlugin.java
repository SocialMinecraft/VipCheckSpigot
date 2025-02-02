package club.somc.vip;

import io.nats.client.Connection;
import io.nats.client.Nats;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class VipCheckPlugin extends JavaPlugin {

    Connection nc;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();

        try {
            this.nc = Nats.connect(getConfig().getString("natsUrl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new PlayerLogin(nc, getLogger()), this);
    }

    @Override
    public void onDisable() {

    }
}
