package datahub.integration;

import com.linkedin.common.AuditStamp;
import com.linkedin.common.urn.DatasetUrn;
import datahub.integration.model.FieldNode;
import datahub.integration.model.Node;
import datahub.integration.model.SchemaEdge;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import org.jgrapht.GraphPath;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuperBuilder
@Getter
public abstract class SchemaContext<G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>,
        N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>,
        E extends SchemaEdge<N, S, F, E>> {

    public static final String FIELD_PATH_VERSION = "[version=2.0]";

    protected final DatasetUrn datasetUrn;
    protected final AuditStamp auditStamp;

    public S root() {
        return graph().root();
    }
    abstract protected C context();
    abstract public G graph();

    public <T, V extends SchemaVisitor<T, G, C, N, S, F, E>> Stream<T> accept(Collection<V> visitors) {
        return graph().accept(context(), visitors);
    }

    public Stream<GraphPath<N, E>> streamAllPaths(F field) {
        return graph().getAllPaths(root(), field).stream();
    }

    public String getFieldPath(GraphPath<N, E> path) {
        String fieldPathString = path.getEdgeList().stream()
                .flatMap(e -> Stream.of(e.getType(), e.edgeTarget().name()))
                .collect(Collectors.joining("."));
        return String.join(".", FIELD_PATH_VERSION, root().fieldPathType(), fieldPathString);
    }

    // This is because order matters for the frontend. Both for matching the protobuf field order
    // and also the nested struct's fieldPaths
    public Double calculateSortOrder(GraphPath<N, E> path, Class<F> fieldClass) {
        List<Integer> weights = path.getEdgeList().stream()
                .map(E::edgeTarget)
                .filter(fieldClass::isInstance)
                .map(f -> fieldClass.cast(f).fieldOrder())
                .collect(Collectors.toList());

        return IntStream.range(0, weights.size())
                .mapToDouble(i -> weights.get(i) * (1.0 / (i + 1)))
                .reduce(Double::sum)
                .orElse(0);
    }
}
