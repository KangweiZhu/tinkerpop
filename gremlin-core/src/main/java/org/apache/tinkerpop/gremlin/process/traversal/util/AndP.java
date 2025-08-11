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

import org.apache.tinkerpop.gremlin.process.traversal.GremlinTypeErrorException;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.PBiPredicate;
import org.apache.tinkerpop.gremlin.process.traversal.PInterface;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class AndP<V> extends ConnectiveP<V> {

    public AndP(final List<PInterface<V>> predicates) {
        super(predicates);
        for (final PInterface<V> p : predicates) {
            this.and(p);
        }
        this.biPredicate = new AndBiPredicate(this);
    }

    @Override
    public P<V> and(final Predicate<? super V> predicate) {
        if (!(predicate instanceof PInterface))
            throw new IllegalArgumentException("Only PInterface predicates can be and'd together");
        else if (predicate instanceof AndP)
            this.predicates.addAll(((AndP) predicate).getPredicates());
        else
            this.predicates.add((PInterface<V>) predicate);
        return this;
    }

    @Override
    public P<V> reduceGValue() {
        return new AndP<>(predicates.stream().map(p -> p == null ? null : p.reduceGValue()).collect(Collectors.toList()));
    }

    @Override
    public P<V> negate() {
        super.negate();
        return new OrP<>(this.predicates);
    }

    @Override
    public String toString() {
        return "and(" + StringFactory.removeEndBrackets(this.predicates) + ")";
    }

    @Override
    public AndP<V> clone() {
        final AndP<V> clone = (AndP<V>) super.clone();
        clone.biPredicate = new AndBiPredicate(clone);
        return clone;
    }

    private class AndBiPredicate implements PBiPredicate<V, V>, Serializable {

        private final AndP<V> andP;

        private AndBiPredicate(final AndP<V> andP) {
            this.andP = andP;
        }

        @Override
        public boolean test(final V valueA, final V valueB) {
            GremlinTypeErrorException typeError = null;
            for (final PInterface<V> predicate : this.andP.predicates) {
                try {
                    if (!predicate.test(valueA))
                        return false;
                } catch (GremlinTypeErrorException ex) {
                    // hold onto it until the end in case any other arguments evaluate to FALSE
                    typeError = ex;
                }
            }
            if (typeError != null)
                throw typeError;
            return true;
        }

        @Override
        public String getPredicateName() {
            return "and";
        }
    }
}
