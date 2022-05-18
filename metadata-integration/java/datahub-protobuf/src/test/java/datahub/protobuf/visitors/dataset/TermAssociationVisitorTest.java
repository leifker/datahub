package datahub.protobuf.visitors.dataset;

import com.linkedin.common.GlossaryTermAssociation;
import com.linkedin.common.urn.GlossaryTermUrn;
import datahub.protobuf.ProtobufContext;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TermAssociationVisitorTest {

    @Test
    public void extendedMessageTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageA", "extended_protobuf.Person");
        TermAssociationVisitor test = new TermAssociationVisitor();
        assertEquals(Set.of(
                    new GlossaryTermAssociation().setUrn(new GlossaryTermUrn("a")),
                    new GlossaryTermAssociation().setUrn(new GlossaryTermUrn("b")),
                    new GlossaryTermAssociation().setUrn(new GlossaryTermUrn("MetaEnumExample.ENTITY")),
                    new GlossaryTermAssociation().setUrn(new GlossaryTermUrn("MetaEnumExample.EVENT")),
                    new GlossaryTermAssociation().setUrn(new GlossaryTermUrn("Classification.HighlyConfidential"))
                ),
                context.accept(List.of(test)).collect(Collectors.toSet()));
    }

    @Test
    public void extendedFieldTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageB", "extended_protobuf.Person");
        TermAssociationVisitor test = new TermAssociationVisitor();
        assertEquals(Set.of(),
                context.accept(List.of(test)).collect(Collectors.toSet()));
    }
}
