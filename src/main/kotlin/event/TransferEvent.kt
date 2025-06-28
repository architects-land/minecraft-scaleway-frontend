package world.anhgelus.architectsland.minecraftscalewayfrontend.event

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.CancellableEvent

/**
 * Emits when a player is transferred to the server
 */
class TransferEvent(val player: Player) : CancellableEvent {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(b: Boolean) {
        cancelled = b
    }
}