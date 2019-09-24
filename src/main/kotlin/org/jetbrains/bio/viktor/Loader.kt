package org.jetbrains.bio.viktor

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Logger
import org.apache.log4j.SimpleLayout
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

internal class ResourceLibrary(private val name: String) {
    @Suppress("unchecked_cast")
    fun install() {
        val resource = System.mapLibraryName(name)
        val inputStream = ResourceLibrary::class.java.getResourceAsStream("/$resource")
        if (inputStream != null) {
            val libraryPath = LIBRARY_DIR.resolve(resource)
            Files.copy(inputStream, libraryPath, REPLACE_EXISTING)
            System.load(libraryPath.toString())
        } else {
            System.loadLibrary(name)
        }
    }

    companion object {
        private val LIBRARY_DIR: Path by lazy {
            val path = Files.createTempDirectory("simd")
            Runtime.getRuntime().addShutdownHook(Thread {
                path.toFile().deleteRecursively()
            })
            path
        }
    }
}

internal object Loader {

    private val LOG = Logger.getLogger(Loader::class.java)

    /** If `true` vector operations will be SIMD-optimized. */
    internal var nativeLibraryLoaded: Boolean = false
        private set

    private var optimizationSupported = false
    private var architectureSupported = false


    fun ensureLoaded() {}

    private val arch: String get() {
        val arch = System.getProperty("os.arch").toLowerCase()
        return when (arch) {
            "amd64", "x86_64" -> "x86_64"
            else -> error("unsupported architecture: $arch")
        }
    }

    init {
        LOG.addAppender(ConsoleAppender(SimpleLayout()))

        try {
            architectureSupported = arch.let { true }
            ResourceLibrary("simd.$arch").install()
            nativeLibraryLoaded = true

            when {
                isAvxSupported() -> {
                    ResourceLibrary("simd.avx.$arch").install()
                    optimizationSupported = true
                }
                isSse2Supported() -> {
                    ResourceLibrary("simd.sse2.$arch").install()
                    optimizationSupported = true
                }
                else -> warnNoOptimization()
            }
        } catch (e: Throwable) {
            LOG.info(e.message)
            warnNoOptimization()
        }
    }

    private fun warnNoOptimization() {
        if (!architectureSupported) {
            LOG.info("SIMD optimization is not available for your architecture, use --debug for details.")
            LOG.debug(
"""Currently supported architectures: x86_64, amd64.
Fallback Kotlin implementation will be used.
Build viktor for your system from source as described in https://github.com/JetBrains-Research/viktor"""
            )
        } else if (!nativeLibraryLoaded) {
            LOG.info("Couldn't load native SIMD library, use --debug for details.")
            LOG.debug(
"""Native SIMD library couldn't be loaded.
Currently supported operational systems: Linux, Windows, MacOS.
Fallback Kotlin implementation will be used.
Build viktor for your system from source as described in https://github.com/JetBrains-Research/viktor"""
            )
        }
        else if (!optimizationSupported) {
            LOG.info("SIMD optimization is not available for your system, use --debug for details.")
            LOG.debug(
"""No supported SIMD instruction sets were detected on your system.
Currently supported SIMD instruction sets: SSE2, AVX.
Fallback Kotlin implementation will be used.
Build viktor for your system from source as described in https://github.com/JetBrains-Research/viktor"""
            )
        }
    }
}

internal external fun isAvxSupported(): Boolean
internal external fun isSse2Supported(): Boolean

fun main(args: Array<String>) {
    Loader.ensureLoaded()
}
