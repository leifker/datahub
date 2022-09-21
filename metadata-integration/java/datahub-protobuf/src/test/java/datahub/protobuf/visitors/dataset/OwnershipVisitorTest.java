package datahub.protobuf.visitors.dataset;

import com.linkedin.common.Owner;
import com.linkedin.common.OwnershipSource;
import com.linkedin.common.OwnershipSourceType;
import com.linkedin.common.OwnershipType;
import com.linkedin.common.urn.Urn;
import datahub.protobuf.ProtobufContext;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class OwnershipVisitorTest {

    @Test
    public void visitorTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageA", "extended_protobuf.MessageA");

        OwnershipVisitor test = new OwnershipVisitor();

        assertEquals(Set.of(new Owner()
                                .setType(OwnershipType.TECHNICAL_OWNER)
                                .setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL))
                                .setOwner(Urn.createFromTuple("corpGroup", "teamb")),
                        new Owner()
                                .setType(OwnershipType.TECHNICAL_OWNER)
                                .setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL))
                                .setOwner(Urn.createFromTuple("corpuser", "datahub")),
                        new Owner()
                                .setType(OwnershipType.TECHNICAL_OWNER)
                                .setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL))
                                .setOwner(Urn.createFromTuple("corpGroup", "technicalowner"))
                ),
                context.accept(List.of(test)).collect(Collectors.toSet()));
    }

    @Test
    public void visitorSingleOwnerTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageB", "extended_protobuf.MessageB");

        OwnershipVisitor test = new OwnershipVisitor();

        assertEquals(Set.of(new Owner()
                                .setType(OwnershipType.DATA_STEWARD)
                                .setSource(new OwnershipSource().setType(OwnershipSourceType.MANUAL))
                                .setOwner(Urn.createFromTuple("corpuser", "datahub"))
                ),
                context.accept(List.of(test)).collect(Collectors.toSet()));
    }
}
