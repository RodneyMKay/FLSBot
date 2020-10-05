package de.fls_wiesbaden.flsbot.reactionroles

import de.fls_wiesbaden.flsbot.Bot
import de.fls_wiesbaden.flsbot.Module
import de.fls_wiesbaden.flsbot.currentKeys
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import reactor.core.publisher.Mono

class ReactionRoles : Module {
    override fun register(bot: Bot, gateway: GatewayDiscordClient) {
        val directives: MutableMap<RoleReaction, ReactionRule> = mutableMapOf()

        for (ruleString in bot.config.currentKeys("reaction-roles")) {
            val rule = ReactionRule.fromConfig(bot.config, "reaction-roles.$ruleString")
            rule.getRoleReactions().forEach { directives[it] = rule }
        }

        gateway.on(ReactionAddEvent::class.java)
                .flatMap { event ->
                    Mono.just(event)
                            .map { RoleReaction(it.emoji, it.channelId, it.messageId) }
                            .flatMap { Mono.justOrEmpty<ReactionRule>(directives[it]) }
                            .flatMap { it.addReaction(event) }
                }
                .subscribe()

        gateway.on(ReactionRemoveEvent::class.java)
                .flatMap { event ->
                    Mono.just(event)
                            .map { RoleReaction(it.emoji, it.channelId, it.messageId) }
                            .flatMap { Mono.justOrEmpty<ReactionRule>(directives[it]) }
                            .flatMap { it.removeReaction(event) }
                }
                .subscribe()

        directives.values.forEach {
            it.checkStartup(gateway)
        }
    }
}
