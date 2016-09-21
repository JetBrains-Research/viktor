package org.jetbrains.bio.viktor

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal inline fun withTempFile(prefix: String, suffix: String,
                                 block: (Path) -> Unit) {
    val path = Files.createTempFile(prefix, suffix)
    try {
        block(path)
    } finally {
        try {
            Files.delete(path)
        } catch (e: IOException) {
            // Mmaped buffer not yet garbage collected. Leave it to the VM.
            path.toFile().deleteOnExit()
        }
    }
}
