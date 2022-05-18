package datahub.jsonschema.visitors;

import datahub.integration.SchemaVisitor;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.model.JSEdge;
import datahub.jsonschema.model.JSElement;
import datahub.jsonschema.model.JSField;
import datahub.jsonschema.model.JSGraph;
import datahub.jsonschema.model.JSSchema;


public interface JsonSchemaVisitor<T> extends SchemaVisitor<T, JSGraph, JsonSchemaContext, JSElement,
        JSSchema, JSField, JSEdge> {
}
