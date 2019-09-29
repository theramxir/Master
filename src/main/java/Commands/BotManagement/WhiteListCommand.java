package Commands.BotManagement;

import CommandListeners.CommandProperties;
import CommandListeners.onNavigationListener;
import CommandSupporters.Command;
import Constants.LogStatus;
import Constants.Permission;
import Constants.Response;
import General.*;
import General.Mention.MentionFinder;
import MySQL.DBServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;

import java.util.ArrayList;

@CommandProperties(
    trigger = "whitelist",
    userPermissions = Permission.MANAGE_SERVER,
    emoji = "✅",
    thumbnail = "http://icons.iconarchive.com/icons/graphicloads/100-flat-2/128/check-1-icon.png",
    executable = true
)
public class WhiteListCommand extends Command implements onNavigationListener {

    private ArrayList<ServerTextChannel> channels;

    public WhiteListCommand() {
        super();
    }

    @Override
    public Response controllerMessage(MessageCreateEvent event, String inputString, int state, boolean firstTime) throws Throwable {
        if (firstTime) {
            channels = DBServer.getWhiteListedChannels(event.getServer().get());
            return Response.TRUE;
        }

        if (state == 1) {
            ArrayList<ServerTextChannel> channelList = MentionFinder.getTextChannels(event.getMessage(), inputString).getList();
            if (channelList.size() == 0) {
                setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "no_results_description", inputString));
                return Response.FALSE;
            } else {
                channels = channelList;
                setLog(LogStatus.SUCCESS, getString("channelset"));
                setState(0);
                DBServer.saveWhiteListedChannels(event.getServer().get(), channels);
                return Response.TRUE;
            }
        }

        return null;
    }

    @Override
    public boolean controllerReaction(SingleReactionEvent event, int i, int state) throws Throwable {
        switch (state) {
            case 0:
                switch (i) {
                    case -1:
                        deleteNavigationMessage();
                        return false;

                    case 0:
                        setState(1);
                        return true;

                    case 1:
                        if (channels.size() > 0) {
                            setLog(LogStatus.SUCCESS, getString("channelset"));
                            channels = new ArrayList<>();
                            DBServer.saveWhiteListedChannels(event.getServer().get(), channels);
                            return true;
                        } else {
                            setLog(LogStatus.FAILURE, getString("nochannel"));
                            return true;
                        }
                }
                return false;

            case 1:
                if (i == -1) {
                    setState(0);
                    return true;
                }
        }
        return false;
    }

    @Override
    public EmbedBuilder draw(DiscordApi api, int state) throws Throwable {
        String everyChannel = getString("all");
        switch (state) {
            case 0:
                setOptions(getString("state0_options").split("\n"));
                return EmbedFactory.getCommandEmbedStandard(this, getString("state0_description"))
                       .addField(getString("state0_mchannel"), ListGen.getChannelList(everyChannel, channels), true);

            case 1:
                return EmbedFactory.getCommandEmbedStandard(this, getString("state1_description"), getString("state1_title"));
        }
        return null;
    }

    @Override
    public void onNavigationTimeOut(Message message) throws Throwable {}

    @Override
    public int getMaxReactionNumber() {
        return 2;
    }
}