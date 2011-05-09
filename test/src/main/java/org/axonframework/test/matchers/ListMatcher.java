/*
 * Copyright (c) 2010-2011. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.test.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation for matchers that use event-specific matchers to match against a list of Events.
 *
 * @param <T> the type of object expected in the matched List
 * @author Allard Buijze
 * @since 1.1
 */
public abstract class ListMatcher<T> extends BaseMatcher<List<? extends T>> {

    private List<Matcher<T>> failedMatchers = new ArrayList<Matcher<T>>();
    private final Matcher<T>[] matchers;

    /**
     * Creates an abstract matcher to match a number of Matchers against Events contained inside a Collection.
     *
     * @param matchers The matchers to match the individual Events in the Collection
     */
    protected ListMatcher(Matcher<T>... matchers) {
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Object item) {
        if (List.class.isInstance(item)) {
            return matchesList((List<T>) item);
        }
        return false;
    }

    /**
     * Evaluates the matcher for argument <code>item</code>.
     *
     * @param item the object against which the matcher is evaluated.
     * @return <code>true</code> if <code>item</code> matches, otherwise <code>false</code>.
     *
     * @see BaseMatcher
     */
    protected abstract boolean matchesList(List<T> item);

    /**
     * Matches all the remaining Matchers in the given <code>matcherIterator</code> against <code>null</code>.
     *
     * @param matcherIterator The iterator potentially containing more matchers
     * @return true if no matchers remain or all matchers succeeded
     */
    protected boolean matchRemainder(Iterator<Matcher<T>> matcherIterator) {
        // evaluate any excess matchers against null
        while (matcherIterator.hasNext()) {
            Matcher<T> matcher = matcherIterator.next();
            if (!matcher.matches(null)) {
                failedMatchers.add(matcher);
                return false;
            }
        }
        return true;
    }

    /**
     * Report the given <code>matcher</code> as a failing matcher. This will be used in the error reporting.
     *
     * @param matcher The failing matcher.
     */
    protected void reportFailed(Matcher<T> matcher) {
        failedMatchers.add(matcher);
    }

    /**
     * Returns a read-only list of Matchers, in the order they were provided in the constructor.
     *
     * @return a read-only list of Matchers, in the order they were provided in the constructor
     */
    protected List<Matcher<T>> getMatchers() {
        return Arrays.asList(matchers);
    }

    /**
     * Describes the type of collection expected. To be used in the sentence: "list with ... of: <description of
     * matchers>". E.g. "all" or "sequence".
     *
     * @param description the description to append the collection type to
     */
    protected abstract void describeCollectionType(Description description);

    @Override
    public void describeTo(Description description) {
        description.appendText("list with ");
        describeCollectionType(description);
        description.appendText(" of: ");
        for (int t = 0; t < matchers.length; t++) {
            if (t != 0 && t < matchers.length - 1) {
                description.appendText(", ");
            } else if (t == matchers.length - 1 && t > 0) {
                description.appendText(" ");
                description.appendText(getLastSeparator());
                description.appendText(" ");
            }
            description.appendText("<");
            matchers[t].describeTo(description);
            description.appendText(">");
            if (failedMatchers.contains(matchers[t])) {
                description.appendText(" (");
                description.appendText(failedMatcherMessage());
                description.appendText(")");
            }
        }
    }

    /**
     * The message to append behind a failing matcher. Defaults to FAILED!.
     *
     * @return The message to append behind a failing matcher
     */
    protected String failedMatcherMessage() {
        return "FAILED!";
    }

    /**
     * The separator to use between the two last events. Defaults to "and".
     *
     * @return The separator to use between the two last events. Defaults to "and".
     */
    protected String getLastSeparator() {
        return "and";
    }
}