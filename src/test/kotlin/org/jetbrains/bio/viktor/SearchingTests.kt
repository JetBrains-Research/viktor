package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals

class SearchingTests {
    @Test fun empty() {
        assertEquals(0, StridedVector(0).searchSorted(42.0))
    }

    @Test fun singleLess() {
        assertEquals(0, StridedVector.of(42.0).searchSorted(0.0))
        assertEquals(0, StridedVector.of(42.0, 42.0, 42.0).searchSorted(0.0))
    }

    @Test fun singleGreater() {
        assertEquals(1, StridedVector.of(0.0).searchSorted(42.0))
        assertEquals(3, StridedVector.of(0.0, 0.0, 0.0).searchSorted(42.0))
    }

    @Test fun duplicates() {
        assertEquals(0, StridedVector.of(0.0, 0.0, 0.0).searchSorted(0.0))
    }
}