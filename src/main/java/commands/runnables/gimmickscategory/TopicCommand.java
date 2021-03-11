package commands.runnables.gimmickscategory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.annotation.CheckReturnValue;
import commands.Command;
import commands.listeners.CommandProperties;
import commands.listeners.OnTrackerRequestListener;
import constants.TrackerResult;
import core.*;
import core.utils.EmbedUtil;
import core.utils.StringUtil;
import mysql.modules.tracker.TrackerSlot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

@CommandProperties(
        trigger = "topic",
        emoji = "\uD83E\uDD60",
        executableWithoutArgs = true,
        aliases = { "topics" }
)
public class TopicCommand extends Command implements OnTrackerRequestListener {

    public TopicCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(GuildMessageReceivedEvent event, String args) throws IOException {
        send(event.getChannel()).queue();
        return true;
    }

    @CheckReturnValue
    private MessageAction send(TextChannel channel) throws IOException {
        List<String> topicList = FileManager.readInList(ResourceHandler.getFileResource("data/resources/topics_" + getLocale().getDisplayName() + ".txt"));
        int n = RandomPicker.getInstance().pick(getTrigger(), channel.getGuild().getIdLong(), topicList.size());
        String topic = topicList.get(n);

        return channel.sendMessage(EmbedFactory.getEmbedDefault(this, topic).build());
    }

    @Override
    public TrackerResult onTrackerRequest(TrackerSlot slot) throws Throwable {
        final int MIN_MINUTES = 10;
        final int MAX_MINUTES = 10080;
        String key = slot.getCommandKey();

        long minutes = StringUtil.filterLongFromString(key);
        if (minutes > MAX_MINUTES || minutes < MIN_MINUTES) {
            EmbedBuilder eb = EmbedFactory.getEmbedError(
                    this,
                    TextManager.getString(getLocale(), TextManager.GENERAL, "number", StringUtil.numToString(MIN_MINUTES), StringUtil.numToString(MAX_MINUTES))
            );
            EmbedUtil.addTrackerRemoveLog(eb, getLocale());

            slot.getTextChannel().get().sendMessage(eb.build()).complete();
            return TrackerResult.STOP_AND_DELETE;
        }

        send(slot.getTextChannel().get()).complete();
        slot.setNextRequest(Instant.now().plus(minutes, ChronoUnit.MINUTES));

        return TrackerResult.CONTINUE_AND_SAVE;
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

}