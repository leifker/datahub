package datahub.jsonschema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedin.util.Pair;
import com.networknt.schema.JsonSchema;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;
import datahub.jsonschema.JSchemaUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@SuperBuilder
@Accessors(fluent = true)
@Getter
public class JSSchema extends SchemaNode<JSElement, JSSchema, JSField, JSEdge> implements JSElement {
    private final JSSchema parentSchema;
    private final JsonSchema jsonSchema;
    private final SchemaPathAt schemaPathAt;
    private final JsonNode jsonNode;
    private final boolean isOneAnyOf;

    @Override
    public <T, G extends SchemaGraph<JSElement, JSSchema, JSField, JSEdge>, V extends SchemaVisitor<T, G, C, JSElement, JSSchema, JSField, JSEdge>,
            C extends SchemaContext<G, C, JSElement, JSSchema, JSField, JSEdge>> Stream<T> accept(V visitor, C context) {
        return visitor.visitSchema(this, context);
    }

    @Override
    public String fullName() {
        return JSchemaUtils.id(jsonSchema, schemaPathAt);
    }

    @Override
    public String fieldPathType() {
        return Optional.ofNullable(fieldPathType)
                .orElse(String.format("[type=%s]", fullName().replace(".", "_")));
    }

    @Override
    public boolean isOneAnyOf() {
        return Optional.of(isOneAnyOf).orElse(super.isOneAnyOf());
    }

    @Override
    public String toString() {
        return String.format("JSSchema[%s]", fullName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSSchema jsSchema = (JSSchema) o;
        return jsonNode.equals(jsSchema.jsonNode) && Objects.equals(schemaPathAt, jsSchema.schemaPathAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonNode, schemaPathAt);
    }
}
