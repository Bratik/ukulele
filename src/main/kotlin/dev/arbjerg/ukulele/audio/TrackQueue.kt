package dev.arbjerg.ukulele.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackQueue {

    /**
    bratik: je connais rien à kotlin du coup j'ai transformé la val en var lol
     */
    private var queue = mutableListOf<AudioTrack>()
    val tracks: List<AudioTrack> get() = queue
    val duration: Long get() = queue.filterNot { it.info.isStream }.sumOf { it.info.length } // Streams don't have a valid time.

    fun add(vararg tracks: AudioTrack) { queue.addAll(tracks) }
    /**
    bratik: HB pour pousser en haut de la list
     */
    fun addNext(vararg tracks: AudioTrack) {
         var newTracksQueue = mutableListOf<AudioTrack>()
         newTracksQueue.addAll(tracks)
         queue = (newTracksQueue+queue).toMutableList()
    }
    fun take() = queue.removeFirstOrNull()
    fun peek() = queue.firstOrNull()
    fun clear() = queue.clear()

    fun removeRange(range: IntRange): List<AudioTrack> {
        val list = queue.slice(range)
        queue.removeAll(list)
        return list
    }
}