package io.rsbox.engine.game.model.entity

import io.rsbox.engine.Engine
import io.rsbox.engine.game.model.World
import io.rsbox.engine.net.game.Packet
import io.rsbox.engine.net.Session
import io.rsbox.engine.net.game.packet.outbound.RegionLoadLoginPacket

/**
 * @author Kyle Escobar
 */

class Player(override val engine: Engine, override val world: World) : LivingEntity(), io.rsbox.api.entity.Player {

    lateinit var session: Session

    /**
     * Player Data
     */
    override lateinit var username: String
    override lateinit var passwordHash: String
    override lateinit var uuid: String
    override lateinit var displayName: String
    override var privilege: Int = 0
    override var banned: Boolean = false

    override var initiated = false

    var lastIndex = -1

    /**
     * Global indexes
     * GPI is arrays of entities which holds the data used to calculate what players are needed
     * to be drawn on the client.
     */
    val gpiLocalPlayers = arrayOfNulls<Player>(2048)

    val gpiLocalIndexes = IntArray(2048)

    var gpiLocalCount = 0

    val gpiExternalIndexes = IntArray(2048)

    var gpiExternalCount = 0

    val gpiTileHashMultipliers = IntArray(2048)


    private fun register() {
        world.register(this)
    }

    fun login() {
        this.register()

        // Setup initial GPI data.
        gpiLocalPlayers[index] = this
        gpiLocalIndexes[gpiLocalCount++] = index

        for(i in 1 until 2048) {
            if(i == index) continue
            gpiExternalIndexes[gpiExternalCount++] = i
            gpiTileHashMultipliers[i] =  if(i < world.players.capacity) world.players[i]?.tile?.asTileHashMultiplier ?: 0 else 0
        }

        val tiles = IntArray(gpiTileHashMultipliers.size)
        System.arraycopy(gpiTileHashMultipliers, 0, tiles, 0, tiles.size)

        write(RegionLoadLoginPacket(index, tile, tiles, world.engine.xteaKeyService))

        initiated = true
    }

    fun prePulse() {

    }

    fun pulse() {
        this.session.pulse()
    }

    fun postPulse() {

    }

    /**
     * Network related functions
     */
    fun write(message: Packet) {
        session.write(message)
    }

    fun flush() {
        session.flush()
    }

    fun disconnect() {
        session.close()
    }
}