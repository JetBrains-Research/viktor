package org.jetbrains.bio.jni

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class ResourceLibrary(private val name: String) {
    @Suppress("unchecked_cast")
    fun install() {
        val resource = System.mapLibraryName(name)
        val inputStream = ResourceLibrary::class.java.getResourceAsStream(resource)
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

object Loader {
    /** If `true` array operations will be SIMD-optimized. */
    internal var useNative = true

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
                isAvxSupported() -> ResourceLibrary("simd.avx.$arch").install()
                isSse2Supported() -> ResourceLibrary("simd.sse2.$arch").install()
                else -> {
                    disableNativeOptimization()
                }
            }
        } catch (e: Exception) {
            System.err.println(listOf(
                    "Native SIMD optimization of statistics and array operations is not available.",
                    "Pure Java fallback implementation will be used instead.").joinToString("\n"))
            e.printStackTrace(System.err)

            disableNativeOptimization()
        }
    }

    /**
     * Enables using the optimized native methods.
     *
     * Note that the optimization should be on by default;
     *
     * it is turned off in the various exceptional cases, e.g. when the library fails to load.
     * If the optimization was turned off automatically but you enable it by calling this method,
     * expect exceptions and runtime errors down the road. The most common use case for this method
     * is turning the optimization back on after disabling it by [.disableNativeOptimization]
     * for any reason.
     */
    fun enableNativeOptimization() {
        useNative = true
    }

    /**
     * Disables using the optimized native methods, opting for pure Java implementations instead.
     *
     * See [enableNativeOptimization] for caveats. Here are a few reasons for this method to be
     * called:
     * 1. producing a baseline reference value for benchmarking
     * 2. as a workaround when native methods fail unexpectedly (in most failure cases the optimization
     * is turned off automatically, but there always might be some really obscure ones)
     * 3. small arrays (native calls come with an overhead, so pure Java might be faster in these cases)
     */
    fun disableNativeOptimization() {
        useNative = false
    }
}

internal external fun isAvxSupported(): Boolean
internal external fun isSse2Supported(): Boolean
