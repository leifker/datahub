package datahub.protobuf.visitors;

import datahub.protobuf.ProtobufContext;
import datahub.protobuf.model.ProtobufEdge;
import datahub.protobuf.model.ProtobufElement;
import datahub.protobuf.model.ProtobufField;
import datahub.protobuf.model.ProtobufGraph;
import org.jgrapht.GraphPath;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.getGraph;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProtobufContextTest {

    @Test
    public void duplicateNestedTest() throws IOException {
        ProtobufGraph graph = getGraph("protobuf", "messageB");
        ProtobufContext test = ProtobufContext.builder().graph(graph).build();

        List<ProtobufField> nestedMessages = graph.vertexSet().stream().filter(f -> f.name().endsWith("nested"))
                .map(f -> (ProtobufField) f)
                .collect(Collectors.toList());

        List<GraphPath<ProtobufElement, ProtobufEdge>> nestedPathsA = graph.getAllPaths(graph.root(), nestedMessages.get(0));
        List<GraphPath<ProtobufElement, ProtobufEdge>> nestedPathsB = graph.getAllPaths(graph.root(), nestedMessages.get(1));
        assertNotEquals(nestedPathsA, nestedPathsB);

        Set<String> fieldPathsA = nestedPathsA.stream().map(test::getFieldPath).collect(Collectors.toSet());
        Set<String> fieldPathsB = nestedPathsB.stream().map(test::getFieldPath).collect(Collectors.toSet());
        assertNotEquals(fieldPathsA, fieldPathsB);
    }
}
