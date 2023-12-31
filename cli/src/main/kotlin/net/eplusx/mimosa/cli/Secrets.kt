package net.eplusx.mimosa.cli

import java.util.Properties

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