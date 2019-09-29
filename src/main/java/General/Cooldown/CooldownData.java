package General.Cooldown;

import org.javacord.api.entity.user.User;

import java.time.Instant;

public class CooldownData {
    private User user;
    private Instant endTimer;
    private int value;
    private boolean botIsSending;

    public CooldownData(User user) {
        this.user = user;
        this.value = 1;
        this.botIsSending = false;
        resetTimer();
    }

    public User getUser() {
        return user;
    }

    public boolean canPost() {
        return Instant.now().isAfter(endTimer) || value <= Cooldown.MAX_ALLOWED;
    }

    public void plus() {
        if (Instant.now().isAfter(endTimer)) value = 0;
        value++;
        if (value > Cooldown.MAX_ALLOWED) resetTimer();
    }

    private void resetTimer() {
        endTimer = Instant.now().plusSeconds(Cooldown.COOLDOWN_TIME_IN_SECONDS);
    }

    public void setBotIsSending(boolean botIsSending) {
        this.botIsSending = botIsSending;
    }

    public boolean isBotIsSending() {
        return botIsSending;
    }
}