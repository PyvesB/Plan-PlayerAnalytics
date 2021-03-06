package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.tasks.TPSCountTimer;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

public class SpongeTPSCountTimer extends TPSCountTimer<PlanSponge> {

    private long lastCheckNano;

    public SpongeTPSCountTimer(PlanSponge plugin) {
        super(plugin);
        lastCheckNano = -1;
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        long diff = nanoTime - lastCheckNano;

        lastCheckNano = nanoTime;

        if (diff > nanoTime) { // First run's diff = nanoTime + 1, no calc possible.
            Log.debug("First run of TPSCountTimer Task.");
            return;
        }

        history.add(calculateTPS(now));
    }

    /**
     * Calculates the TPS
     *
     * @param now  The time right now
     * @return the TPS
     */
    private TPS calculateTPS(long now) {
        double averageCPUUsage = getCPUUsage();

        long usedMemory = getUsedMemory();

        double tps = Sponge.getGame().getServer().getTicksPerSecond();
        int playersOnline = ServerInfo.getServerProperties().getOnlinePlayers();
        latestPlayersOnline = playersOnline;
        int loadedChunks = -1; // getLoadedChunks();
        int entityCount = getEntityCount();

        return TPSBuilder.get()
                .date(now)
                .tps(tps)
                .playersOnline(playersOnline)
                .usedCPU(averageCPUUsage)
                .usedMemory(usedMemory)
                .entities(entityCount)
                .chunksLoaded(loadedChunks)
                .toTPS();
    }

    /**
     * Gets the amount of loaded chunks
     *
     * @return amount of loaded chunks
     */
    private int getLoadedChunks() {
        // DISABLED
        int loaded = 0;
        for (World world : Sponge.getGame().getServer().getWorlds()) {
            loaded += world.getLoadedChunks().spliterator().estimateSize();
        }
        return loaded;
    }

    /**
     * Gets the amount of entities on the server
     *
     * @return amount of entities
     */
    private int getEntityCount() {
        return Sponge.getGame().getServer().getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
    }
}
