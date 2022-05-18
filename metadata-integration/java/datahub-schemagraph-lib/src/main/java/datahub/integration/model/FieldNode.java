package datahub.integration.model;

import com.linkedin.schema.SchemaFieldDataType;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;


/**
 * Node type which represents a non-nested field
 */
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@AllArgsConstructor
abstract public class FieldNode<N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>,
        F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> implements Node<N, S, F, E> {

    protected final String fieldPathType;

    abstract public S parentSchema();
    abstract public int fieldOrder();
    abstract public Boolean isNestedType();
    abstract public boolean nullable();
    abstract public boolean isPrimaryKey();
    abstract public SchemaFieldDataType schemaFieldDataType();

    public String parentSchemaName() {
        return parentSchema().fullName();
    }
}
