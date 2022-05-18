package datahub.protobuf.visitors;

import datahub.integration.SchemaVisitor;
import datahub.protobuf.ProtobufContext;
import datahub.protobuf.model.ProtobufEdge;
import datahub.protobuf.model.ProtobufElement;
import datahub.protobuf.model.ProtobufField;
import datahub.protobuf.model.ProtobufGraph;
import datahub.protobuf.model.ProtobufMessage;


public interface ProtobufVisitor<T> extends SchemaVisitor<T, ProtobufGraph, ProtobufContext, ProtobufElement,
        ProtobufMessage, ProtobufField, ProtobufEdge> {
}
