package datahub.protobuf.visitors.field;

import com.linkedin.common.GlobalTags;
import com.linkedin.common.GlossaryTermAssociation;
import com.linkedin.common.GlossaryTermAssociationArray;
import com.linkedin.common.GlossaryTerms;
import com.linkedin.common.TagAssociation;
import com.linkedin.common.TagAssociationArray;
import com.linkedin.common.urn.TagUrn;
import com.linkedin.schema.SchemaField;
import com.linkedin.tag.TagProperties;
import com.linkedin.util.Pair;
import datahub.integration.visitors.FieldVisitor;
import datahub.protobuf.model.*;
import datahub.protobuf.visitors.ProtobufExtensionUtil;
import datahub.protobuf.ProtobufContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static datahub.protobuf.ProtobufUtils.getFieldOptions;
import static datahub.protobuf.ProtobufUtils.getMessageOptions;

public class ProtobufExtensionFieldVisitor extends FieldVisitor<ProtobufGraph, ProtobufContext, ProtobufElement,
        ProtobufMessage, ProtobufField, ProtobufEdge> {

    public ProtobufExtensionFieldVisitor() {
        super(ProtobufField.class);
    }

    @Override
    public Stream<Pair<SchemaField, Double>> visitField(ProtobufField field, ProtobufContext context) {
        List<TagAssociation> tags = Stream.concat(
                ProtobufExtensionUtil.extractTagPropertiesFromOptions(
                        getFieldOptions(field.fieldProto()),
                        context.graph().getRegistry()),
                        promotedTags(field, context))
                .distinct().map(tag -> new TagAssociation().setTag(new TagUrn(tag.getName())))
                .sorted(Comparator.comparing(t -> t.getTag().getName()))
                .collect(Collectors.toList());

        List<GlossaryTermAssociation> terms =  Stream.concat(
                ProtobufExtensionUtil.extractTermAssociationsFromOptions(
                                getFieldOptions(field.fieldProto()), context.graph().getRegistry()),
                promotedTerms(field, context))
                .distinct()
                .sorted(Comparator.comparing(a -> a.getUrn().getNameEntity()))
                .collect(Collectors.toList());

        return context.streamAllPaths(field).map(path -> Pair.of(
                new SchemaField()
                        .setFieldPath(context.getFieldPath(path))
                        .setNullable(field.nullable())
                        .setIsPartOfKey(field.isPrimaryKey())
                        .setDescription(field.description())
                        .setNativeDataType(field.nativeType())
                        .setType(field.schemaFieldDataType())
                        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray(tags)))
                        .setGlossaryTerms(new GlossaryTerms()
                                .setTerms(new GlossaryTermAssociationArray(terms))
                                .setAuditStamp(context.getAuditStamp())),
                context.calculateSortOrder(path, ProtobufField.class)));
    }

    /**
     * Promote tags from nested message to field.
     * @return tags
     */
    private Stream<TagProperties> promotedTags(ProtobufField field, ProtobufContext context) {
        if (field.isMessage()) {
            return context.graph().outgoingEdgesOf(field).stream().flatMap(e ->
                    ProtobufExtensionUtil.extractTagPropertiesFromOptions(getMessageOptions(e.edgeTarget().messageProto()),
                            context.graph().getRegistry())
            ).distinct();
        } else {
            return Stream.of();
        }
    }

    /**
     * Promote terms from nested message to field.
     * @return terms
     */
    private Stream<GlossaryTermAssociation> promotedTerms(ProtobufField field, ProtobufContext context) {
        if (field.isMessage()) {
            return context.graph().outgoingEdgesOf(field).stream().flatMap(e ->
                    ProtobufExtensionUtil.extractTermAssociationsFromOptions(getMessageOptions(e.edgeTarget().messageProto()),
                            context.graph().getRegistry())
            ).distinct();
        } else {
            return Stream.of();
        }
    }

}
