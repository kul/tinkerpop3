package com.tinkerpop.gremlin.structure.io.util;

import com.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class IOVertex extends IOElement {
    public static IOVertex from(final Vertex vertex) {
        final IOVertex iov = new IOVertex();
        return from(vertex, iov);
    }
}