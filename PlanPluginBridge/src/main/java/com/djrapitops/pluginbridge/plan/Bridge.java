package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.aac.AdvancedAntiCheatHook;
import com.djrapitops.pluginbridge.plan.advancedachievements.AdvancedAchievementsHook;
import com.djrapitops.pluginbridge.plan.advancedban.AdvancedBanHook;
import com.djrapitops.pluginbridge.plan.askyblock.ASkyBlockHook;
import com.djrapitops.pluginbridge.plan.banmanager.BanManagerHook;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;
import com.djrapitops.pluginbridge.plan.discordsrv.DiscordSRVHook;
import com.djrapitops.pluginbridge.plan.essentials.EssentialsHook;
import com.djrapitops.pluginbridge.plan.factions.FactionsHook;
import com.djrapitops.pluginbridge.plan.griefprevention.GriefPreventionHook;
import com.djrapitops.pluginbridge.plan.griefprevention.plus.GriefPreventionPlusHook;
import com.djrapitops.pluginbridge.plan.jobs.JobsHook;
import com.djrapitops.pluginbridge.plan.kingdoms.KingdomsHook;
import com.djrapitops.pluginbridge.plan.litebans.LiteBansBukkitHook;
import com.djrapitops.pluginbridge.plan.litebans.LiteBansBungeeHook;
import com.djrapitops.pluginbridge.plan.luckperms.LuckPermsHook;
import com.djrapitops.pluginbridge.plan.mcmmo.McmmoHook;
import com.djrapitops.pluginbridge.plan.nucleus.NucleusHook;
import com.djrapitops.pluginbridge.plan.protocolsupport.ProtocolSupportHook;
import com.djrapitops.pluginbridge.plan.redprotect.RedProtectHook;
import com.djrapitops.pluginbridge.plan.sponge.SpongeEconomyHook;
import com.djrapitops.pluginbridge.plan.superbvote.SuperbVoteHook;
import com.djrapitops.pluginbridge.plan.towny.TownyHook;
import com.djrapitops.pluginbridge.plan.vault.VaultHook;
import com.djrapitops.pluginbridge.plan.viaversion.ViaVersionBukkitHook;
import com.djrapitops.pluginbridge.plan.viaversion.ViaVersionBungeeHook;

/**
 * Manages connection to other plugins.
 *
 * @author Rsl1122
 */
@SuppressWarnings("WeakerAccess")
public class Bridge {

    private Bridge() {
        throw new IllegalStateException("Utility class");
    }

    public static void hook(HookHandler h) {
        Hook[] hooks = getHooks(h);
        hookInto(hooks);
    }

    private static void hookInto(Hook[] hooks) {
        for (Hook hook : hooks) {
            try {
                hook.hook();
            } catch (Exception | NoClassDefFoundError e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog("PluginBridge", e);
                }
            }
        }
    }

    private static Hook[] getHooks(HookHandler h) {
        Hook[] hooks;
        if (Check.isBungeeAvailable()) {
            hooks = getBungeeHooks(h);
        } else if (Check.isBukkitAvailable()) {
            hooks = getBukkitHooks(h);
        } else if (Check.isSpongeAvailable()) {
            hooks = getSpongeHooks(h);
        } else {
            return new Hook[0];
        }
        return hooks;
    }

    private static Hook[] getSpongeHooks(HookHandler h) {
        return new Hook[]{
                new BuyCraftHook(h),
                new LuckPermsHook(h),
                new SpongeEconomyHook(h),
                new NucleusHook(h)
        };
    }

    private static Hook[] getBungeeHooks(HookHandler h) {
        return new Hook[]{
                new AdvancedBanHook(h),
                new BuyCraftHook(h),
                new LiteBansBungeeHook(h),
                new LuckPermsHook(h),
                new ViaVersionBungeeHook(h)
        };
    }

    private static Hook[] getBukkitHooks(HookHandler h) {
        return new Hook[]{
                new AdvancedAntiCheatHook(h),
                new AdvancedAchievementsHook(h),
                new AdvancedBanHook(h),
                new ASkyBlockHook(h),
                new BanManagerHook(h),
                new BuyCraftHook(h),
                new DiscordSRVHook(h),
                new EssentialsHook(h),
                new FactionsHook(h),
                new GriefPreventionHook(h),
                new GriefPreventionPlusHook(h),
                new JobsHook(h),
                new KingdomsHook(h),
                new LiteBansBukkitHook(h),
                new LuckPermsHook(h),
                new McmmoHook(h),
                new SuperbVoteHook(h),
                new ProtocolSupportHook(h),
                // new ReactHook(h),
                new RedProtectHook(h),
                new TownyHook(h),
                new VaultHook(h),
                new ViaVersionBukkitHook(h)//,
                // new PlaceholderAPIHook(h)
        };
    }
}
