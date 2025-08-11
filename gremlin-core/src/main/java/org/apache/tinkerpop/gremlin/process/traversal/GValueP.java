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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GValueP<V> implements PInterface<V> {
    protected PBiPredicate<V, V> biPredicate;
    private GValue<V> value;
    private Collection<GValue<V>> values;
    private GValue<V> originalValue;
    private Collection<GValue<V>> originalValues;

    private boolean parameterized;

    public GValueP(final PBiPredicate<V, V> biPredicate, GValue<V> value) {
        this.value = value;
        this.originalValue = value;
        this.biPredicate = biPredicate;
        this.parameterized = (value != null && value.isVariable());
    }

    public GValueP(final PBiPredicate<V, V> biPredicate, Collection<GValue<V>> values) {
        this.values = values;
        this.originalValues = values;
        this.biPredicate = biPredicate;
        this.parameterized = (this.values != null && this.values.stream().anyMatch(v -> v != null && v.isVariable()));
    }

    @Override
    public PBiPredicate<V, V> getBiPredicate() {
        return biPredicate;
    }

    @Override
    public V getOriginalValue() {
        return this.originalValue != null ? this.originalValue.get() : (V) stripGValues(this.originalValues);
    }

    @Override
    public String getPredicateName() {
        return biPredicate.getPredicateName();
    }

    @Override
    public V getValue() {
        return this.value != null ? this.value.get() : (V) stripGValues(this.values);
    }

    public Object getGValue() {
        return this.value != null ? this.value : this.values;
    }

    @Override
    public void setValue(final V value) {
        if (value instanceof Collection) {
            this.values = ((Collection<V>) value).stream().map(v -> GValue.of(v)).collect(Collectors.toList()); //TODO:: retain original collection type
            this.parameterized = values.stream().anyMatch(v -> v != null && v.isVariable());
            this.value = null;
        } else {
            this.value = value instanceof GValue ? (GValue<V>) value : GValue.of(value);
            this.parameterized = this.value != null && this.value.isVariable();
            this.values = null;
        }
    }

    public void setValue(final GValue<V> value) {
        this.value = value;
        this.parameterized = (value != null && value.isVariable());
        this.values = null;
    }

    @Override
    public boolean test(final V testValue) {
        throw new IllegalStateException("GValueP is not testable");
    }

    @Override
    public GValueP<V> negate() {
        return this.values == null ? new GValueP<>(this.biPredicate.negate(), this.value) : new GValueP<>(this.biPredicate, this.values);
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

    public GValueP<V> clone() {
        try {
            return (GValueP<V>) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        int result = this.biPredicate.hashCode();
        if (null != this.value)
            result ^= this.value.hashCode();
        if (null != this.values)
            result ^= this.values.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof GValueP &&
                ((GValueP) other).getClass().equals(this.getClass()) &&
                ((GValueP) other).getBiPredicate().equals(this.biPredicate) &&
                ((((GValueP) other).value == null && this.value == null) || ((GValueP) other).value.equals(this.value)) &&
                ((((GValueP) other).values == null && this.values == null) || ((GValueP) other).values.equals(this.values));
    }

    @Override
    public P<V> reduceGValue() {
        return new P<>(biPredicate, getValue());
    }

    @Override
    public boolean isParameterized() {
        return parameterized;
    }

    @Override
    public void updateVariable(final String name, final Object value) {
        if (this.parameterized) {
            if (this.value != null && name.equals(this.value.getName())) {
                this.value = GValue.of(name, (V) value);
                this.originalValue = GValue.of(name, (V) value); //TODO:: is this right?
            } else if (this.values instanceof List) { // Special handling for List to update GValues in-place.
                List<GValue<V>> valuesList = (List<GValue<V>>) this.values;
                if (valuesList != null) {
                    for (int i = 0; i < valuesList.size(); i++) {
                        if (valuesList.get(i) != null && valuesList.get(i) instanceof GValue) {
                            if (name.equals(valuesList.get(i).getName())) {
                                valuesList.set(i, GValue.of(name, (V) value));
                                ((List<GValue<V>>) this.originalValues).set(i, GValue.of(name, (V) value)); //TODO:: is this right?
                            }
                        }
                    }
                }
            } else {
                for (Object v : this.values) {
                    if (v != null && v instanceof GValue) {
                        if (name.equals(((GValue<?>) v).getName())) {
                            this.values.remove(v);
                            this.values.add(GValue.of(name, (V) value));
                            this.originalValues.remove(v);
                            this.originalValues.add(GValue.of(name, (V) value)); //TODO:: is this right?
                        }
                    }
                }
            }
        }
    }

    @Override
    public Set<GValue<?>> getGValues() {
        Set<GValue<?>> results = new HashSet<>();
        if (value != null) {
            if (value.isVariable()) {
                results.add(value);
            } else if (values != null) {
                for (GValue<V> v : values) {
                    if (v != null && v.isVariable()) {
                        results.add(v);
                    }
                }
            }
        }
        return results;
    }

    private Collection<V> stripGValues(Collection<GValue<V>> value) {
        if (value != null) {
            List<V> strippedValues = new ArrayList<>(); //TODO:: retain original collection type
            for (GValue<V> gValue : value) {
                strippedValues.add(gValue == null ? null : gValue.get());
            }
            return strippedValues;
        }
        return null;
    }
}
