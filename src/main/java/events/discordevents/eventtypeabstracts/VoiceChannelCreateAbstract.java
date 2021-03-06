package events.discordevents.eventtypeabstracts;

import java.util.ArrayList;
import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;

public abstract class VoiceChannelCreateAbstract extends DiscordEventAbstract {

    public abstract boolean onVoiceChannelCreate(VoiceChannelCreateEvent event) throws Throwable;

    public static void onVoiceChannelCreateStatic(VoiceChannelCreateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((VoiceChannelCreateAbstract) listener).onVoiceChannelCreate(event)
        );
    }

}
