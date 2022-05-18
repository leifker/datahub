package datahub.jsonschema.visitors.dataset;

import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;

public class DescriptionVisitor implements JsonSchemaVisitor<String> {

    @Override
    public Stream<String> visitGraph(JsonSchemaContext context) {
        return Stream.of(context.root().description());
    }
}
