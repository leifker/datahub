package datahub.protobuf;

import datahub.integration.SchemaContext;
import datahub.protobuf.model.ProtobufEdge;
import datahub.protobuf.model.ProtobufElement;
import datahub.protobuf.model.ProtobufField;
import datahub.protobuf.model.ProtobufGraph;
import datahub.protobuf.model.ProtobufMessage;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Accessors(fluent = true)
@Getter
public class ProtobufContext extends SchemaContext<ProtobufGraph, ProtobufContext, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {
    private final ProtobufGraph graph;

    @Override
    protected ProtobufContext context() {
        return this;
    }
}
