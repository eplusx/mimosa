package net.eplusx.mimosa.lib

import java.util.Properties

/**
 * Mimosa secrets.
 *
 * To make use of these secrets, you need to create a file named `secrets.config` in the `resources` directory by
 * copying `secrets.config.template` and filling in the values in each project (cli and server).
 */
object Secrets {
    object SwitchBot {
        val accessToken: String by lazy { props.getProperty("switchbot.access-token")!! }
        val secret: String by lazy { props.getProperty("switchbot.secret")!! }
    }

    object Nature {
        val accessToken: String by lazy { props.getProperty("nature.access-token")!!}
    }

    private val props: Properties by lazy {
        Properties().apply {
            Secrets::class.java.getResourceAsStream("/secrets.config")!!.use { this.load(it) }
        }
    }
}