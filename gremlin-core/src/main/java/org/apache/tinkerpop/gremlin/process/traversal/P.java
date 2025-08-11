/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.step.GValue;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Predefined {@code Predicate} values that can be used to define filters to {@code has()} and {@code where()}.
 * 
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class P<V> implements PInterface<V> {

    protected PBiPredicate<V, V> biPredicate;
    protected V value;
    protected V originalValue;
    protected boolean parameterized;

    public P(final PBiPredicate<V, V> biPredicate, V value) {
        this.value = value;
        this.originalValue = value;
        this.biPredicate = biPredicate;
        this.parameterized = false;
    }

    @Override
    public PBiPredicate<V, V> getBiPredicate() {
        return this.biPredicate;
    }

    /**
     * Gets the original value used at time of construction of the {@code P}. This value can change its type
     * in some cases.
     */
    @Override
    public V getOriginalValue() {
        return originalValue;
    }

    /**
     * Get the name of the predicate
     */
    @Override
    public String getPredicateName() { return biPredicate.getPredicateName(); }

    /**
     * Gets the current value to be passed to the predicate for testing.
     */
    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public void setValue(final V value) {
        this.value = value;
    }

    @Override
    public boolean test(final V testValue) {
        return this.biPredicate.test(testValue, this.value);
    }

    @Override
    public int hashCode() {
        int result = this.biPredicate.hashCode();
        if (null != this.originalValue)
            result ^= this.originalValue.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof P &&
                ((P) other).getClass().equals(this.getClass()) &&
                ((P) other).getBiPredicate().equals(this.biPredicate) &&
                ((((P) other).getOriginalValue() == null && this.originalValue == null) || ((P) other).getOriginalValue().equals(this.originalValue));
    }

    @Override
    public String toString() {
        return null == this.originalValue ? this.biPredicate.toString() : this.biPredicate.toString() + "(" + this.originalValue + ")";
    }

    @Override
    public P<V> negate() {
        return new P<>(this.biPredicate.negate(), this.originalValue);
    }

    @Override
    public P<V> and(final Predicate<? super V> predicate) {
        if (!(predicate instanceof PInterface))
            throw new IllegalArgumentException("Only PInterface predicates can be and'd together");
        return new AndP<>(Arrays.asList(this, (PInterface<V>) predicate));
    }

    @Override
    public P<V> or(final Predicate<? super V> predicate) {
        if (!(predicate instanceof PInterface))
            throw new IllegalArgumentException("Only PInterface predicates can be or'd together");
        return new OrP<>(Arrays.asList(this, (PInterface<V>) predicate));
    }

    public P<V> clone() {
        try {
            return (P<V>) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public P<V> reduceGValue() {
        return this;
    }

    @Override
    public boolean isParameterized() {
        return parameterized;
    }

    @Override
    public void updateVariable(String name, Object value) {
        // Do nothing
    }

    @Override
    public Set<GValue<?>> getGValues() {
        return Collections.EMPTY_SET;
    }

    //////////////// statics

    /**
     * Determines if values are equal.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> eq(final V value) {
        if (value instanceof GValue) {
            return P.eq((GValue<V>) value);
        }
        return new P(Compare.eq, value);
    }

    /**
     * Determines if values are equal.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> eq(final GValue<V> value) {
        if (value == null) {
            return P.eq((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.eq, value);
    }

    /**
     * Determines if values are not equal.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> neq(final V value) {
        if (value instanceof GValue) {
            return P.neq((GValue<V>) value);
        }
        return new P(Compare.neq, value);
    }

    /**
     * Determines if values are not equal.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> neq(final GValue<V> value) {
        if (value == null) {
            return P.neq((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.neq, value);
    }

    /**
     * Determines if a value is less than another.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> lt(final V value) {
        if (value instanceof GValue) {
            return P.lt((GValue<V>) value);
        }
        return new P(Compare.lt, value);
    }

    /**
     * Determines if a value is less than another.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> lt(final GValue<V> value) {
        if (value == null) {
            return P.lt((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.lt, value);
    }

    /**
     * Determines if a value is less than or equal to another.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> lte(final V value) {
        if (value instanceof GValue) {
            return P.lte((GValue<V>) value);
        }
        return new P(Compare.lte, value);
    }

    /**
     * Determines if a value is less than or equal to another.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> lte(final GValue<V> value) {
        if (value == null) {
            return P.lte((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.lte, value);
    }

    /**
     * Determines if a value is greater than another.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> gt(final V value) {
        if (value instanceof GValue) {
            return P.gt((GValue<V>) value);
        }
        return new P(Compare.gt, value);
    }

    /**
     * Determines if a value is greater than another.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> gt(final GValue<V> value) {
        if (value == null) {
            return P.gt((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.gt, value);
    }

    /**
     * Determines if a value is greater than or equal to another.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> gte(final V value) {
        if (value instanceof GValue) {
            return P.gte((GValue<V>) value);
        }
        return new P(Compare.gte, value);
    }

    /**
     * Determines if a value is greater than or equal to another.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> gte(final GValue<V> value) {
        if (value == null) {
            return P.gte((V) null);
        }
        return new GValueP<>((PBiPredicate<V, V>) Compare.gte, value);
    }

    /**
     * Determines if a value is within (exclusive) the range of the two specified values.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> inside(final V first, final V second) {
        if (first instanceof GValue || second instanceof GValue) {
            return P.inside(GValue.of(first), GValue.of(second));
        }
        return new AndP<V>(Arrays.asList(new P(Compare.gt, first), new P(Compare.lt, second)));
    }

    /**
     * Determines if a value is within (exclusive) the range of the two specified values.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> inside(final GValue<V> first, final GValue<V> second) {
        if (first == null && second == null) {
            return P.inside((V) null, (V) null);
        }
        return new AndP<V>(Arrays.asList(new GValueP<>((PBiPredicate<V, V>) Compare.gt, first), new GValueP<>((PBiPredicate<V, V>) Compare.lt, second)));
    }

    /**
     * Determines if a value is not within (exclusive) of the range of the two specified values.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> outside(final V first, final V second) {
        if (first instanceof GValue || second instanceof GValue) {
            return P.outside(GValue.of(first), GValue.of(second));
        }
        return new OrP<V>(Arrays.asList(new P(Compare.lt, first), new P(Compare.gt, second)));
    }

    /**
     * Determines if a value is not within (exclusive) of the range of the two specified values.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> outside(final GValue<V> first, final GValue<V> second) {
        if (first == null && second == null) {
            return P.outside((V) null, (V) null);
        }
        return new OrP<V>(Arrays.asList(new GValueP<>((PBiPredicate<V, V>) Compare.lt, first), new GValueP<>((PBiPredicate<V, V>) Compare.gt, second)));
    }

    /**
     * Determines if a value is within (inclusive) of the range of the two specified values.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> between(final V first, final V second) {
        if (first instanceof GValue || second instanceof GValue) {
            return P.between(GValue.of(first), GValue.of(second));
        }
        return new AndP<V>(Arrays.asList(new P(Compare.gte, first), new P(Compare.lt, second)));
    }

    /**
     * Determines if a value is within (inclusive) of the range of the two specified values.
     *
     * @since 3.8.0
     */
    public static <V> PInterface<V> between(final GValue<V> first, final GValue<V> second) {
        if (first == null && second == null) {
            return P.between((V) null, (V) null);
        }
        return new AndP<V>(Arrays.asList(new GValueP<>((PBiPredicate<V, V>)Compare.gte, first), new GValueP<>((PBiPredicate<V, V>)Compare.lt, second)));
    }

    /**
     * Determines if a value is within the specified list of values. If the array of arguments itself is {@code null}
     * then the argument is treated as {@code Object[1]} where that single value is {@code null}.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> within(final V... values) {
        final V[] v = null == values ? (V[]) new Object[] { null } : (V[]) values;
        return P.within(Arrays.asList(v));
    }

    /**
     * Determines if a value is within the specified list of values. Calling this with {@code null} is the same as
     * calling {@link #within(Object[])} using {@code null}.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> within(final Collection<V> value) {
        if (null == value) return P.within((V) null);
        if (value.stream().anyMatch(v -> v instanceof GValue)) {
            return new GValueP<>((PBiPredicate<V, V>) Contains.within, value.stream().map(v -> GValue.of(v)).collect(Collectors.toList())); //TODO:: retain original collection type
        }
        return new P(Contains.within, value);
    }

    /**
     * Determines if a value is not within the specified list of values. If the array of arguments itself is {@code null}
     * then the argument is treated as {@code Object[1]} where that single value is {@code null}.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> without(final V... values) {
        final V[] v = null == values ? (V[]) new Object[] { null } : values;
        return P.without(Arrays.asList(v));
    }

    /**
     * Determines if a value is not within the specified list of values. Calling this with {@code null} is the same as
     * calling {@link #within(Object[])} using {@code null}.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> without(final Collection<V> value) {
        if (null == value) return P.without((V) null);
        if (value.stream().anyMatch(v -> v instanceof GValue)) {
            return new GValueP<>((PBiPredicate<V, V>) Contains.without, value.stream().map(v -> GValue.of(v)).collect(Collectors.toList())); //TODO:: retain original collection type
        }
        return new P(Contains.without, value);
    }

    /**
     * Construct an instance of {@code P} from a {@code BiPredicate}.
     *
     * @since 3.0.0-incubating
     */
    public static P test(final PBiPredicate biPredicate, final Object value) {
        return new P(biPredicate, value);
    }

    /**
     * The opposite of the specified {@code P}.
     *
     * @since 3.0.0-incubating
     */
    public static <V> PInterface<V> not(final PInterface<V> predicate) {
        return predicate.negate();
    }

}
