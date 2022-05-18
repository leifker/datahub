package datahub.protobuf.visitors.dataset;

import com.linkedin.common.urn.Urn;
import datahub.protobuf.ProtobufContext;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class DomainVisitorTest {

    @Test
    public void visitorTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageA", "extended_protobuf.MessageA");

        DomainVisitor test = new DomainVisitor();

        assertEquals(Set.of(Urn.createFromTuple("domain", "engineering")),
                context.accept(List.of(test)).collect(Collectors.toSet()));
    }
}
