package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals

class SearchingTests {
    @Test fun singleLess() {
        assertEquals(0, F64Array.of(42.0).searchSorted(0.0))
        assertEquals(0, F64Array.of(42.0, 42.0, 42.0).searchSorted(0.0))
    }

    @Test fun singleGreater() {
        assertEquals(1, F64Array.of(0.0).searchSorted(42.0))
        assertEquals(3, F64Array.of(0.0, 0.0, 0.0).searchSorted(42.0))
    }

    @Test fun duplicates() {
        assertEquals(0, F64Array.of(0.0, 0.0, 0.0).searchSorted(0.0))
    }
}