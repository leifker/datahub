package datahub.jsonschema.visitors.dataset;

import com.linkedin.common.urn.Urn;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;


public class DomainVisitor implements JsonSchemaVisitor<Urn> {

    @Override
    public Stream<Urn> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }
}
