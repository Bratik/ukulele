package dev.arbjerg.ukulele.command

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.ukulele.audio.Player
import dev.arbjerg.ukulele.audio.PlayerRegistry
import dev.arbjerg.ukulele.features.HelpContext
import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import net.dv8tion.jda.api.Permission
import org.springframework.stereotype.Component

@Component
class PlayNextCommand(
        val players: PlayerRegistry,
        val apm: AudioPlayerManager
) : Command("playnext", "pn") {
    override suspend fun CommandContext.invoke() {
        if (!ensureVoiceChannel()) return
        val identifier = argumentText
        apm.loadItem(identifier, Loader(this, player, identifier))
    }

    fun CommandContext.ensureVoiceChannel(): Boolean {
        val ourVc = guild.selfMember.voiceState?.channel
        val theirVc = invoker.voiceState?.channel

        if (ourVc == null && theirVc == null) {
            reply("You need to be in a voice channel")
            return false
        }

        if (ourVc != theirVc && theirVc != null)  {
            val canTalk = selfMember.hasPermission(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)
            if (!canTalk) {
                reply("I need permission to connect and speak in ${theirVc.name}")
                return false
            }

            guild.audioManager.openAudioConnection(theirVc)
            guild.audioManager.sendingHandler = player
            return true
        }

        return ourVc != null
    }

    class Loader(
            private val ctx: CommandContext,
            private val player: Player,
            private val identifier: String
    ) : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            val started = player.addNext(track)
            if (started) {
                ctx.reply("Started playing `${track.info.title}`")
            } else {
                ctx.reply("Added `${track.info.title}` for next song")
            }
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            player.addNext(*playlist.tracks.toTypedArray())
            ctx.reply("Added `${playlist.tracks.size}` tracks from `${playlist.name}` for next songs")
        }

        override fun noMatches() {
            ctx.reply("Nothing found for “$identifier”")
        }

        override fun loadFailed(exception: FriendlyException) {
            ctx.handleException(exception)
        }
    }

    override fun HelpContext.provideHelp() {
        addUsage("<url>")
        addDescription("Add the given track to the top of the queue")
    }
}
