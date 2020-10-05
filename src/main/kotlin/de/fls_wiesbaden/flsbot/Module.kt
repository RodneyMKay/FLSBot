package de.fls_wiesbaden.flsbot

import discord4j.core.GatewayDiscordClient

interface Module {
    fun register(bot: Bot, gateway: GatewayDiscordClient)
}