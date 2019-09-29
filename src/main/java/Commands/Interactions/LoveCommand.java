package Commands.Interactions;
import CommandListeners.CommandProperties;
import CommandListeners.onRecievedListener;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

@CommandProperties(
        trigger = "love",
        emoji = "\u2764\uFE0F",
        executable = false
)
public class LoveCommand extends InteractionCommand implements onRecievedListener {
    private static ArrayList<Integer> picked = new ArrayList<>();

    public LoveCommand() {
        super( "https://media1.tenor.com/images/0a3203ced13826a92230cc61214318da/tenor.gif?itemid=5243518",
                "https://media1.tenor.com/images/052a6cf478262740bee6ea7a4a2087bb/tenor.gif?itemid=7482475",
                "https://media1.tenor.com/images/c251343262ab40b922b1b05d007f2b94/tenor.gif?itemid=5180037",
                "https://media1.tenor.com/images/dd10eb337856d14a8640828f99dd7a2f/tenor.gif?itemid=12479111",
                "https://media1.tenor.com/images/110dbddfd3d662479c214cacb754995d/tenor.gif?itemid=10932413",
                "https://media1.tenor.com/images/6db54c4d6dad5f1f2863d878cfb2d8df/tenor.gif?itemid=7324587",
                "https://media1.tenor.com/images/1ea084fcde776de33ebcbff90eda1d01/tenor.gif?itemid=14210736",
                "https://media1.tenor.com/images/59371e16bf2c92a158a0bf84e1e70bb6/tenor.gif?itemid=12479110",
                "https://media1.tenor.com/images/06f88667b86a701b1613bbf5fb9205e9/tenor.gif?itemid=13417199",
                "https://media1.tenor.com/images/b92289789869848bc6c6e36224d4597f/tenor.gif?itemid=11478579",
                "https://media1.tenor.com/images/93f5876e82ae575a6c4b4613d57f6e29/tenor.gif?itemid=13665536",
                "https://media1.tenor.com/images/42922e87b3ec288b11f59ba7f3cc6393/tenor.gif?itemid=5634630",
                "https://media1.tenor.com/images/fdafbad47d6a69cb5d3a90a8b9dff86f/tenor.gif?itemid=4936338",
                "https://media1.tenor.com/images/b830955c1b6d8093dd8cc369208ed3ce/tenor.gif?itemid=10268970",
                "https://media1.tenor.com/images/5bb32afaf0b50552fa71a869bdd6c135/tenor.gif?itemid=4553174",
                "https://media1.tenor.com/images/f68c955294bbf31734786b08e538d7e7/tenor.gif?itemid=7366180",
                "https://media1.tenor.com/images/62a43e567137edec0d5231d5ec4b814b/tenor.gif?itemid=8955295",
                "https://media1.tenor.com/images/44850486ab6ee663cb28121810297eaa/tenor.gif?itemid=11722517",
                "https://media1.tenor.com/images/850577b758b548fcaac70fb8abd55286/tenor.gif?itemid=4849603",
                "https://media1.tenor.com/images/433a5625a2aae2793b23a3cc38260afb/tenor.gif?itemid=12802747",
                "https://media1.tenor.com/images/79c461726e53ee8f9a5a36521f69d737/tenor.gif?itemid=13221416",
                "https://media1.tenor.com/images/37ac3414835b0cce1304b6a4b5fcaddd/tenor.gif?itemid=12669038",
                "https://media1.tenor.com/images/cc805107341e281102a2280f08b582e0/tenor.gif?itemid=13925386",
                "https://media1.tenor.com/images/640e8004816e37a169113b7464f748f7/tenor.gif?itemid=5452015",
                "https://media1.tenor.com/images/b011c3ec014e3fc90b071e477e9f9054/tenor.gif?itemid=5601384",
                "https://media1.tenor.com/images/4699b77bd5e4e057750e39f4ea7caca1/tenor.gif?itemid=11514021",
                "https://media1.tenor.com/images/277be5cb4e40f88a6bc3076e765d64ac/tenor.gif?itemid=5865253",
                "https://media1.tenor.com/images/e9f3db84734ab5e60cd122f857fa2ec3/tenor.gif?itemid=6052595",
                "https://media1.tenor.com/images/e858678426357728038c277598871d6d/tenor.gif?itemid=9903014"

        );
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        return onInteractionRecieved(event, followedString, picked);
    }
}