/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPI;

import java.util.Map;

/**
 * @author Rsl1122
 */
public class MaxPlayersWebAPI extends WebAPI {
    @Override
    public Response onResponse(IPlan plugin, Map<String, String> variables) {
        return new JsonResponse(plugin.getVariable().getMaxPlayers());
    }
}