package Commands.External;

import CommandListeners.CommandProperties;
import CommandListeners.onRecievedListener;
import CommandSupporters.Command;
import Constants.Language;
import General.*;
import General.Internet.URLDataContainer;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONObject;

import java.time.Instant;

@CommandProperties(
    trigger = "dadjoke",
    withLoadingBar = true,
    emoji = "\uD83D\uDE44",
    thumbnail = "http://icons.iconarchive.com/icons/webalys/kameleon.pics/128/Man-6-icon.png",
    executable = true
)
public class DadJokeCommand extends Command implements onRecievedListener {

    public DadJokeCommand() {
        super();
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        String joke;
        if (Tools.getLanguage(getLocale()) == Language.DE) {
            joke = URLDataContainer.getInstance().getData("https://api.opossum.media/streamacademy/commands/fun/flachwitz.php", Instant.now());
        } else {
            joke = new JSONObject(URLDataContainer.getInstance().getData("https://icanhazdadjoke.com/slack", Instant.now())).getJSONArray("attachments").getJSONObject(0).getString("text");
        }

        event.getChannel().sendMessage(EmbedFactory.getCommandEmbedStandard(this, joke)).get();
        return true;
    }
}