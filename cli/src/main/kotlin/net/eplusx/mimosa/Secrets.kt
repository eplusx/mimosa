package net.eplusx.mimosa

import java.util.Properties

object Secrets {
    object SwitchBot {
        val token: String by lazy { props.getProperty("switchbot.token")!! }
        val secret: String by lazy { props.getProperty("switchbot.secret")!! }
    }

    object Nature {
        val token: String by lazy { props.getProperty("nature.token")!!}
    }

    private val props: Properties by lazy {
        Properties().apply {
            Secrets::class.java.getResourceAsStream("/secrets.config")!!.use { this.load(it) }
        }
    }
}