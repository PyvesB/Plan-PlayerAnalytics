/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Vault Economy.
 *
 * @author Rsl1122
 */
public class VaultPermData extends PluginData {

    private final Permission permSys;

    public VaultPermData(Permission permSys) {
        super(ContainerSize.THIRD, "Permissions (" + permSys.getName() + ")");
        super.setIconColor("cyan");
        super.setPluginIcon("asl-interpreting");
        this.permSys = permSys;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String name = DataCache.getInstance().getName(uuid);
        if (name == null) {
            return inspectContainer;
        }
        OfflinePlayer p = new FakeOfflinePlayer(uuid, name);
        inspectContainer.addValue(getWithIcon("Permission Group", "bookmark-o", "cyan"), permSys.getPrimaryGroup(null, p));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        List<FakeOfflinePlayer> offlinePlayers = Optional.ofNullable(analysisData)
                .flatMap(c -> c.getValue(AnalysisKeys.PLAYERS_MUTATOR))
                .map(mutator -> mutator.all().stream().map(FakeOfflinePlayer::new)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        Map<UUID, String> groups = new HashMap<>();
        for (FakeOfflinePlayer p : offlinePlayers) {
            String group = StringUtils.capitalize(permSys.getPrimaryGroup(null, p));
            groups.put(p.getUniqueId(), group);
        }
        analysisContainer.addPlayerTableValues(getWithIcon("Balance", "money"), groups);

        return analysisContainer;
    }
}