package datahub.protobuf.visitors.tags;

import com.linkedin.data.template.RecordTemplate;
import datahub.integration.visitors.TagVisitor;
import datahub.protobuf.model.ProtobufEdge;
import datahub.protobuf.model.ProtobufElement;
import datahub.protobuf.model.ProtobufField;
import datahub.protobuf.model.ProtobufGraph;
import datahub.protobuf.model.ProtobufMessage;
import datahub.protobuf.visitors.ProtobufExtensionUtil;
import datahub.protobuf.ProtobufContext;
import datahub.event.MetadataChangeProposalWrapper;

import static datahub.protobuf.ProtobufUtils.getFieldOptions;
import static datahub.protobuf.ProtobufUtils.getMessageOptions;

import java.util.stream.Stream;

public class ProtobufTagVisitor extends TagVisitor<ProtobufGraph, ProtobufContext, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {
    @Override
    public Stream<MetadataChangeProposalWrapper<? extends RecordTemplate>> visitGraph(ProtobufContext context) {
        return ProtobufExtensionUtil.extractTagPropertiesFromOptions(getMessageOptions(context.root().messageProto()),
                        context.graph().getRegistry())
                .map(TagVisitor::wrapTagProperty);
    }

    @Override
    public Stream<MetadataChangeProposalWrapper<? extends RecordTemplate>> visitField(ProtobufField field, ProtobufContext context) {
        return ProtobufExtensionUtil.extractTagPropertiesFromOptions(getFieldOptions(field.fieldProto()),
                        context.graph().getRegistry())
                .map(TagVisitor::wrapTagProperty);
    }
}
