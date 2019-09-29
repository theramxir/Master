package Commands.Interactions;
import CommandListeners.CommandProperties;
import CommandListeners.onRecievedListener;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

@CommandProperties(
        trigger = "kill",
        emoji = "☠️",
        executable = false
)
public class KillCommand extends InteractionCommand implements onRecievedListener {
    private static ArrayList<Integer> picked = new ArrayList<>();

    public KillCommand() {
        super( "https://media1.tenor.com/images/28c19622e8d7362ccc140bb24e4089ec/tenor.gif?itemid=9363668",
                "https://media1.tenor.com/images/2c945adbbc31699861f411f86ce8058f/tenor.gif?itemid=5459053",
                "https://media1.tenor.com/images/eb7fc71c616347e556ab2b4c813700d1/tenor.gif?itemid=5840101",
                "https://media1.tenor.com/images/db1136b19969ca0809daffc3d93fc848/tenor.gif?itemid=9983954",
                "https://media1.tenor.com/images/cff010b188084e1faed2905c0f1634c2/tenor.gif?itemid=10161883",
                "https://media1.tenor.com/images/25f853a32137e24b11cd13bc2142f63a/tenor.gif?itemid=7172028",
                "https://media1.tenor.com/images/405efa8099e014cfeb8178c5b3801322/tenor.gif?itemid=13843226",
                "https://media1.tenor.com/images/364e3dcc7ce079c06e79d110eb85f4cf/tenor.gif?itemid=4885036",
                "https://media1.tenor.com/images/e1a8a560a7d532442f6d4e00d6f131a4/tenor.gif?itemid=14424096",
                "https://media1.tenor.com/images/1b189d99ba29bc7b4aa8f24f4827c12e/tenor.gif?itemid=13726342",
                "https://media1.tenor.com/images/4776a4baa6eb9813ecfde2a16071d96e/tenor.gif?itemid=4775517"
        );
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        return onInteractionRecieved(event, followedString, picked);
    }
}