package com.djrapitops.plan.system.info.server.properties;

import com.djrapitops.plan.system.settings.Settings;
import net.md_5.bungee.api.ProxyServer;

/**
 * ServerProperties for Bungee.
 * <p>
 * Supports RedisBungee for Players online getting.
 *
 * @author Rsl1122
 */
public class BungeeServerProperties extends ServerProperties {

    public BungeeServerProperties(ProxyServer server) {
        super(
                server.getServers().toString(),
                "BungeeCord",
                -1,
                server.getVersion(),
                server.getVersion(),
                Settings.BUNGEE_IP::toString,
                server.getConfig().getPlayerLimit(),
                RedisCheck.isClassAvailable() ? new RedisPlayersOnlineSupplier() : server::getOnlineCount
        );
    }
}