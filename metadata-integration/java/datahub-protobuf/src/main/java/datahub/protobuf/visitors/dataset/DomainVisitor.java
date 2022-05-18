package datahub.protobuf.visitors.dataset;

import com.linkedin.common.urn.Urn;
import com.linkedin.util.Pair;
import datahub.protobuf.visitors.ProtobufExtensionUtil;
import datahub.protobuf.visitors.ProtobufVisitor;
import datahub.protobuf.ProtobufContext;

import java.util.stream.Stream;

import static datahub.protobuf.ProtobufUtils.getMessageOptions;

public class DomainVisitor implements ProtobufVisitor<Urn> {

    @Override
    public Stream<Urn> visitGraph(ProtobufContext context) {
        return ProtobufExtensionUtil.filterByDataHubType(getMessageOptions(context.root().messageProto()),
                        context.graph().getRegistry(), ProtobufExtensionUtil.DataHubMetadataType.DOMAIN)
                .stream().map(Pair::getValue).map(o ->
                    Urn.createFromTuple("domain", ((String) o).toLowerCase())
                );
    }
}
