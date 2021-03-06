package com.tinkerpop.gremlin.tinkergraph.process.graph.step.sideEffect;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.GraphStep;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.structure.Compare;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.HasContainer;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraphStep<E extends Element> extends GraphStep<E> {

    public final List<HasContainer> hasContainers = new ArrayList<>();

    public TinkerGraphStep(final Traversal traversal, final Graph graph, final Class<E> returnClass, final Object... ids) {
        super(traversal, graph, returnClass, ids);
        this.setIteratorSupplier(() -> (Iterator<E>) (Vertex.class.isAssignableFrom(this.returnClass) ? this.vertices() : this.edges()));
    }

    private Iterator<? extends Edge> edges() {
        final HasContainer indexedContainer = getIndexKey(Edge.class);
        return null == indexedContainer ?
                this.iteratorList(this.getGraph(TinkerGraph.class).iterators().edgeIterator(this.ids)) :
                TinkerHelper.queryEdgeIndex(this.getGraph(TinkerGraph.class), indexedContainer.key, indexedContainer.value).stream()
                        .filter(edge -> ElementHelper.idExists(edge.id(), this.ids) && HasContainer.testAll(edge, this.hasContainers))
                        .collect(Collectors.<Edge>toList()).iterator();
    }

    private Iterator<? extends Vertex> vertices() {
        final HasContainer indexedContainer = getIndexKey(Vertex.class);
        return null == indexedContainer ?
                this.iteratorList(this.getGraph(TinkerGraph.class).iterators().vertexIterator(this.ids)) :
                TinkerHelper.queryVertexIndex(this.getGraph(TinkerGraph.class), indexedContainer.key, indexedContainer.value).stream()
                        .filter(vertex -> ElementHelper.idExists(vertex.id(), this.ids) && HasContainer.testAll(vertex, this.hasContainers))
                        .collect(Collectors.<Vertex>toList()).iterator();
    }

    private HasContainer getIndexKey(final Class<? extends Element> indexedClass) {
        final Set<String> indexedKeys = this.getGraph(TinkerGraph.class).getIndexedKeys(indexedClass);
        return this.hasContainers.stream()
                .filter(c -> indexedKeys.contains(c.key) && c.predicate.equals(Compare.eq))
                .findAny()
                .orElseGet(() -> null);
    }

    public String toString() {
        if (this.hasContainers.isEmpty())
            return super.toString();
        else
            return 0 == this.ids.length ?
                    TraversalHelper.makeStepString(this, this.returnClass.getSimpleName().toLowerCase(), this.hasContainers) :
                    TraversalHelper.makeStepString(this, this.returnClass.getSimpleName().toLowerCase(), Arrays.toString(this.ids), this.hasContainers);
    }

    private final <E extends Element> Iterator<E> iteratorList(final Iterator<E> iterator) {
        final List<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            final E e = iterator.next();
            if (HasContainer.testAll(e, this.hasContainers))
                list.add(e);
        }
        return list.iterator();
    }

}
