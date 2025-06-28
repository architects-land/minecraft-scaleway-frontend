package world.anhgelus.world.architectsland.minecraftscalewayfrontend.event

import net.minestom.server.event.trait.CancellableEvent

class InstanceStartEvent : CancellableEvent {
    var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(b: Boolean) {
        cancelled = b
    }
}