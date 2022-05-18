package datahub.jsonschema.visitors.dataset;

import com.linkedin.dataset.DatasetProperties;
import datahub.jsonschema.JsonSchemaContext;
import datahub.jsonschema.visitors.JsonSchemaVisitor;

import java.util.stream.Stream;


public class PropertyVisitor implements JsonSchemaVisitor<DatasetProperties> {

    @Override
    public Stream<DatasetProperties> visitGraph(JsonSchemaContext context) {
        return Stream.of();
    }
}
