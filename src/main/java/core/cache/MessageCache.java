package core.cache;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class MessageCache {

    private static final MessageCache ourInstance = new MessageCache();

    public static MessageCache getInstance() {
        return ourInstance;
    }

    private MessageCache() {
    }

    private final Cache<Long, Message> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    private final Cache<Long, Boolean> cacheMessageBlock = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    public synchronized CompletableFuture<Message> retrieveMessage(TextChannel channel, long messageId) {
        if (cacheMessageBlock.asMap().containsKey(messageId)) {
            return CompletableFuture.failedFuture(new NoSuchElementException("No such message"));
        }

        if (cache.asMap().containsKey(messageId)) {
            return CompletableFuture.completedFuture(cache.getIfPresent(messageId));
        } else {
            return channel.retrieveMessageById(messageId).submit()
                    .thenApply(message -> {
                        cache.put(messageId, message);
                        return message;
                    });
        }
    }

    public synchronized void put(Message message) {
        cache.put(message.getIdLong(), message);
    }

    public synchronized void block(long messageId) {
        if (!cacheMessageBlock.asMap().containsKey(messageId)) {
            cacheMessageBlock.put(messageId, true);
        }
    }

    public synchronized void delete(long messageId) {
        cache.invalidate(messageId);
    }

}