package datahub.protobuf.visitors.dataset;

import com.linkedin.common.TagAssociation;
import com.linkedin.common.urn.TagUrn;
import datahub.protobuf.visitors.ProtobufVisitor;
import datahub.protobuf.visitors.ProtobufExtensionUtil;
import datahub.protobuf.ProtobufContext;

import java.util.stream.Stream;

import static datahub.protobuf.ProtobufUtils.getMessageOptions;


public class TagAssociationVisitor implements ProtobufVisitor<TagAssociation> {

    @Override
    public Stream<TagAssociation> visitGraph(ProtobufContext context) {
        return ProtobufExtensionUtil.extractTagPropertiesFromOptions(getMessageOptions(context.root().messageProto()),
                        context.graph().getRegistry())
                .map(tag -> new TagAssociation().setTag(new TagUrn(tag.getName())));
    }
}
