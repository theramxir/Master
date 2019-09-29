package Commands.Interactions;

import CommandListeners.CommandProperties;
import CommandSupporters.Command;
import General.*;
import General.Mention.Mention;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

public class InteractionCommand extends Command {

    private String[] gifs;

    public InteractionCommand(String... gifs) {
        this.gifs = gifs;
    }

    public boolean onInteractionRecieved(MessageCreateEvent event, String followedString, ArrayList<Integer> picked) throws Throwable {
        Message message = event.getMessage();
        Mention mention = Tools.getMentionedString(getLocale(),message,followedString);
        if (mention == null) {
            message.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this,
                    TextManager.getString(getLocale(),TextManager.GENERAL,"no_mentions"))).get();
            return false;
        } else if (mention.getString().equals( "**"+event.getMessage().getAuthor().getDisplayName()+"**")) {
            message.getChannel().sendMessage(
                    EmbedFactory.getCommandEmbedStandard(this,
                        TextManager.getString(getLocale(),TextManager.GENERAL,"alone"))
                    .setImage("https://media1.giphy.com/media/od5H3PmEG5EVq/giphy.gif?cid=790b76115ce968cf4a364a6845982172&rid=giphy.gif")).get();
            return false;
        }

        String gifUrl = gifs[Tools.pickFullRandom(picked, gifs.length)];
        EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this,getString("template", mention.isMultiple(), mention.getString(), "**"+event.getMessage().getAuthor().getDisplayName()+"**"))
                .setImage(gifUrl);

        message.getChannel().sendMessage(eb).get();
        removeMessageForwarder();

        return true;
    }

}