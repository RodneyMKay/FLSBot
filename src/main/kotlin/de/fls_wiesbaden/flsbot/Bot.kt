package de.fls_wiesbaden.flsbot

import de.fls_wiesbaden.flsbot.reactionroles.ReactionRoles
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.YAMLConfiguration
import org.yaml.snakeyaml.Yaml
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Bot private constructor(val gateway: GatewayDiscordClient, val config: Configuration) {
    private val modules: Array<Module> = arrayOf(
            ReactionRoles()
    )

    fun start() {
        for (module in modules) {
            module.register(this, gateway)
        }

        gateway.onDisconnect().block()
    }

    companion object {
        fun fromConfig(configPath: Path): Bot {
            val config = YAMLConfiguration()
            Files.newInputStream(configPath).use { input ->
                config.read(input)
            }

            val token = config.getString("token", "your-token-here")
            val client = DiscordClient.create(token)
            val gateway = client.login().block() ?: throw IOException("Could not log in to discord!")

            return Bot(gateway, config)
        }
    }
}

fun main() {
    val bot = Bot.fromConfig(Paths.get("config.yml"))
    bot.start()
}