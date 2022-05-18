package datahub.integration;


import datahub.integration.model.FieldNode;
import datahub.integration.model.Node;
import datahub.integration.model.SchemaEdge;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;

import java.util.stream.Stream;

/**
 *
 * @param <T> Type of object being emitted by the visitor
 * @param <S> Type of nested or root schema within a SchemaGraph
 * @param <F> Type of the fields within a SchemaGraph
 */
public interface SchemaVisitor<T, G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>,
        N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>,
        E extends SchemaEdge<N, S, F, E>> {
    default Stream<T> visitField(F field, C context) {
        return Stream.of();
    }
    default Stream<T> visitSchema(S schema, C context) {
        return Stream.of();
    }

    default Stream<T> visitGraph(C context) {
        return Stream.of();
    }
}
