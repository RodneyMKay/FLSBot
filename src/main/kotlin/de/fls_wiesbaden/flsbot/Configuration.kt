package de.fls_wiesbaden.flsbot

import org.apache.commons.configuration2.Configuration

fun Configuration.currentKeys(path: String): List<String> {
    val length = path.length + 1

    return this.getKeys(path)
            .asSequence()
            .map { s -> s.substring(length) }
            .map { s -> s.split(".")[0] }
            .distinct()
            .toList()
}