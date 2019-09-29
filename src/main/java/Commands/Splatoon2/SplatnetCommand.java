package Commands.Splatoon2;

import CommandListeners.CommandProperties;
import CommandListeners.onRecievedListener;
import CommandListeners.onTrackerRequestListener;
import CommandSupporters.Command;
import Constants.Permission;
import General.*;
import General.Internet.URLDataContainer;
import General.Tracker.TrackerData;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;

@CommandProperties(
    trigger = "splatnet",
    botPermissions = Permission.USE_EXTERNAL_EMOJIS_IN_TEXT_CHANNEL,
    withLoadingBar = true,
    emoji = "\uD83D\uDED2",
    thumbnail = "https://vignette.wikia.nocookie.net/splatoon/images/1/12/InklingUsingSplatNet.jpg/revision/latest?cb=20160116221000&path-prefix=de",
    executable = true
)
public class SplatnetCommand extends Command implements onRecievedListener, onTrackerRequestListener {
    
    private Instant trackingTime;

    public SplatnetCommand() {
        super();
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        event.getChannel().sendMessage(getEmbed(event.getApi())).get();
        return true;
    }

    private EmbedBuilder getEmbed(DiscordApi api) throws Throwable {
        int datesShown = 2;
        String language = getLocale().getLanguage().split("_")[0].toLowerCase();

        String[] urls = new String[]{
                "https://splatoon2.ink/data/merchandises.json",
                "https://splatoon2.ink/data/locale/" + language + ".json"
        };

        JSONArray netData = new JSONObject(URLDataContainer.getInstance().getData(urls[0])).getJSONArray("merchandises");
        JSONObject languageData = new JSONObject(URLDataContainer.getInstance().getData(urls[1]));;

        //Sorgt dafür, dass die aktuellen Daten genommen werden.
        if (netData.length() == 6) {
            for (int i = 0; i < netData.length(); i++) {
                JSONObject data = netData.getJSONObject(i);
                Instant endTime = new Date(data.getLong("end_time") * 1000L).toInstant();
                if (endTime.isBefore(Instant.now())) {
                    Thread.sleep(5000);
                    return getEmbed(api);
                }
            }
        }

        EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this)
                .setTimestampToNow();

        Instant trackingTime = null;
        for(int i=0; i < netData.length(); i++) {
            JSONObject data = netData.getJSONObject(i);
            Instant endTime = new Date(data.getLong("end_time") * 1000L).toInstant();

            if (trackingTime == null) trackingTime = endTime;
            if (endTime.isBefore(trackingTime)) trackingTime = endTime;

            String gearName = languageData.getJSONObject("gear").getJSONObject(data.getString("kind")).getJSONObject(data.getJSONObject("gear").getString("id")).getString("name");
            String fieldTitle = Shortcuts.getCustomEmojiByID(api, 437258157136543744L).getMentionTag() + " __**" + gearName + "**__";
            int price = data.getInt("price");

            String mainAbility = languageData.getJSONObject("skills").getJSONObject(data.getJSONObject("skill").getString("id")).getString("name");
            int slots = data.getJSONObject("gear").getInt("rarity") + 1;
            String brand = languageData.getJSONObject("brands").getJSONObject(data.getJSONObject("gear").getJSONObject("brand").getString("id")).getString("name");
            //String effect = languageData.getJSONObject("skills").getJSONObject(data.getJSONObject("gear").getJSONObject("brand").getJSONObject("frequent_skill").getString("id")).getString("name");
            String effect = getString("nothing");
            if (data.getJSONObject("gear").getJSONObject("brand").has("frequent_skill")) effect = languageData.getJSONObject("skills").getJSONObject(data.getJSONObject("gear").getJSONObject("brand").getJSONObject("frequent_skill").getString("id")).getString("name");

            String fieldContent = getString("template", Shortcuts.getCustomEmojiByID(api, 437239777834827786L).getMentionTag(), String.valueOf(price), Tools.getInstantString(getLocale(), endTime, true), Tools.getRemainingTimeString(getLocale(), endTime, Instant.now(), true), mainAbility, String.valueOf(slots), brand, effect);
            eb.addField(fieldTitle, fieldContent, true);
        }

        this.trackingTime = trackingTime;

        URLDataContainer.getInstance().setInstantForURL(trackingTime, urls);
        return eb;
    }

    @Override
    public TrackerData onTrackerRequest(TrackerData trackerData) throws Throwable {
        if (trackerData.getMessageDelete() != null) trackerData.getMessageDelete().delete();
        Message message = trackerData.getChannel().sendMessage(getEmbed(trackerData.getChannel().getApi())).get();
        trackerData.setMessageDelete(message);
        trackerData.setInstant(trackingTime);
        return trackerData;
    }

    @Override
    public boolean trackerUsesKey() {
        return false;
    }

    @Override
    public boolean needsPrefix() {
        return false;
    }
}