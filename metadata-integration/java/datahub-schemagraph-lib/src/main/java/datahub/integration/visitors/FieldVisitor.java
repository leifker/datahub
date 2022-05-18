package datahub.integration.visitors;

import com.linkedin.schema.SchemaField;
import com.linkedin.util.Pair;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.*;

import java.util.stream.Stream;

public class FieldVisitor<G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>,
        N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>,
        E extends SchemaEdge<N, S, F, E>> implements SchemaVisitor<Pair<SchemaField, Double>, G, C, N, S, F, E>  {

    final private Class<F> fieldClass;

    public FieldVisitor(Class<F> fieldClass) {
        this.fieldClass = fieldClass;
    }

    @Override
    public Stream<Pair<SchemaField, Double>> visitField(F field, C context) {
        return context.streamAllPaths(field).map(path ->
                Pair.of(
                        new SchemaField()
                                .setFieldPath(context.getFieldPath(path))
                                .setNullable(field.nullable())
                                .setIsPartOfKey(field.isPrimaryKey())
                                .setDescription(field.description())
                                .setNativeDataType(field.nativeType())
                                .setType(field.schemaFieldDataType()),
                        context.calculateSortOrder(path, fieldClass)));
    }
}
