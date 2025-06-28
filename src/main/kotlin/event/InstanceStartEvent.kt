package world.anhgelus.architectsland.minecraftscalewayfrontend.event

import net.minestom.server.event.trait.CancellableEvent

/**
 * Emits when the instance will be started
 */
class InstanceStartEvent : CancellableEvent {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(b: Boolean) {
        cancelled = b
    }
}