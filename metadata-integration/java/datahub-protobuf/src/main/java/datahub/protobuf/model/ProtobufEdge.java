package datahub.protobuf.model;

import datahub.integration.model.SchemaEdge;
import lombok.Builder;
import lombok.Getter;
import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;

@Builder
@Getter
public class ProtobufEdge extends DefaultEdge implements SchemaEdge<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {

    @Builder.Default
    protected final String type = "";
    @Builder.Default
    protected final boolean isMessageType = false;

    private final ProtobufElement source;
    private final ProtobufElement target;

    @Override
    public ProtobufElement edgeTarget() {
        return target;
    }
    @Override
    public ProtobufElement edgeSource() {
        return source;
    }
    @Override
    public boolean isNestedType() {
        return isMessageType;
    }

    @Override
    public ProtobufEdge getInstance() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtobufEdge that = (ProtobufEdge) o;
        return isMessageType == that.isMessageType && type.equals(that.type) && source.equals(that.source) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isMessageType, source, target);
    }
}
