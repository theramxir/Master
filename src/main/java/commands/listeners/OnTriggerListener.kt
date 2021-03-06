package commands.listeners

import commands.Command
import commands.CommandContainer
import commands.CommandEvent
import commands.runnables.utilitycategory.TriggerDeleteCommand
import core.MemberCacheController
import core.PermissionCheckRuntime
import core.Program
import core.interactionresponse.InteractionResponse
import core.interactionresponse.SlashCommandResponse
import core.schedule.MainScheduler
import core.utils.ExceptionUtil
import mysql.modules.commandusages.DBCommandUsages
import mysql.modules.guild.DBGuild
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean

interface OnTriggerListener {

    @Throws(Throwable::class)
    fun onTrigger(event: CommandEvent, args: String): Boolean

    @Throws(Throwable::class)
    fun processTrigger(event: CommandEvent, args: String, freshCommand: Boolean): Boolean {
        val command = this as Command
        if (freshCommand && event.isSlashCommandEvent()) {
            val interactionResponse: InteractionResponse = SlashCommandResponse(event.slashCommandEvent!!.hook)
            command.interactionResponse = interactionResponse
        }
        val isProcessing = AtomicBoolean(true)
        command.setAtomicAssets(event.channel, event.member)
        command.commandEvent = event
        if (Program.publicVersion()) {
            DBCommandUsages.getInstance().retrieve(command.trigger).increase()
        }
        if (event.isGuildMessageReceivedEvent()) {
            command.addLoadingReaction(event.guildMessageReceivedEvent!!.message, isProcessing)
            processTriggerDelete(event.guildMessageReceivedEvent)
        }
        addKillTimer(isProcessing)
        try {
            if (command.commandProperties.requiresFullMemberCache) {
                MemberCacheController.getInstance().loadMembersFull(event.guild).get()
            }
            return onTrigger(event, args)
        } catch (e: Throwable) {
            ExceptionUtil.handleCommandException(e, command)
            return false
        } finally {
            isProcessing.set(false)
        }
    }

    private fun addKillTimer(isProcessing: AtomicBoolean) {
        val command = this as Command
        val commandThread = Thread.currentThread()
        MainScheduler.schedule(command.commandProperties.maxCalculationTimeSec.toLong(), ChronoUnit.SECONDS, "command_timeout") {
            if (!command.commandProperties.turnOffTimeout) {
                CommandContainer.addCommandTerminationStatus(command, commandThread, isProcessing.get())
            }
        }
    }

    private fun processTriggerDelete(event: GuildMessageReceivedEvent) {
        val guildBean = DBGuild.getInstance().retrieve(event.guild.idLong)
        if (guildBean.isCommandAuthorMessageRemoveEffectively &&
            PermissionCheckRuntime.botHasPermission(guildBean.locale, TriggerDeleteCommand::class.java, event.channel, Permission.MESSAGE_MANAGE)
        ) {
            event.message.delete().queue()
        }
    }

}