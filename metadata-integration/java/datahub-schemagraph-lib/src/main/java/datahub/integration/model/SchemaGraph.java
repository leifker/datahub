package datahub.integration.model;

import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class SchemaGraph<N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>,
        F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> extends DefaultDirectedGraph<N, E> {

    protected final transient AllDirectedPaths<N, E> directedPaths;

    public SchemaGraph(Class<E> clazz) {
        super(clazz);
        this.directedPaths = new AllDirectedPaths<>(this);
    }

    public <T, G extends SchemaGraph<N, S, F, E>, V extends SchemaVisitor<T, G, C, N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>> Stream<T>
    accept(C context, Collection<V> visitors) {
        return Stream.concat(
                visitors.stream().flatMap(visitor -> visitor.visitGraph(context)),
                vertexSet().stream().flatMap(vertex -> visitors.stream().flatMap(visitor -> vertex.accept(visitor, context)))
        );
    }

    public List<GraphPath<N, E>> getAllPaths(S a, F b) {
        return directedPaths.getAllPaths((N) a, (N) b, true, null);
    }

    abstract public S root();

    public String getFullName() {
        return root().fullName();
    }

    public long getMajorVersion() {
        return root().majorVersion();
    }

    public String getComment() {
        return root().description();
    }

    public void attachNestedSchemaFields(Class<F> fieldClass) {
        Map<String, List<F>> fieldMap = vertexSet().stream()
                .filter(v -> fieldClass.isInstance(v) && incomingEdgesOf(v).stream().noneMatch(e -> e.edgeSource().isOneAnyOf()))
                .map(fieldClass::cast)
                .collect(Collectors.groupingBy(F::parentSchemaName));
        edgeSet().stream().filter(E::isNestedType).collect(Collectors.toSet())
                .stream().map(e -> fieldClass.cast(e.edgeTarget()))
                .forEach(f -> attachNestedSchemaFields(fieldMap, f));
    }

    abstract protected void attachNestedSchemaFields(Map<String, List<F>> fieldMap, F nestedField);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SchemaGraph<?, ?, ?, ?> that = (SchemaGraph<?, ?, ?, ?>) o;

        return root().equals(that.root());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + root().hashCode();
        return result;
    }

    public String getHash() {
        return String.valueOf(super.hashCode());
    }
}
