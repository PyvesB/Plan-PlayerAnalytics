package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.CheckConnectionRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;

import java.util.List;
import java.util.UUID;

/**
 * This manage SubCommand is used to request settings from Bungee so that connection can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageConDebugCommand extends CommandNode {

    private final Locale locale;

    public ManageConDebugCommand(PlanPlugin plugin) {
        super("con", Permissions.MANAGE.getPermission(), CommandType.ALL);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(Check.isBungeeAvailable() ? CmdHelpLang.CON : CmdHelpLang.MANAGE_CON));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_CON));
    }

    public static boolean testServer(ISender sender, String accessAddress, Server server, Locale locale) {
        String address = server.getWebAddress().toLowerCase();
        boolean usingHttps = address.startsWith("https");
        boolean local = address.contains("localhost")
                || address.startsWith("https://:") // IP empty = Localhost
                || address.startsWith("http://:") // IP empty = Localhost
                || address.contains("127.0.0.1");

        try {

            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new CheckConnectionRequest(accessAddress), server);
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, true));
            return true;

        } catch (ForbiddenException | BadRequestException | InternalErrorException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage(locale.getString(ManageLang.CON_EXCEPTION, e.getClass().getSimpleName()));
        } catch (UnauthorizedServerException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, false));
            sender.sendMessage(locale.getString(ManageLang.CON_UNAUTHORIZED));
        } catch (ConnectionFailException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage(locale.getString(ManageLang.CON_GENERIC_FAIL) + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            if (!local) {
                sender.sendMessage(locale.getString(ManageLang.CON_EXTERNAL_URL));
            }
        } catch (GatewayException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, false));
        } catch (NotFoundException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage(locale.getString(ManageLang.CON_OLD_VERSION));
        } catch (WebException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage(locale.getString(ManageLang.CON_EXCEPTION, e.getClass().getSimpleName()));
        }
        return false;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!WebServerSystem.isWebServerEnabled()) {
            sender.sendMessage(locale.getString(CommandLang.CONNECT_WEBSERVER_NOT_ENABLED));
            return;
        }

        Processing.submitNonCritical(() -> testServers(sender));
    }

    private void testServers(ISender sender) {
        List<Server> servers = Database.getActive().fetch().getServers();

        if (servers.isEmpty()) {
            sender.sendMessage(locale.getString(ManageLang.CON_NO_SERVERS));
        }

        String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
        UUID thisServer = ServerInfo.getServerUUID();
        for (Server server : servers) {
            if (thisServer.equals(server.getUuid())) {
                continue;
            }
            testServer(sender, accessAddress, server, locale);
        }
    }

    private static String getMsgFor(String address, boolean usingHttps, boolean local, boolean successTo, boolean successFrom) {
        ColorScheme cs = PlanPlugin.getInstance().getColorScheme();
        String tCol = cs.getTertiaryColor();
        String sCol = cs.getSecondaryColor();
        return tCol + address + sCol + ": "
                + (usingHttps ? "HTTPS" : "HTTP") + " : "
                + (local ? "Local" : "External") + " : "
                + "To:" + (successTo ? "§aOK" : "§cFail") + sCol + " : "
                + "From:" + (successFrom ? "§aOK" : "§cFail");
    }
}
