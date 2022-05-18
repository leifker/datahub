package datahub.integration.model;

import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;

import java.util.stream.Stream;

/**
 * Node type which has two sub-types of nested and field. These are nodes within SchemaGraph.
 */
public interface Node<N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> {
    String name();
    String fullName();
    String nativeType();
    String description();
    String fieldPathType();

    default boolean isOneAnyOf() {
        return false;
    }

    <T, G extends SchemaGraph<N, S, F, E>, V extends SchemaVisitor<T, G, C, N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>>
    Stream<T> accept(V visitor, C context);
}
