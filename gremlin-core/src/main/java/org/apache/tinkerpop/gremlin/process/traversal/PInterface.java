package org.apache.tinkerpop.gremlin.process.traversal;
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
import org.apache.tinkerpop.gremlin.process.traversal.step.GValue;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Predicate;

public interface PInterface<V> extends Predicate<V>, Serializable, Cloneable {
    PBiPredicate<V, V> getBiPredicate();

    V getOriginalValue();

    String getPredicateName();

    V getValue();

    void setValue(V value);

    @Override
    boolean test(V testValue);

    @Override
    PInterface<V> negate();

    @Override
    PInterface<V> and(Predicate<? super V> predicate);

    @Override
    PInterface<V> or(Predicate<? super V> predicate);

    boolean isParameterized();

    public void updateVariable(final String name, final Object value);

    public Set<GValue<?>> getGValues();

    public PInterface<V> clone();

    public P<V> reduceGValue();
}
