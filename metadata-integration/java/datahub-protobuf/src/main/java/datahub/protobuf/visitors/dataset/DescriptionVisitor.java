package datahub.protobuf.visitors.dataset;

import datahub.protobuf.visitors.ProtobufVisitor;
import datahub.protobuf.ProtobufContext;

import java.util.stream.Stream;

public class DescriptionVisitor implements ProtobufVisitor<String> {

    @Override
    public Stream<String> visitGraph(ProtobufContext context) {
        return Stream.of(context.root().description());
    }
}
