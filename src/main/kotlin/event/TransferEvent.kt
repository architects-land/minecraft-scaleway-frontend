package world.anhgelus.world.architectsland.minecraftscalewayfrontend.event

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.CancellableEvent

class TransferEvent(val player: Player) : CancellableEvent {
    var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(b: Boolean) {
        cancelled = b
    }
}