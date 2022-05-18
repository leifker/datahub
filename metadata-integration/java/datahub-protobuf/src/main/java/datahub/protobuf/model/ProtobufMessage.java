package datahub.protobuf.model;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.linkedin.schema.MapType;
import com.linkedin.schema.RecordType;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.SchemaNode;
import datahub.integration.model.SchemaGraph;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import com.linkedin.schema.SchemaFieldDataType;

import datahub.protobuf.ProtobufUtils;
import lombok.experimental.SuperBuilder;


@SuperBuilder(toBuilder = true)
public class ProtobufMessage extends SchemaNode<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> implements ProtobufElement {
    private final DescriptorProto messageProto;
    private final DescriptorProto parentMessageProto;
    private final FileDescriptorProto fileProto;

    @Override
    public String name() {
        return messageProto.getName();
    }

    @Override
    public String fullName() {
        if (parentMessageProto  != null) {
            return String.join(".", fileProto.getPackage(), parentMessageProto.getName(), name());
        }
        return String.join(".", fileProto.getPackage(), name());
    }

    @Override
    public String nativeType() {
        return fullName();
    }

    @Override
    public FileDescriptorProto fileProto() {
        return fileProto;
    }

    @Override
    public DescriptorProto messageProto() {
        return messageProto;
    }

    public SchemaFieldDataType schemaFieldDataType() {
        if (parentMessageProto != null && messageProto.getName().equals("MapFieldEntry")) {
            return new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new MapType()));
        }
        return new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new RecordType()));
    }

    @Override
    public long majorVersion() {
        return Integer.parseInt(Arrays.stream(fileProto.getName().split("/"))
                .filter(p -> p.matches("^v[0-9]+$"))
                .findFirst()
                .map(p -> p.replace("v", ""))
                .orElse("1"));
    }

    @Override
    public String description() {
        return messageLocations()
                .map(ProtobufUtils::collapseLocationComments)
                .findFirst().orElse("");
    }

    @Override
    public <T, G extends SchemaGraph<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>,
            V extends SchemaVisitor<T, G, C, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>,
            C extends SchemaContext<G, C, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>>
    Stream<T> accept(V visitor, C context) {
        return visitor.visitSchema(this, context);
    }

    @Override
    public String fieldPathType() {
        return Optional.ofNullable(fieldPathType).orElse(String.format("[type=%s]", nativeType().replace(".", "_")));
    }

    @Override
    public String toString() {
        return String.format("ProtobufMessage[%s]", fullName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProtobufMessage that = (ProtobufMessage) o;

        if (!fullName().equals(that.fullName())) {
            return false;
        }
        if (!messageProto.equals(that.messageProto)) {
            return false;
        }
        if (parentMessageProto != null ? !parentMessageProto.equals(that.parentMessageProto) : that.parentMessageProto != null) {
            return false;
        }
        return fileProto.equals(that.fileProto);
    }

    @Override
    public int hashCode() {
        int result = messageProto.hashCode();
        result = 31 * result + (parentMessageProto != null ? parentMessageProto.hashCode() : 0);
        result = 31 * result + fileProto.hashCode();
        result = 31 * result + fullName().hashCode();
        return result;
    }
}
