package org.jetbrains.bio.jni;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * @author Sergei Lebedev
 * @date 15/01/15
 */
class Internal {
    static void sampleUniformGamma(final double[] values) {
        final RandomDataGenerator gen = new RandomDataGenerator();
        for (int i = 0; i < values.length; i++) {
            if (i < values.length / 3) {
                values[i] = gen.nextUniform(0, 1);
            } else {
                values[i] = gen.nextGamma(42., 42.);
            }
        }
    }
}
