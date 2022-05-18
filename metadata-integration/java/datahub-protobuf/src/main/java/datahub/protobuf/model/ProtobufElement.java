package datahub.protobuf.model;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo;
import datahub.integration.model.Node;

import java.util.List;
import java.util.stream.Stream;


public interface ProtobufElement extends Node<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {
    FileDescriptorProto fileProto();
    DescriptorProto messageProto();

    default Stream<SourceCodeInfo.Location> messageLocations() {
        List<SourceCodeInfo.Location> fileLocations = fileProto().getSourceCodeInfo().getLocationList();
        return fileLocations.stream()
                .filter(loc -> loc.getPathCount() > 1
                        && loc.getPath(0) == FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER
                        && messageProto() == fileProto().getMessageType(loc.getPath(1)));
    }
}
