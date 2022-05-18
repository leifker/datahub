package datahub.protobuf.visitors.dataset;

import com.linkedin.common.GlossaryTermAssociation;
import datahub.protobuf.visitors.ProtobufVisitor;
import datahub.protobuf.visitors.ProtobufExtensionUtil;
import datahub.protobuf.ProtobufContext;

import java.util.stream.Stream;

import static datahub.protobuf.ProtobufUtils.getMessageOptions;

public class TermAssociationVisitor implements ProtobufVisitor<GlossaryTermAssociation> {

    @Override
    public Stream<GlossaryTermAssociation> visitGraph(ProtobufContext context) {
        return ProtobufExtensionUtil.extractTermAssociationsFromOptions(getMessageOptions(context.root().messageProto()),
                context.graph().getRegistry());
    }
}
