package commands.runnables.externalcategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import commands.Command;
import commands.listeners.CommandProperties;
import commands.listeners.OnTrackerRequestListener;
import constants.TrackerResult;
import core.EmbedFactory;
import core.TextManager;
import core.utils.EmbedUtil;
import core.utils.InternetUtil;
import core.utils.StringUtil;
import modules.PostBundle;
import modules.reddit.RedditDownloader;
import modules.reddit.RedditPost;
import mysql.modules.tracker.TrackerBeanSlot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@CommandProperties(
    trigger = "reddit",
    withLoadingBar = true,
    emoji = "\uD83E\uDD16",
    executableWithoutArgs = false
)
public class RedditCommand extends Command implements OnTrackerRequestListener {

    public RedditCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(GuildMessageReceivedEvent event, String args) throws ExecutionException, InterruptedException {
        args = args.trim();
        if (args.startsWith("r/")) args = args.substring(2);

        if (args.length() == 0) {
            event.getChannel().sendMessage(EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args")).build()).queue();
            return false;
        } else {
            RedditPost post;
            post = RedditDownloader.getPost(getLocale(), args);

            if (post != null) {
                if (post.isNsfw() && !event.getChannel().isNSFW()) {
                    event.getChannel().sendMessage(EmbedFactory.getNSFWBlockEmbed(getLocale()).build()).queue();
                    return false;
                }

                EmbedBuilder eb = getEmbed(post);
                EmbedUtil.addTrackerNoteLog(getLocale(), event.getMember(), eb, getPrefix(), getTrigger());
                event.getChannel().sendMessage(eb.build()).queue();
                return true;
            } else {
                EmbedBuilder eb = EmbedFactory.getEmbedError(this)
                        .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                        .setDescription(TextManager.getNoResultsString(getLocale(), args));
                event.getChannel().sendMessage(eb.build()).queue();
                return false;
            }
        }
    }

    private EmbedBuilder getEmbed(RedditPost post) {
        EmbedBuilder eb = EmbedFactory.getEmbedDefault(this, post.getDescription())
                .setTitle(post.getTitle())
                .setAuthor(post.getAuthor(), "https://www.reddit.com/user/" + post.getAuthor(), "")
                .setTimestamp(post.getInstant());

        if (InternetUtil.stringHasURL(post.getThumbnail(), true))
            eb.setThumbnail(post.getThumbnail());
        if (InternetUtil.stringHasURL(post.getUrl(), true))
            eb.setTitle(post.getTitle(), post.getUrl());
        if (InternetUtil.stringHasURL(post.getImage(), true))
            eb.setImage(post.getImage());

        String flairText = "";
        String flair = post.getFlair();
        if (flair != null && !("" + flair).equals("null") && !("" + flair).equals("") && !("" + flair).equals(" "))
            flairText = flair + " | ";

        String nsfwString = "";
        if (post.isNsfw()) {
            nsfwString = " " + getString("nsfw");
        }

        EmbedUtil.setFooter(eb, this, getString("footer", flairText, StringUtil.numToString(post.getScore()), StringUtil.numToString(post.getComments()), post.getDomain()) + nsfwString);

        return eb;
    }

    @Override
    public TrackerResult onTrackerRequest(TrackerBeanSlot slot) throws Throwable {
        if (slot.getCommandKey().isEmpty()) {
            EmbedBuilder eb = EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args"));
            EmbedUtil.addTrackerRemoveLog(eb, getLocale());
            slot.getTextChannel().get().sendMessage(eb.build()).complete();
            return TrackerResult.STOP_AND_DELETE;
        } else {
            slot.setNextRequest(Instant.now().plus(10, ChronoUnit.MINUTES));
            PostBundle<RedditPost> postBundle = RedditDownloader.getPostTracker(getLocale(), slot.getCommandKey(), slot.getArgs().orElse(null));
            TextChannel channel = slot.getTextChannel().get();
            boolean containsOnlyNsfw = true;

            if (postBundle != null) {
                for(int i = 0; i < Math.min(5, postBundle.getPosts().size()); i++) {
                    RedditPost post = postBundle.getPosts().get(i);
                    if (!post.isNsfw() || channel.isNSFW()) {
                        channel.sendMessage(getEmbed(post).build()).complete();
                        containsOnlyNsfw = false;
                        if (slot.getArgs().isEmpty())
                            break;
                    }
                }

                if (containsOnlyNsfw && slot.getArgs().isEmpty()) {
                    EmbedBuilder eb = EmbedFactory.getNSFWBlockEmbed(getLocale());
                    EmbedUtil.addTrackerRemoveLog(eb, getLocale());
                    channel.sendMessage(eb.build()).complete();
                    return TrackerResult.STOP_AND_DELETE;
                }

                slot.setArgs(postBundle.getNewestPost());
                return TrackerResult.CONTINUE_AND_SAVE;
            } else {
                if (slot.getArgs().isEmpty()) {
                    EmbedBuilder eb = EmbedFactory.getEmbedError(this)
                            .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                            .setDescription(TextManager.getNoResultsString(getLocale(), slot.getCommandKey()));
                    EmbedUtil.addTrackerRemoveLog(eb, getLocale());
                    channel.sendMessage(eb.build()).complete();
                    return TrackerResult.STOP_AND_DELETE;
                } else {
                    return TrackerResult.CONTINUE;
                }
            }
        }
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

}
