package de.fls_wiesbaden.flsbot.reactionroles

import de.fls_wiesbaden.flsbot.currentKeys
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import org.apache.commons.configuration2.Configuration
import reactor.core.publisher.Mono
import java.lang.NumberFormatException

class ReactionRule(
        val channelId: Snowflake,
        val messageId: Snowflake,
        val reactionRoles: Map<ReactionEmoji, Snowflake>
) {
    private val allRoles = reactionRoles.values

    fun getRoleReactions(): List<RoleReaction> {
        return reactionRoles.map { e ->
            RoleReaction(e.key, channelId, messageId)
        }
    }

    fun addReaction(event: ReactionAddEvent): Mono<Void> {
        return Mono.justOrEmpty(event.member)
                .filter { member -> !member.roleIds.any { it in allRoles } }
                .flatMap { member ->
                    Mono.justOrEmpty<Snowflake>(reactionRoles[event.emoji])
                            .flatMap { member.addRole(it) }
                }
    }

    fun removeReaction(event: ReactionRemoveEvent): Mono<Void> {
        return event.user
                .flatMap { user ->
                    Mono.justOrEmpty(event.guildId)
                            .flatMap { user.asMember(it) }
                }
                .flatMap { member ->
                    Mono.justOrEmpty<Snowflake>(reactionRoles[event.emoji])
                            .filter { member.roleIds.contains(it) }
                            .flatMap { member.removeRole(it) }
                }
    }

    fun checkStartup(gateway: GatewayDiscordClient): Mono<Void> {
        return Mono.empty()
        // TODO: Implement
//        return gateway.getChannelById(channelId)
//                .flatMap { Mono.justOrEmpty<GuildMessageChannel>(it as? GuildMessageChannel) }
//                .flatMap { it.getMessageById(messageId) }
//                .flatMap { message ->
//                    Flux.fromIterable(reactionRoles.entries)
//                            .flatMap { e -> e.value }
//                }
//                .then()
    }

    companion object {
        fun fromConfig(config: Configuration, path: String): ReactionRule {
            val channelId = Snowflake.of(config.getString("$path.channelId"))
            val messageId = Snowflake.of(config.getString("$path.messageId"))

            val reactions: MutableMap<ReactionEmoji, Snowflake> = mutableMapOf()

            for (emojiString in config.currentKeys("$path.roles")) {
                val emoji = try {
                    ReactionEmoji.custom(Snowflake.of(emojiString), "", false) // Only id relevant for matching
                } catch (e: NumberFormatException) {
                    ReactionEmoji.unicode(emojiString)
                }

                val role = Snowflake.of(config.getString("$path.roles.$emojiString"))

                reactions[emoji] = role
            }

            return ReactionRule(channelId, messageId, reactions)
        }
    }
}