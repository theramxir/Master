package Commands.ManagementCategory;

import CommandListeners.CommandProperties;
import CommandListeners.OnNavigationListener;
import CommandSupporters.Command;
import CommandSupporters.CommandContainer;
import CommandSupporters.CommandManager;
import CommandSupporters.NavigationHelper;
import Constants.Category;
import Constants.LogStatus;
import Constants.Permission;
import Constants.Response;
import Core.CustomObservableList;
import Core.EmbedFactory;
import Core.ListGen;
import Core.Mention.MentionTools;
import Core.TextManager;
import MySQL.Modules.AutoRoles.AutoRolesBean;
import MySQL.Modules.AutoRoles.DBAutoRoles;
import MySQL.Modules.CommandManagement.CommandManagementBean;
import MySQL.Modules.CommandManagement.DBCommandManagement;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@CommandProperties(
        trigger = "commandmanagement",
        userPermissions = Permission.ADMINISTRATOR,
        emoji = "\uD83D\uDEA6",
        thumbnail = "http://icons.iconarchive.com/icons/elegantthemes/beautiful-flat/128/traffic-icon.png",
        executable = true,
        aliases = {"cmanagement", "cm", "commandmanagements"}
)
public class CommandManagementCommand extends Command implements OnNavigationListener {

    final static Logger LOGGER = LoggerFactory.getLogger(CommandManagementCommand.class);

    private CommandManagementBean commandManagementBean;
    private String category;

    @Override
    protected boolean onMessageReceived(MessageCreateEvent event, String followedString) throws Throwable {
        commandManagementBean = DBCommandManagement.getInstance().getBean(event.getServer().get().getId());
        return true;
    }

    @Override
    public Response controllerMessage(MessageCreateEvent event, String inputString, int state) { return null; }

    @Override
    public boolean controllerReaction(SingleReactionEvent event, int i, int state) throws Throwable {
        switch (state) {
            case 0:
                if (i == -1) {
                    deleteNavigationMessage();
                    return false;
                } else if (i < Category.LIST.length) {
                    category = Category.LIST[i];
                    setState(1);
                    return true;
                }
                return false;

            case 1:
                switch (i) {
                    case -1:
                        setState(0);
                        return true;

                    case 0:
                        turnOnAllCategoryCommands();
                        commandManagementBean.getSwitchedOffElements().remove(category);
                        setLog(LogStatus.SUCCESS, getString("categoryset_all", true, TextManager.getString(getLocale(), TextManager.COMMANDS, category)));
                        return true;

                    case 1:
                        setState(2);
                        return true;

                    case 2:
                        turnOnAllCategoryCommands();
                        commandManagementBean.getSwitchedOffElements().add(category);
                        setLog(LogStatus.SUCCESS, getString("categoryset_all", false, TextManager.getString(getLocale(), TextManager.COMMANDS, category)));
                        return true;
                }
                return false;

            case 2:
                List<Command> commandList = CommandContainer.getInstance().getCommandList().stream()
                        .map(clazz -> {
                            try {
                                return CommandManager.createCommandByClass(clazz, getLocale(), getPrefix());
                            } catch (IllegalAccessException | InstantiationException e) {
                                LOGGER.error("Could not create command", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(command -> command.getCategory().equals(category))
                        .collect(Collectors.toList());

                if (i == -1) {
                    setState(1);
                    return true;
                } else if (i < commandList.size()) {
                    Command command = commandList.get(i);
                    if (commandManagementBean.commandIsTurnedOn(command)) {
                        commandManagementBean.getSwitchedOffElements().add(command.getTrigger());
                        setLog(LogStatus.SUCCESS, getString("commandset", false, command.getTrigger()));
                    } else {
                        if (commandManagementBean.getSwitchedOffElements().contains(command.getCategory())) {
                            commandManagementBean.getSwitchedOffElements().remove(command.getCategory());
                            commandList.stream()
                                    .filter(c -> !c.equals(command))
                                    .forEach(c -> commandManagementBean.getSwitchedOffElements().add(c.getTrigger()));
                        } else {
                            commandManagementBean.getSwitchedOffElements().remove(command.getTrigger());
                        }
                        setLog(LogStatus.SUCCESS, getString("commandset", true, command.getTrigger()));
                    }
                    return true;
                }
                return false;
        }

        return false;
    }

    private void turnOnAllCategoryCommands() {
        commandManagementBean.getSwitchedOffElements().removeIf(element -> {
            try {
                Class<? extends Command> clazz = CommandContainer.getInstance().getCommands().get(element);
                if (clazz == null) return false;
                return CommandManager.createCommandByClass(clazz, getLocale(), getPrefix()).getCategory().equals(category);
            } catch (IllegalAccessException | InstantiationException e) {
                LOGGER.error("Error when creating command", e);
                return false;
            }
        });
    }

    private int getCategoryStatus(String category) throws InstantiationException, IllegalAccessException {
        boolean hasOn = false, hasOff = false;

        if (!commandManagementBean.getSwitchedOffElements().contains(category)) {
            for (Class<? extends Command> clazz : CommandContainer.getInstance().getCommandList()) {
                Command command = CommandManager.createCommandByClass(clazz, getLocale(), getPrefix());
                if (command.getCategory().equals(category)) {
                    if (!hasOn && commandManagementBean.commandIsTurnedOn(command)) hasOn = true;
                    else if (!hasOff && !commandManagementBean.commandIsTurnedOn(command)) hasOff = true;
                }
            }
        }

        return hasOn ? (hasOff ? 1 : 2) : 0;
    }

    @Override
    public EmbedBuilder draw(DiscordApi api, int state) throws Throwable {
        switch (state) {
            case 0:
                String[] options = Arrays.stream(Category.LIST)
                        .map(id -> {
                            String name = TextManager.getString(getLocale(), TextManager.COMMANDS, id);
                            try {
                                return getString("category", getCategoryStatus(id), name);
                            } catch (InstantiationException | IllegalAccessException e) {
                                LOGGER.error("Error while creating command", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(String[]::new);
                setOptions(options);
                return EmbedFactory.getCommandEmbedStandard(this, getString("state0_description"));

            case 1:
                setOptions(getString("state1_options").split("\n"));
                String categoryName = TextManager.getString(getLocale(), TextManager.COMMANDS, category);
                return EmbedFactory.getCommandEmbedStandard(this, getString("state1_description", getCategoryStatus(category), categoryName));

            case 2:
                options = CommandContainer.getInstance().getCommandList().stream()
                        .map(clazz -> {
                            try {
                                return CommandManager.createCommandByClass(clazz, getLocale(), getPrefix());
                            } catch (IllegalAccessException | InstantiationException e) {
                                LOGGER.error("Could not create command", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(command -> command.getCategory().equals(category))
                        .map(command -> getString("command", commandManagementBean.commandIsTurnedOn(command), command.getTrigger(), TextManager.getString(getLocale(), TextManager.COMMANDS, command.getTrigger() + "_title")))
                        .toArray(String[]::new);
                setOptions(options);
                return EmbedFactory.getCommandEmbedStandard(this, getString("state2_description"), getString("state2_title"));
        }
        return null;
    }

    @Override
    public void onNavigationTimeOut(Message message) {}

    @Override
    public int getMaxReactionNumber() {
        return 12;
    }

}