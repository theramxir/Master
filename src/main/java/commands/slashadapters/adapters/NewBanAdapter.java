package commands.slashadapters.adapters;

import commands.runnables.moderationcategory.NewBanCommand;
import commands.slashadapters.Slash;
import commands.slashadapters.SlashAdapter;
import commands.slashadapters.SlashMeta;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Slash(command = NewBanCommand.class)
public class NewBanAdapter extends SlashAdapter {

    public CommandData addOptions(CommandData commandData) {
        return commandData
                .addOption(OptionType.STRING, "time", "The time span of the server joins (e.g. 1h 3m)", true)
                .addOption(OptionType.STRING, "reason", "The reason of this mod action", false);
    }

    @Override
    public SlashMeta process(SlashCommandEvent event) {
        return new SlashMeta(NewBanCommand.class, collectArgs(event));
    }

}
