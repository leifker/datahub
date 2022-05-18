package datahub.integration.model;

import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

/**
 * Node type represents a nested field
 */
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
abstract public class SchemaNode<N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>,
        F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> implements Node<N, S, F, E> {

    protected final String fieldPathType;

    public long majorVersion() {
        return Long.parseLong(Arrays.stream(fullName().split("[.]"))
                .filter(p -> p.matches("^v[0-9]+$"))
                .findFirst()
                .map(p -> p.replace("v", ""))
                .orElse("1"));
    }
}
