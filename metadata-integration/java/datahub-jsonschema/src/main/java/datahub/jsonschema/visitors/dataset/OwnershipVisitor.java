package datahub.jsonschema.visitors.dataset;

import com.linkedin.common.Owner;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;


public class OwnershipVisitor implements JsonSchemaVisitor<Owner> {

    @Override
    public Stream<Owner> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }
}
