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

        RegisteredServiceProvider<LuckPerms> lpp = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (lpp != null) {
           getLogger().severe("LuckPerms is not found.");
        }
        LuckPerms luckPerms = lpp.getProvider();
        // So I could have used bungee plugin with the following...
        //LuckPermsProvider.get()

        try {
            this.nc = Nats.connect(getConfig().getString("natsUrl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new PlayerLogin(luckPerms, nc), this);
    }

    @Override
    public void onDisable() {

    }
}
