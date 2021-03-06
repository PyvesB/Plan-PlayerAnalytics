package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.UpdateCancelRequest;
import com.djrapitops.plan.system.info.request.UpdateRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.update.VersionInfo;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Command that updates all servers in the network
 *
 * @author Rsl1122
 */
public class UpdateCommand extends CommandNode {

    private final Locale locale;

    public UpdateCommand(PlanPlugin plugin) {
        super("update", Permissions.MANAGE.getPermission(), CommandType.ALL);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("[-u]/[cancel]");
        setShortHelp(locale.getString(CmdHelpLang.UPDATE));
        setInDepthHelp(locale.getArray(DeepHelpLang.UPDATE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!VersionCheckSystem.isNewVersionAvailable()) {
            sender.sendMessage("§a" + locale.getString(PluginLang.VERSION_NEWEST));
            return;
        }

        VersionInfo available = VersionCheckSystem.getInstance().getNewVersionAvailable();
        String downloadUrl = available.getDownloadUrl();

        if (!available.isTrusted()) {
            sender.sendMessage(locale.getString(CommandLang.UPDATE_WRONG_URL, "https://github.com/Rsl1122/Plan-PlayerAnalytics/releases/"));
            sender.sendLink(downloadUrl, downloadUrl);
            return;
        }

        if (args.length == 0) {
            String message = locale.getString(CommandLang.UPDATE_CHANGE_LOG, available.getVersion().toString());
            String url = available.getChangeLogUrl();
            if (CommandUtils.isConsole(sender)) {
                sender.sendMessage(message + url);
            } else {
                sender.sendMessage(message);
                sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
            }
            return;
        }

        String firstArgument = args[0];
        RunnableFactory.createNew("Update Command Task", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    if ("-u".equals(firstArgument)) {
                        handleUpdate(sender, args);
                    } else if ("cancel".equals(firstArgument)) {
                        handleCancel(sender);
                    } else {
                        throw new IllegalArgumentException("Unknown argument, use '-u' or 'cancel'");
                    }
                } finally {
                    cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void handleCancel(ISender sender) {
        try {
            cancel(sender, Database.getActive().fetch().getServers());
            sender.sendMessage(locale.getString(CommandLang.UPDATE_CANCEL_SUCCESS));
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase error occurred, cancel could not be performed.");
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void handleUpdate(ISender sender, String[] args) {
        sender.sendMessage(locale.getString(CommandLang.UPDATE_NOTIFY_CANCEL));
        sender.sendMessage(locale.getString(CommandLang.UPDATE_ONLINE_CHECK));
        if (!checkNetworkStatus(sender)) {
            sender.sendMessage(locale.getString(CommandLang.UPDATE_FAIL_NOT_ONLINE));
            // If -force, continue, otherwise return.
            if (args.length < 2 || !"-force".equals(args[1])) {
                return;
            }
        }
        try {
            List<Server> servers = Database.getActive().fetch().getServers();
            update(sender, servers, args);
        } catch (DBOpException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void update(ISender sender, List<Server> servers, String[] args) {
        for (Server server : servers) {
            if (update(sender, server)) {
                sender.sendMessage(locale.getString(CommandLang.UPDATE_SCHEDULED, server.getName()));
            } else {
                if (args.length > 1 && "-force".equals(args[1])) {
                    sender.sendMessage(locale.getString(CommandLang.UPDATE_FAIL_FORCED));
                    continue;
                }
                sender.sendMessage(locale.getString(CommandLang.UPDATE_FAIL_CANCEL));
                cancel(sender, servers);
                sender.sendMessage(locale.getString(CommandLang.UPDATE_CANCELLED));
                break;
            }
        }
    }

    private void cancel(ISender sender, List<Server> servers) {
        for (Server server : servers) {
            cancel(sender, server);
        }
    }

    private void cancel(ISender sender, Server server) {
        try {
            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new UpdateCancelRequest(), server);
        } catch (ForbiddenException | GatewayException | InternalErrorException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Odd Exception: " + e.getClass().getSimpleName());
        } catch (UnauthorizedServerException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Unauthorized. " + server.getName() + " might be using different database.");
        } catch (ConnectionFailException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            String address = server.getWebAddress();
            boolean local = address.contains("localhost")
                    || address.startsWith("https://:") // IP empty = Localhost
                    || address.startsWith("http://:") // IP empty = Localhost
                    || address.contains("127.0.0.1");
            if (!local) {
                sender.sendMessage("§cNon-local address, check that port is open");
            }
        } catch (NotFoundException e) {
            /* Ignored, older version */
        } catch (WebException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Odd Exception:" + e.getClass().getSimpleName());
        }
    }

    private boolean update(ISender sender, Server server) {
        try {
            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new UpdateRequest(), server);
            return true;
        } catch (BadRequestException e) {
            sender.sendMessage("§c" + server.getName() + " has Allow-Update set to false, aborting update.");
            return false;
        } catch (ForbiddenException | GatewayException | InternalErrorException | NoServersException e) {
            sender.sendMessage("§c" + server.getName() + ": Odd Exception: " + e.getClass().getSimpleName());
            return false;
        } catch (UnauthorizedServerException e) {
            sender.sendMessage("§cFail reason: Unauthorized. " + server.getName() + " might be using different database.");
            return false;
        } catch (ConnectionFailException e) {
            sender.sendMessage("§cFail reason: " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            String address = server.getWebAddress();
            boolean local = address.contains("localhost")
                    || address.startsWith("https://:") // IP empty = Localhost
                    || address.startsWith("http://:") // IP empty = Localhost
                    || address.contains("127.0.0.1");
            if (!local) {
                sender.sendMessage("§cNon-local address, check that port is open");
            }
            return false;
        } catch (NotFoundException e) {
            sender.sendMessage("§e" + server.getName() + " is using older version and can not be scheduled for update. " +
                    "You can update it manually, update will proceed.");
            return true;
        } catch (WebException e) {
            sender.sendMessage("§eOdd Exception: " + e.getClass().getSimpleName());
            return false;
        }
    }

    private boolean checkNetworkStatus(ISender sender) {
        try {
            FetchOperations fetch = Database.getActive().fetch();
            Optional<Server> bungeeInformation = fetch.getBungeeInformation();
            if (!bungeeInformation.isPresent()) {
                sender.sendMessage("Bungee address not found in the database, assuming this is not a network.");
                return true;
            }
            Map<UUID, Server> bukkitServers = fetch.getBukkitServers();
            String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
            boolean success = true;
            for (Server server : bukkitServers.values()) {
                if (!ManageConDebugCommand.testServer(sender, accessAddress, server, locale)) {
                    success = false;
                }
            }
            Server bungee = bungeeInformation.get();
            if (!ManageConDebugCommand.testServer(sender, accessAddress, bungee, locale)) {
                success = false;
            }
            return success;
        } catch (DBOpException e) {
            sender.sendMessage("§cDatabase error occurred, update has been cancelled.");
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
    }
}