package commands;

import commands.listeners.CommandProperties;
import commands.listeners.OnReactionListener;
import commands.listeners.OnStaticReactionAddListener;
import commands.listeners.OnTriggerListener;
import constants.Emojis;
import core.Bot;
import core.TextManager;
import core.atomicassets.AtomicMember;
import core.atomicassets.AtomicTextChannel;
import core.schedule.MainScheduler;
import core.utils.BotPermissionUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Command implements OnTriggerListener {

    private final long id = System.nanoTime();
    private final String category;
    private final String prefix;
    private Locale locale;
    private final CommandProperties commandProperties;
    private final JSONObject attachments = new JSONObject();
    private boolean loadingReactionSet = false;
    private final ArrayList<Runnable> completedListeners = new ArrayList<>();
    private AtomicTextChannel atomicTextChannel;
    private AtomicMember atomicMember;

    public Command(Locale locale, String prefix) {
        this.locale = locale;
        this.prefix = prefix;
        commandProperties = this.getClass().getAnnotation(CommandProperties.class);
        category = CategoryCalculator.getCategoryByCommand(this.getClass());
    }

    public void addLoadingReaction(Message message, AtomicBoolean isProcessing) {
        if (getCommandProperties().withLoadingBar()) {
            addLoadingReactionInstantly(message, isProcessing);
        } else {
            MainScheduler.getInstance().schedule(3, ChronoUnit.SECONDS, getTrigger() + "_idle", () -> {
                if (isProcessing.get()) {
                    addLoadingReactionInstantly(message, isProcessing);
                }
            });
        }
    }

    public void addLoadingReactionInstantly(Message message, AtomicBoolean isProcessing) {
        TextChannel channel = message.getTextChannel();
        if (!loadingReactionSet && BotPermissionUtil.canRead(channel, Permission.MESSAGE_ADD_REACTION)) {
            loadingReactionSet = true;

            String reaction;
            if (BotPermissionUtil.can(channel, Permission.MESSAGE_EXT_EMOJI)) {
                reaction = Emojis.LOADING;
            } else {
                reaction = "⏳";
            }

            message.addReaction(reaction).queue();
            MainScheduler.getInstance().poll(100, getTrigger() + "_loading", () -> {
                if (isProcessing.get()) {
                    return true;
                } else {
                    message.removeReaction(reaction).queue();
                    loadingReactionSet = false;
                    return false;
                }
            });
        }
    }

    public String getString(String key, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, args);
        if (prefix != null) text = text.replace("%PREFIX", prefix);
        return text;
    }

    public String getString(String key, int option, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, option, args);
        if (prefix != null) text = text.replace("%PREFIX", prefix);
        return text;
    }

    public String getString(String key, boolean secondOption, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, secondOption, args);
        if (prefix != null) text = text.replace("%PREFIX", prefix);
        return text;
    }

    public CommandLanguage getCommandLanguage() {
        String title = getString("title");
        String descLong = getString("helptext");
        String usage = getString("usage");
        String examples = getString("examples");
        return new CommandLanguage(title, descLong, usage, examples);
    }

    public Permission[] getUserGuildPermissions() {
        Permission[] permissions = commandProperties.userGuildPermissions();
        return processUserPermissions(permissions);
    }

    public Permission[] getUserChannelPermissions() {
        Permission[] permissions = commandProperties.userChannelPermissions();
        return processUserPermissions(permissions);
    }

    private Permission[] processUserPermissions(Permission[] permissions) {
        if (Arrays.stream(permissions).anyMatch(permission -> permission == Permission.ADMINISTRATOR)) {
            return new Permission[]{ Permission.ADMINISTRATOR };
        }

        //TODO: Does that work?
        if ((this instanceof OnReactionListener || this instanceof NavigationCommand || this instanceof OnStaticReactionAddListener) &&
                Arrays.stream(permissions).noneMatch(permission -> permission == Permission.MESSAGE_HISTORY)
        ) {
            permissions = Arrays.copyOf(permissions, permissions.length + 1);
            permissions[permissions.length - 1] = Permission.MESSAGE_HISTORY;
        }

        return permissions;
    }

    public boolean isModCommand() {
        return Arrays.stream(commandProperties.userGuildPermissions()).anyMatch(p -> p != Permission.MESSAGE_HISTORY) ||
                Arrays.stream(commandProperties.userChannelPermissions()).anyMatch(p -> p != Permission.MESSAGE_HISTORY);
    }

    public Permission[] getBotPermissions() {
        Permission[] permissions = commandProperties.botPermissions();
        if (Arrays.stream(permissions).anyMatch(permission -> permission == Permission.ADMINISTRATOR)) {
            return new Permission[]{ Permission.ADMINISTRATOR };
        }

        //TODO: Does that work?
        if ((this instanceof OnReactionListener || this instanceof NavigationCommand || this instanceof OnStaticReactionAddListener) &&
                Arrays.stream(permissions).noneMatch(permission -> permission == Permission.MESSAGE_HISTORY)
        ) {
            permissions = Arrays.copyOf(permissions, permissions.length + 2);
            permissions[permissions.length - 2] = Permission.MESSAGE_HISTORY;
            permissions[permissions.length - 1] = Permission.MESSAGE_ADD_REACTION;
        }

        return permissions;
    }

    public boolean canRunOnGuild(long guildId, long userId) {
        long[] allowedServerIds = commandProperties.exlusiveServers();
        long[] allowedUserIds = commandProperties.exlusiveUsers();

        return ((allowedServerIds.length == 0) || Arrays.stream(allowedServerIds).anyMatch(checkServerId -> checkServerId == guildId)) &&
                ((allowedUserIds.length == 0) || Arrays.stream(allowedUserIds).anyMatch(checkUserId -> checkUserId == userId)) &&
                (!commandProperties.onlyPublicVersion() || Bot.isPublicVersion());
    }

    public Optional<LocalDate> getReleaseDate() {
        int[] releaseDateArray = commandProperties.releaseDate();
        return Optional.ofNullable(releaseDateArray.length == 3 ? LocalDate.of(releaseDateArray[0], releaseDateArray[1], releaseDateArray[2]) : null);
    }

    public void addCompletedListener(Runnable runnable) {
        completedListeners.add(runnable);
    }

    public List<Runnable> getCompletedListeners() {
        return Collections.unmodifiableList(completedListeners);
    }

    public long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getTrigger() {
        return getCommandProperties().trigger();
    }

    public JSONObject getAttachments() {
        return attachments;
    }

    public void setTextChannelAndMember(TextChannel textChannel, Member member) {
        atomicTextChannel = new AtomicTextChannel(textChannel);
        atomicMember = new AtomicMember(member);
    }

    public Optional<TextChannel> getTextChannel() {
        return Optional.ofNullable(atomicTextChannel)
                .flatMap(AtomicTextChannel::get);
    }

    public Optional<Member> getMember() {
        return Optional.ofNullable(atomicMember)
                .flatMap(AtomicMember::get);
    }

    public CommandProperties getCommandProperties() {
        return commandProperties;
    }

    public static String getCategory(Class<? extends Command> clazz) {
        return CategoryCalculator.getCategoryByCommand(clazz);
    }

    public static CommandProperties getCommandProperties(Class<? extends Command> clazz) {
        return clazz.getAnnotation(CommandProperties.class);
    }

}
