package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.command.utils.DataFormatUtils;
import com.djrapitops.planlite.command.utils.DataUtils;
import com.djrapitops.planlite.command.utils.Analysis;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnalyzeCommand extends SubCommand {

    private PlanLite plugin;
    private HashMap<UUID, HashMap<String, DataPoint>> playerData;
    private HashMap<String, DataPoint> analyzedPlayerdata;
    private Date refreshDate;

    public AnalyzeCommand(PlanLite plugin) {
        super("analyze", "planlite.analyze", "Analyze data of all players /plan analyze [-refresh]", CommandType.CONSOLE);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-refresh")) {
                if (sender.hasPermission("planlite.analyze.refresh") || !(sender instanceof Player)) {
                    refreshAnalysisData(sender);
                }
            }
        }
        if (this.playerData == null || this.refreshDate == null || this.analyzedPlayerdata == null || DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()).contains("m")) {
            refreshAnalysisData(sender);
        }
        
        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Analysis results, refreshed " 
                + DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + " ago:" + textColor + "] --");
        
        List<String[]> dataList = DataFormatUtils.turnDataHashMapToSortedListOfArrays(analyzedPlayerdata);
        
        sender.sendMessage("" + textColor + "Averages for " + this.playerData.size() + " player(s)");
        for (String[] dataString : dataList) {
            sender.sendMessage("" + operatorColor + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + textColor + dataString[1]);
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }

    private void refreshAnalysisData(CommandSender sender) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        sender.sendMessage(textColor + "[" + operatorColor + "Plan" + textColor + "] "
                + "Refreshing playerData, this might take a while..");
        this.playerData = DataUtils.getTotalData(DataUtils.getMatchingDisplaynames(true));
        this.refreshDate = new Date();
        this.analyzedPlayerdata = Analysis.analyze(this.playerData);
        sender.sendMessage(textColor + "[" + operatorColor + "Plan" + textColor + "] "
                + "Refreshed, took "+DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()));
    }
}