package commands.slashadapters.adapters

import commands.runnables.informationcategory.CommandUsagesCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData

@Slash(command = CommandUsagesCommand::class)
class CommandUsagesAdapter : SlashAdapter() {

    public override fun addOptions(commandData: CommandData): CommandData {
        return commandData
            .addOption(OptionType.INTEGER, "page", "Which page to view", false)
    }

    override fun process(event: SlashCommandEvent): SlashMeta {
        return SlashMeta(CommandUsagesCommand::class.java, collectArgs(event))
    }

}