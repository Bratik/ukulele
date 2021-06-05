package dev.arbjerg.ukulele.command

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import dev.arbjerg.ukulele.audio.Player
import dev.arbjerg.ukulele.audio.PlayerRegistry
import dev.arbjerg.ukulele.features.HelpContext
import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import net.dv8tion.jda.api.Permission
import org.springframework.stereotype.Component

@Component
class SearchCommand(
        val players: PlayerRegistry,
        val apm: AudioPlayerManager,
) : Command("search") {
    override suspend fun CommandContext.invoke() {
    if (!ensureVoiceChannel()) return
    val identifier = argumentText
    apm.loadItem("ytsearch: $identifier", Loader(this, player, identifier))
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


    override fun HelpContext.provideHelp() {
        addUsage("<query>")
        addDescription("search for a track on youtube")
    }

    class Loader(
        private val ctx: CommandContext,
        private val player: Player,
        private val identifier: String
    ) : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
        val started = player.add(track)
        if (started) {
            ctx.reply("Started playing `${track.info.title}`")
        } else {
            ctx.reply("Added `${track.info.title}`")
        }
    }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            player.add(*playlist.tracks.slice(0..0).toTypedArray())
            ctx.reply("Added `${playlist.tracks[0].info.title}`")
        }

        override fun noMatches() {
            ctx.reply("Nothing found for “$identifier”")
        }

        override fun loadFailed(exception: FriendlyException) {
            ctx.handleException(exception)
        }
    }
}
