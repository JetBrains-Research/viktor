package org.jetbrains.bio.viktor

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
            }
        } catch (e: Throwable) {
            System.err.println(listOf(
                    "Native SIMD optimization of vector operations is not available.",
                    "Fallback Kotlin implementation will be used instead.").joinToString("\n"))
            e.printStackTrace(System.err)
        }
    }
}

internal external fun isAvxSupported(): Boolean
internal external fun isSse2Supported(): Boolean
