package datahub.protobuf.visitors.tag;

import com.linkedin.tag.TagProperties;
import datahub.protobuf.ProtobufContext;
import datahub.protobuf.visitors.tags.ProtobufTagVisitor;
import datahub.event.MetadataChangeProposalWrapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TagVisitorTest {

    @Test
    public void extendedMessageTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageA", "extended_protobuf.Person");
        ProtobufTagVisitor test = new ProtobufTagVisitor();
        assertEquals(Set.of(
                new TagProperties()
                        .setName("bool_feature")
                        .setDescription("meta.msg.bool_feature is true."),
                new TagProperties()
                        .setName("MetaEnumExample.ENTITY")
                        .setDescription("Enum MetaEnumExample.ENTITY of {UNKNOWN, ENTITY, EVENT}"),
                new TagProperties()
                        .setName("MetaEnumExample.EVENT")
                        .setDescription("Enum MetaEnumExample.EVENT of {UNKNOWN, ENTITY, EVENT}"),
                new TagProperties()
                        .setName("a")
                        .setDescription("meta.msg.tag_list"),
                new TagProperties()
                        .setName("b")
                        .setDescription("meta.msg.tag_list"),
                new TagProperties()
                        .setName("c")
                        .setDescription("meta.msg.tag_list"),
                new TagProperties()
                        .setName("repeat_string.a")
                        .setDescription("meta.msg.repeat_string"),
                new TagProperties()
                        .setName("repeat_string.b")
                        .setDescription("meta.msg.repeat_string"),
                new TagProperties()
                        .setName("deprecated")
                        .setColorHex("#FF0000")
        ), context.accept(List.of(test))
                .map(MetadataChangeProposalWrapper::getAspect)
                .collect(Collectors.toSet()));
    }

    @Test
    public void extendedFieldTest() throws IOException {
        ProtobufContext context = getContext("extended_protobuf", "messageB", "extended_protobuf.Person");
        Set<TagProperties> expectedTagProperties = Set.of(
                new TagProperties()
                        .setName("product_type_bool")
                        .setDescription("meta.fld.product_type_bool is true."),
                new TagProperties()
                        .setName("product_type.my type")
                        .setDescription("meta.fld.product_type"),
                new TagProperties()
                        .setName("MetaEnumExample.EVENT")
                        .setDescription("Enum MetaEnumExample.EVENT of {UNKNOWN, ENTITY, EVENT}"),
                new TagProperties()
                        .setName("d")
                        .setDescription("meta.fld.tag_list"),
                new TagProperties()
                        .setName("e")
                        .setDescription("meta.fld.tag_list"),
                new TagProperties()
                        .setName("f")
                        .setDescription("meta.fld.tag_list"),
                new TagProperties()
                        .setName("deprecated")
                        .setColorHex("#FF0000")
        );

        assertEquals(expectedTagProperties,
                context.accept(List.of(new ProtobufTagVisitor()))
                        .map(MetadataChangeProposalWrapper::getAspect)
                        .collect(Collectors.toSet()));
    }
}