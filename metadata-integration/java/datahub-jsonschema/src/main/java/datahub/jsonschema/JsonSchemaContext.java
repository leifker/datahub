package datahub.jsonschema;

import datahub.integration.SchemaContext;
import datahub.jsonschema.model.JSEdge;
import datahub.jsonschema.model.JSElement;
import datahub.jsonschema.model.JSField;
import datahub.jsonschema.model.JSGraph;
import datahub.jsonschema.model.JSSchema;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Accessors(fluent = true)
@Getter
public class JsonSchemaContext extends SchemaContext<JSGraph, JsonSchemaContext, JSElement, JSSchema, JSField, JSEdge> {

    private final JSGraph graph;

    @Override
    protected JsonSchemaContext context() {
        return this;
    }
}
