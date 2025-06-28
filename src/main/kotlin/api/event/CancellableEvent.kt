package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.event

interface CancellableEvent : Event {
    fun cancel()
}