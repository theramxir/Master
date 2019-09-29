package Commands.ServerManagement;

import CommandListeners.*;
import CommandSupporters.Command;
import Constants.LetterEmojis;
import Constants.Permission;
import General.*;
import General.Survey.VoteInfo;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@CommandProperties(
    trigger = "vote",
    botPermissions = Permission.REMOVE_REACTIONS_OF_OTHERS_IN_TEXT_CHANNEL | Permission.READ_MESSAGE_HISTORY_OF_TEXT_CHANNEL,
    thumbnail = "http://icons.iconarchive.com/icons/graphicloads/colorful-long-shadow/128/Hand-thumbs-up-like-2-icon.png",
    emoji = "\uD83D\uDDF3️️",
    executable = false
)
public class VoteCommand extends Command implements onRecievedListener, onReactionAddStatic, onReactionRemoveStatic {

    public VoteCommand() {
        super();
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        followedString = Tools.cutSpaces(followedString.replace("\n", ""));
        if (followedString.startsWith("|")) followedString = followedString.substring(1);
        String args[] = followedString.split("\\|");
        if (args.length >= 3 && args.length <= 10) {
            String topic = Tools.cutSpaces(args[0]);

            if (topic.length() == 0) {
                event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this, getString("no_topic")));
                return false;
            } else {
                String[] answers = new String[args.length - 1];
                int[] values = new int[answers.length];
                for (int i = 0; i < answers.length; i++) {
                    answers[i] = args[i + 1];
                    values[i] = 0;
                }

                VoteInfo voteInfo = new VoteInfo(topic, answers, values);
                EmbedBuilder eb = getEmbed(voteInfo);
                Message message = event.getServerTextChannel().get().sendMessage(eb).get();
                for (int i = 0; i < answers.length; i++) {
                    message.addReaction(LetterEmojis.LETTERS[i]);
                }
                return true;
            }
        } else {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this, getString("wrong_args")));
            return false;
        }
    }

    public EmbedBuilder getEmbed(VoteInfo voteInfo) throws IOException {
        StringBuilder answerText = new StringBuilder();
        StringBuilder resultsText = new StringBuilder();

        for(int i=0; i < voteInfo.getSize(); i++) {
            answerText.append(LetterEmojis.LETTERS[i]).append(" | ").append(voteInfo.getChoices(i)).append("\n");
            resultsText.append(LetterEmojis.LETTERS[i]).append(" | ").append(Tools.getBar((double) voteInfo.getValue(i) / voteInfo.getTotalVotes(),12)).append(" 【 ").append(voteInfo.getValue(i)).append(" • ").append((int)(voteInfo.getPercantage(i)*100)).append("% 】").append("\n");
        }

        EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this, "", getString("title") + Tools.getEmptyCharacter())
                .addField(getString("topic"), voteInfo.getTopic(),false)
                .addField(getString("choices"), answerText.toString(),false)
                .addField(getString("results") + " (" + voteInfo.getTotalVotes() + " " + getString("votes", voteInfo.getTotalVotes() != 1) + ")",resultsText.toString(),false);

        return eb;
    }

    private VoteInfo getValuesFromMessage(Message message) {
        Embed embed = message.getEmbeds().get(0);
        List<EmbedField> field = embed.getFields();

        String topic = field.get(0).getValue();

        String choiceString = field.get(1).getValue();
        String[] choices = new String[choiceString.split("\n").length];
        for(int i=0; i < choices.length; i++) {
            String choiceLine = choiceString.split("\n")[i];
            choices[i] = choiceLine.split("\\|")[1];
        }

        int[] values = new int[choices.length];
        for(int i=0; i < values.length; i++) {
            boolean found = false;
            for (Reaction reaction: message.getReactions()) {
                if (reaction.getEmoji().getMentionTag().equalsIgnoreCase(LetterEmojis.LETTERS[i])) {
                    if (reaction.containsYou()) values[i] = reaction.getCount() - 1;
                    else values[i] = reaction.getCount();
                    found = true;
                    break;
                }
            }
            if (!found) values[i] = 0;
        }

        return new VoteInfo(topic, choices, values);
    }

    private void removeEmoteIfNotSupported(Message message, ReactionAddEvent reactionAddEvent) {
        String emoteString = reactionAddEvent.getEmoji().getMentionTag();
        String choiceString = message.getEmbeds().get(0).getFields().get(1).getValue();

        for (int i = 0; i < choiceString.split("\n").length; i++) {
            if (emoteString.equalsIgnoreCase(LetterEmojis.LETTERS[i])) {
                return;
            }
        }

        reactionAddEvent.removeReaction();
    }

    @Override
    public void onReactionAddStatic(Message message, ReactionAddEvent event) throws Throwable {
        removeEmoteIfNotSupported(message, event);

        //Doppelte Reaktionen entfernen
        boolean block = false;
        boolean userFound = false;

        User user = event.getUser();
        for (Reaction reaction : message.getReactions()) {
            if (!reaction.getEmoji().getMentionTag().equalsIgnoreCase(event.getEmoji().getMentionTag())) {
                try {
                    List<User> userList = reaction.getUsers().get();
                    if (userList.contains(user)) {
                        reaction.removeUser(user);
                        block = true;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                userFound = reaction.getUsers().get().contains(user);
            }
        }

        //VoteInfo aktualisierung
        VoteInfo voteInfo = getValuesFromMessage(message);

        //VoteInfo ausführen
        if (!block && userFound) message.edit(getEmbed(voteInfo)).get();
    }

    @Override
    public void onReactionRemoveStatic(Message message, ReactionRemoveEvent event) throws Throwable {
        VoteInfo voteInfo = getValuesFromMessage(message);
        message.edit(getEmbed(voteInfo)).get();
    }

    @Override
    public boolean requiresLocale() {
        return true;
    }

    @Override
    public String getTitleStartIndicator() {
        return getEmoji();
    }
}