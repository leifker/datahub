package datahub.jsonschema.visitors.tags;

import com.linkedin.data.template.RecordTemplate;
import datahub.event.MetadataChangeProposalWrapper;
import datahub.integration.visitors.TagVisitor;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.model.JSEdge;
import datahub.jsonschema.model.JSElement;
import datahub.jsonschema.model.JSField;
import datahub.jsonschema.model.JSGraph;
import datahub.jsonschema.model.JSSchema;

import java.util.stream.Stream;

public class JsonSchemaTagVisitor extends TagVisitor<JSGraph, JsonSchemaContext, JSElement, JSSchema, JSField, JSEdge> {
    @Override
    public Stream<MetadataChangeProposalWrapper<? extends RecordTemplate>> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }

    @Override
    public Stream<MetadataChangeProposalWrapper<? extends RecordTemplate>> visitField(JSField field, JsonSchemaContext context) {
        return Stream.of();
    }
}