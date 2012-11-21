/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc.util;


import java.util.Iterator;

import static org.junit.Assert.*;


/**
 * All the assert utilities we wished JUnit included.
 */
public class Assert {

    /**
     * Asserts that two iterables have equal contents.  A given element
     * may be null in which case it is equal if and only if the
     * corresponding element in the other iterable is also null.
     *
     * @param expecteds Expected values, not null
     * @param actuals Actual values, not null
     */
    public static <E> void assertIterablesEqual(Iterable<E> expecteds, Iterable<E> actuals) {
        int index = 0;
        E expected;
        E actual;
        Iterator<E> iterExpecteds = expecteds.iterator();
        Iterator<E> iterActuals = actuals.iterator();
        while (iterExpecteds.hasNext() && iterActuals.hasNext()) {
            expected = iterExpecteds.next();
            actual = iterActuals.next();
            if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
                fail("Iterables first differ at index " + index + ": expected <" + expected + "> but was <" + actual + ">.");
            }
            index++;
        }
        if (iterExpecteds.hasNext() != iterActuals.hasNext()) {
            if (iterExpecteds.hasNext()) {
                fail("Iterable 'actuals' has fewer elements than expected.");
            } else {
                fail("Iterable 'actuals' has more elements than expected.");
            }
        }
    }
}
