package datahub.jsonschema.visitors.dataset;

import com.linkedin.common.Deprecation;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;


public class DeprecationVisitor implements JsonSchemaVisitor<Deprecation> {

    @Override
    public Stream<Deprecation> visitGraph(JsonSchemaContext context) {
        return Stream.empty();
    }
}
