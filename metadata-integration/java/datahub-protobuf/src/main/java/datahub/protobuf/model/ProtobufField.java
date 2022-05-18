package datahub.protobuf.model;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;
import com.linkedin.data.template.StringArray;
import com.linkedin.schema.ArrayType;
import com.linkedin.schema.BooleanType;
import com.linkedin.schema.BytesType;
import com.linkedin.schema.EnumType;
import com.linkedin.schema.FixedType;
import com.linkedin.schema.NumberType;
import com.linkedin.schema.RecordType;
import com.linkedin.schema.SchemaFieldDataType;
import com.linkedin.schema.StringType;
import com.linkedin.util.Pair;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.FieldNode;
import datahub.integration.model.SchemaGraph;
import datahub.protobuf.ProtobufUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static datahub.protobuf.ProtobufUtils.getFieldOptions;


@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@Getter
public class ProtobufField extends FieldNode<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> implements ProtobufElement {
    private final ProtobufMessage parentSchema;
    private final FieldDescriptorProto fieldProto;
    private final String nativeType;
    private final Boolean isNestedType;
    private final SchemaFieldDataType schemaFieldDataType;

    public OneofDescriptorProto oneOfProto() {
        if (fieldProto.hasOneofIndex()) {
            return parentSchema.messageProto().getOneofDecl(fieldProto.getOneofIndex());
        }
        return null;
    }

    @Override
    public boolean nullable() {
        return !isPrimaryKey();
    }

    @Override
    public boolean isPrimaryKey() {
        return getFieldOptions(fieldProto).stream().map(Pair::getKey)
                .anyMatch(fieldDesc -> fieldDesc.getName().matches("(?i).*primary_?key"));
    }

    @Override
    public FileDescriptorProto fileProto() {
        return parentSchema.fileProto();
    }

    @Override
    public DescriptorProto messageProto() {
        return parentSchema.messageProto();
    }

    @Override
    public String name() {
        return fieldProto.getName();
    }

    @Override
    public String fullName() {
        return String.join(".", parentSchemaName(), name());
    }

    public int getNumber() { 
        return fieldProto.getNumber(); 
    }

    @Override
    public String nativeType() {
        return Optional.ofNullable(nativeType).orElseGet(() -> {
            if (fieldProto.getTypeName().isEmpty()) {
                return fieldProto.getType().name().split("_")[1].toLowerCase();
            } else {
                return fieldProto.getTypeName().replaceFirst("^[.]", "");
            }
        });
    }

    @Override
    public String fieldPathType() {
        return Optional.ofNullable(fieldPathType).orElseGet(() -> {
            final String pathType;

            switch (fieldProto.getType()) {
                case TYPE_DOUBLE:
                    pathType = "double";
                    break;
                case TYPE_FLOAT:
                    pathType = "float";
                    break;
                case TYPE_SFIXED64:
                case TYPE_FIXED64:
                case TYPE_UINT64:
                case TYPE_INT64:
                case TYPE_SINT64:
                    pathType = "long";
                    break;
                case TYPE_FIXED32:
                case TYPE_SFIXED32:
                case TYPE_INT32:
                case TYPE_UINT32:
                case TYPE_SINT32:
                    pathType = "int";
                    break;
                case TYPE_BYTES:
                    pathType = "bytes";
                    break;
                case TYPE_ENUM:
                    pathType = "enum";
                    break;
                case TYPE_BOOL:
                    pathType = "boolean";
                    break;
                case TYPE_STRING:
                    pathType = "string";
                    break;
                case TYPE_GROUP:
                case TYPE_MESSAGE:
                    pathType = nativeType().replace(".", "_");
                    break;
                default:
                    throw new IllegalStateException(String.format("Unexpected FieldDescriptorProto => FieldPathType %s", fieldProto.getType()));
            }

            StringArray fieldPath = new StringArray();

            if (schemaFieldDataType().getType().isArrayType()) {
                fieldPath.add("[type=array]");
            }

            fieldPath.add(String.format("[type=%s]", pathType));

            return String.join(".", fieldPath);
        });
    }

    public boolean isMessage() {
        return Optional.ofNullable(isNestedType).orElseGet(() ->
                    fieldProto.getType().equals(FieldDescriptorProto.Type.TYPE_MESSAGE));
    }

    @Override
    public int fieldOrder() {
        return messageProto().getFieldList().indexOf(fieldProto) + 1;
    }

    @Override
    public SchemaFieldDataType schemaFieldDataType() throws IllegalStateException {
        return Optional.ofNullable(schemaFieldDataType).orElseGet(() -> {
            final SchemaFieldDataType.Type fieldType;

            switch (fieldProto.getType()) {
                case TYPE_DOUBLE:
                case TYPE_FLOAT:
                case TYPE_INT64:
                case TYPE_UINT64:
                case TYPE_INT32:
                case TYPE_UINT32:
                case TYPE_SINT32:
                case TYPE_SINT64:
                    fieldType = SchemaFieldDataType.Type.create(new NumberType());
                    break;
                case TYPE_GROUP:
                case TYPE_MESSAGE:
                    fieldType = SchemaFieldDataType.Type.create(new RecordType());
                    break;
                case TYPE_BYTES:
                    fieldType = SchemaFieldDataType.Type.create(new BytesType());
                    break;
                case TYPE_ENUM:
                    fieldType = SchemaFieldDataType.Type.create(new EnumType());
                    break;
                case TYPE_BOOL:
                    fieldType = SchemaFieldDataType.Type.create(new BooleanType());
                    break;
                case TYPE_STRING:
                    fieldType = SchemaFieldDataType.Type.create(new StringType());
                    break;
                case TYPE_FIXED64:
                case TYPE_FIXED32:
                case TYPE_SFIXED32:
                case TYPE_SFIXED64:
                    fieldType = SchemaFieldDataType.Type.create(new FixedType());
                    break;
                default:
                    throw new IllegalStateException(String.format("Unexpected FieldDescriptorProto => SchemaFieldDataType: %s", fieldProto.getType()));
            }

            if (fieldProto.getLabel().equals(FieldDescriptorProto.Label.LABEL_REPEATED)) {
                return new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new ArrayType()
                        .setNestedType(new StringArray())));
            }

            return new SchemaFieldDataType().setType(fieldType);
        });
    }

    @Override
    public String description() {
        return messageLocations()
                .filter(loc -> loc.getPathCount() > 3
                        && loc.getPath(2) == DescriptorProto.FIELD_FIELD_NUMBER
                        && fieldProto == messageProto().getField(loc.getPath(3)))
                .map(ProtobufUtils::collapseLocationComments)
                .collect(Collectors.joining("\n"))
                .trim();
    }

    @Override
    public <T, G extends SchemaGraph<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>,
            V extends SchemaVisitor<T, G, C, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>,
            C extends SchemaContext<G, C, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge>>
    Stream<T> accept(V visitor, C context) {
        return visitor.visitField(this, context);
    }

    @Override
    public String toString() {
        return String.format("ProtobufField[%s]", fullName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProtobufElement that = (ProtobufElement) o;

        return fullName().equals(that.fullName());
    }

    @Override
    public int hashCode() {
        return fullName().hashCode();
    }
}
