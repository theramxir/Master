package commands.slashadapters.adapters

import commands.runnables.informationcategory.NewCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData

@Slash(command = NewCommand::class)
class NewAdapter : SlashAdapter() {

    public override fun addOptions(commandData: CommandData): CommandData {
        return commandData
            .addOption(OptionType.INTEGER, "number", "The number of recent versions to view")
            .addOption(OptionType.STRING, "version", "Which bot version to view")
    }

    override fun process(event: SlashCommandEvent): SlashMeta {
        return SlashMeta(NewCommand::class.java, collectArgs(event))
    }

}