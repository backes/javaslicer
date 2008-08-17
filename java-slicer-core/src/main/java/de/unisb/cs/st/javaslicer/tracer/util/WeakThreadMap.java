/*
 *  @(#)HashMap.java    1.73 07/03/13
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * THIS COPY IS HERE FOR NOT BEING INSTRUMENTED!
 *
 */

package de.unisb.cs.st.javaslicer.tracer.util;

public class WeakThreadMap<V> extends WeakIdentityHashMap<Thread, V> {

    @Override
    protected int hash(final Object key) {
        if (key instanceof Thread)
            return (int) (((Thread) key).getId());
        return 0;
    }

}
