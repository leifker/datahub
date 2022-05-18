package datahub.jsonschema.visitors.dataset;

import com.linkedin.common.TagAssociation;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;


public class TagAssociationVisitor implements JsonSchemaVisitor<TagAssociation> {

    @Override
    public Stream<TagAssociation> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }
}
