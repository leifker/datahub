package datahub.jsonschema.model;

import com.fasterxml.jackson.databind.JsonNode;
import datahub.integration.model.Node;
import datahub.jsonschema.JSchemaUtils;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface JSElement extends Node<JSElement, JSSchema, JSField, JSEdge> {
    JsonNode jsonNode();
    SchemaPathAt schemaPathAt();
    JSSchema parentSchema();

    @Override
    default String description() {
        return Stream.of(
                    JSchemaUtils.lookup(jsonNode(), "title"),
                    JSchemaUtils.lookup(jsonNode(), "description"),
                    JSchemaUtils.lookup(jsonNode(), "$comment")
                ).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.joining("\n"));
    }


    @Override
    default String nativeType() {
        return JSchemaUtils.nativeType(parentSchema().jsonSchema(), jsonNode(), schemaPathAt());
    }

    @Override
    default String name() {
        return JSchemaUtils.name(fullName());
    }
}
