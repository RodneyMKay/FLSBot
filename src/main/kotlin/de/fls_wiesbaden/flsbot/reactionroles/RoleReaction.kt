package de.fls_wiesbaden.flsbot.reactionroles

import discord4j.common.util.Snowflake
import discord4j.core.`object`.reaction.ReactionEmoji

data class RoleReaction(val emote: ReactionEmoji, val channelId: Snowflake, val messageId: Snowflake)