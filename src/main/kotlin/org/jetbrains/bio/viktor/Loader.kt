package org.jetbrains.bio.viktor

import org.apache.log4j.Logger
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

            // See http://stackoverflow.com/a/15409446 for explanation.
            val usrPathsField = ClassLoader::class.java.getDeclaredField("usr_paths")
            usrPathsField.isAccessible = true

            val usrPaths = usrPathsField.get(null) as Array<String>
            if (LIBRARY_DIR.toString() !in usrPaths) {
                val newPaths = usrPaths.copyOf(usrPaths.size + 1)
                newPaths[newPaths.size - 1] = LIBRARY_DIR.toString()
                usrPathsField.set(null, newPaths)
            }
        }

        System.loadLibrary(name)
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
    internal var useNative = false

    fun ensureLoaded() {}

    private val arch: String get() {
        val arch = System.getProperty("os.arch").toLowerCase()
        return when (arch) {
            "amd64", "x86_64" -> "x86_64"
            else -> error("unsupported architecture: $arch")
        }
    }

    init {
        try {
            ResourceLibrary("simd.$arch").install()

            when {
                isAvxSupported() -> {
                    ResourceLibrary("simd.avx.$arch").install()
                    useNative = true
                }
                isSse2Supported() -> {
                    ResourceLibrary("simd.sse2.$arch").install()
                    useNative = true
                }
                else -> warnNoOptimization()
            }
        } catch (e: Throwable) {
            warnNoOptimization()
        }
    }

    private fun warnNoOptimization() {
        LOG.info("SIMD optimization is not available for your system, use --debug for details.")
        LOG.debug("No supported SIMD instruction sets were detected on your system.\n" +
                "Currently supported SIMD instruction sets: SSE2, AVX.\n" +
                "Fallback Kotlin implementation will be used.\n" +
                "Build viktor for your system from source as described in " +
                "https://github.com/JetBrains-Research/viktor")
    }
}

internal external fun isAvxSupported(): Boolean
internal external fun isSse2Supported(): Boolean
