package dev.arbjerg.ukulele.command

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.ukulele.audio.PlayerRegistry
import dev.arbjerg.ukulele.features.HelpContext
import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import org.springframework.stereotype.Component

@Component
/**
bratik: shuffle de la playlist en cours
 */
class ShuffleCommand : Command("shuffle","random") {
    override suspend fun CommandContext.invoke() {
        player.shuffle()
        reply("Playlist has been shuffled.")
    }
        override fun HelpContext.provideHelp() {
        addUsage("")
        addDescription("Shuffle the playlist.")
    }
}