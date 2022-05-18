package datahub.jsonschema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedin.data.template.StringArray;
import com.linkedin.schema.ArrayType;
import com.linkedin.schema.BooleanType;
import com.linkedin.schema.BytesType;
import com.linkedin.schema.EnumType;
import com.linkedin.schema.NumberType;
import com.linkedin.schema.RecordType;
import com.linkedin.schema.SchemaFieldDataType;
import com.linkedin.schema.StringType;
import com.linkedin.schema.UnionType;
import com.linkedin.util.Pair;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.FieldNode;
import datahub.integration.model.SchemaGraph;
import datahub.jsonschema.JSchemaUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@Getter
public class JSField extends FieldNode<JSElement, JSSchema, JSField, JSEdge> implements JSElement {
    private final JSSchema parentSchema;
    private final JsonNode jsonNode;
    private final SchemaPathAt schemaPathAt;
    private final SchemaFieldDataType schemaFieldDataType;
    private final Boolean isNestedType;
    private final boolean nullable;
    private final boolean isOneAnyOf;

    @Override
    public <T, G extends SchemaGraph<JSElement, JSSchema, JSField, JSEdge>,
            V extends SchemaVisitor<T, G, C, JSElement, JSSchema, JSField, JSEdge>,
            C extends SchemaContext<G, C, JSElement, JSSchema, JSField, JSEdge>> Stream<T> accept(V visitor, C context) {
        return visitor.visitField(this, context);
    }

    @Override
    public String fullName() {
        return JSchemaUtils.id(parentSchema.jsonSchema(), schemaPathAt);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public SchemaFieldDataType schemaFieldDataType() {
        if (schemaFieldDataType != null) {
            return schemaFieldDataType;
        } else {
            return JSchemaUtils.extractTypeJsonNode(jsonNode).map(jsonNodeCollapsed -> {
                final SchemaFieldDataType.Type fieldType;
                switch (jsonNodeCollapsed.getNodeType()) {
                    case NUMBER:
                        fieldType = SchemaFieldDataType.Type.create(new NumberType());
                        break;
                    case OBJECT:
                        if (jsonNodeCollapsed.has("enum")) {
                            fieldType = SchemaFieldDataType.Type.create(new EnumType());
                        } else if (jsonNode.has("oneOf")) {
                            fieldType = SchemaFieldDataType.Type.create(new UnionType());
                        } else if (!jsonNodeCollapsed.get("type").asText().equals("array")) {
                            switch (jsonNodeCollapsed.get("type").asText()) {
                                case "integer":
                                case "number":
                                    fieldType = SchemaFieldDataType.Type.create(new NumberType());
                                    break;
                                case "string":
                                    fieldType = SchemaFieldDataType.Type.create(new StringType());
                                    break;
                                case "boolean":
                                    fieldType = SchemaFieldDataType.Type.create(new BooleanType());
                                    break;
                                case "object":
                                    fieldType = SchemaFieldDataType.Type.create(new RecordType());
                                    break;
                                default:
                                    throw new IllegalStateException(String.format("Unexpected SchemaFieldDataType type => %s", jsonNodeCollapsed));
                            }
                        } else {
                            fieldType = SchemaFieldDataType.Type.create(new ArrayType()
                                    .setNestedType(new StringArray()));
                        }
                        break;
                    case BINARY:
                        fieldType = SchemaFieldDataType.Type.create(new BytesType());
                        break;
                    case BOOLEAN:
                        fieldType = SchemaFieldDataType.Type.create(new BooleanType());
                        break;
                    case STRING:
                        fieldType = SchemaFieldDataType.Type.create(new StringType());
                        break;
                    case ARRAY:
                        fieldType = SchemaFieldDataType.Type.create(new ArrayType()
                                .setNestedType(new StringArray()));
                        break;
                    default:
                        throw new IllegalStateException(String.format("Unexpected JsonSchema NodeType => SchemaFieldDataType: %s", jsonNodeCollapsed.getNodeType()));
                }
                return new SchemaFieldDataType().setType(fieldType);
            }).orElseThrow(() -> new IllegalStateException(String.format("Unable to determine SchemaFieldDataType: %s", jsonNode)));
        }
    }

    @Override
    public String fieldPathType() {
        return Optional.ofNullable(fieldPathType)
                .orElseGet(() -> {
                    LinkedList<String> typeList = new LinkedList<>();
                    getFieldPathType(jsonNode, typeList);
                    return typeList.stream().map(t -> String.format("[type=%s]", t)).collect(Collectors.joining("."));
                });
    }

    private void getFieldPathType(JsonNode jsonNode, List<String> typeList) {
        Optional<JsonNode> withType = JSchemaUtils.extractTypeJsonNode(jsonNode);

        withType.ifPresent(jsonNodeWithType -> {
            if (jsonNodeWithType.has("enum")) {
                typeList.add("enum");
            } else if (jsonNodeWithType.has("oneOf")) {
                typeList.add("union");
            } else if(!jsonNodeWithType.get("type").asText().equals("array")) {
                final String type;
                switch (jsonNodeWithType.get("type").asText()) {
                    case "integer":
                        type = "int";
                        break;
                    case "number":
                        type = "double";
                        break;
                    case "string":
                    case "boolean":
                        type = jsonNodeWithType.get("type").asText();
                        break;
                    case "object":
                        type = fullName().replace(".", "_");
                        break;
                    default:
                        throw new IllegalStateException(String.format("Unexpected JsonSchema type => %s", jsonNodeWithType));
                }
                typeList.add(type);
            } else {
                typeList.add("array");
                getFieldPathType(jsonNodeWithType.get("items"), typeList);
            }
        });
    }

    // TODO get actual order
    @Override
    public int fieldOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("JSField[%s]", fullName());
    }

    @Override
    public boolean isOneAnyOf() {
        return Optional.of(isOneAnyOf).orElse(super.isOneAnyOf());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JSElement that = (JSElement) o;

        return fullName().equals(that.fullName());
    }

    @Override
    public int hashCode() {
        return fullName().hashCode();
    }
}
