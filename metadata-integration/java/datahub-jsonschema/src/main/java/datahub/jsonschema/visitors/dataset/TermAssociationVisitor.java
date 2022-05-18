package datahub.jsonschema.visitors.dataset;

import com.linkedin.common.GlossaryTermAssociation;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;

public class TermAssociationVisitor implements JsonSchemaVisitor<GlossaryTermAssociation> {

    @Override
    public Stream<GlossaryTermAssociation> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }
}
