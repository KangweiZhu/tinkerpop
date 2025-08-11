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
package org.apache.tinkerpop.gremlin.process.traversal.util;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.PInterface;
import org.apache.tinkerpop.gremlin.process.traversal.step.GValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ConnectiveP<V> extends P<V> {

    protected List<PInterface<V>> predicates = new ArrayList<>();
    private boolean parameterized;

    public ConnectiveP(final List<PInterface<V>> predicates) {
        super(null, (V) null);

        if (predicates.size() < 2)
            throw new IllegalArgumentException("The provided " + this.getClass().getSimpleName() + " array must have at least two arguments: " + predicates.size());

        this.parameterized = predicates.stream().anyMatch(PInterface::isParameterized);
    }

    public List<PInterface<V>> getPredicates() {
        return Collections.unmodifiableList(this.predicates);
    }

    @Override
    public P<V> negate() {
        final List<PInterface<V>> negated = new ArrayList<>();
        for (final PInterface<V> predicate : this.predicates) {
            negated.add(predicate.negate());
        }
        this.predicates = negated;
        return this;
    }

    protected P<V> negate(final ConnectiveP<V> p) {
        final List<PInterface<V>> negated = new ArrayList<>();
        for (final PInterface<V> predicate : this.predicates) {
            negated.add(predicate.negate());
        }
        p.predicates = negated;
        return p;
    }

    @Override
    public int hashCode() {
        int result = 0, i = 0;
        for (final PInterface p : this.predicates) {
            result ^= Integer.rotateLeft(p.hashCode(), i++);
        }
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (other != null && other.getClass().equals(this.getClass())) {
            final List<PInterface<V>> otherPredicates = ((ConnectiveP<V>) other).predicates;
            if (this.predicates.size() == otherPredicates.size()) {
                for (int i = 0; i < this.predicates.size(); i++) {
                    if (!this.predicates.get(i).equals(otherPredicates.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean test(V testValue) {
        if (this.isParameterized()) {
            predicates = predicates.stream().map(p -> p == null ? null : p.reduceGValue()).collect(Collectors.toList());
            parameterized = false;
        }
        return super.test(testValue);
    }

    @Override
    public ConnectiveP<V> clone() {
        final ConnectiveP<V> clone = (ConnectiveP<V>) super.clone();
        clone.predicates = new ArrayList<>();
        for (final PInterface<V> p : this.predicates) {
            clone.predicates.add(p.clone());
        }
        return clone;
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

    @Override
    public boolean isParameterized() {
        return parameterized;
    }

    @Override
    public void updateVariable(final String name, final Object value) {
        predicates.stream().map((p) -> {
            p.updateVariable(name, value);
            return p;
        });
    }

    @Override
    public Set<GValue<?>> getGValues() {
        Set<GValue<?>> allGValues = new HashSet<>();
        for (final PInterface<V> p : this.predicates) {
            allGValues.addAll(p.getGValues());
        }
        return allGValues;
    }

    @Override
    public abstract P<V> reduceGValue();
}