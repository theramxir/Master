package mysql.modules.suggestions;

import java.util.HashMap;
import java.util.Optional;
import core.CustomObservableMap;
import modules.suggestions.SuggestionMessage;
import mysql.BeanWithGuild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SuggestionsBean extends BeanWithGuild {

    private final CustomObservableMap<Long, SuggestionMessage> messages;
    private boolean active;
    private Long channelId;

    public SuggestionsBean(long serverId, boolean active, Long channelId, @NonNull HashMap<Long, SuggestionMessage> messages) {
        super(serverId);
        this.messages = new CustomObservableMap<>(messages);
        this.active = active;
        this.channelId = channelId;
    }


    /* Getters */

    public boolean isActive() {
        return active;
    }

    public CustomObservableMap<Long, SuggestionMessage> getSuggestionMessages() {
        return messages;
    }

    public Optional<Long> getTextChannelId() {
        return Optional.ofNullable(channelId);
    }

    public Optional<TextChannel> getTextChannel() {
        return getGuild().map(guild -> guild.getTextChannelById(channelId != null ? channelId : 0L));
    }


    /* Setters */

    public void toggleActive() {
        this.active = !this.active;
        setChanged();
        notifyObservers();
    }

    public void setChannelId(Long channelId) {
        if (this.channelId == null || !this.channelId.equals(channelId)) {
            this.channelId = channelId;
            setChanged();
            notifyObservers();
        }
    }

}
