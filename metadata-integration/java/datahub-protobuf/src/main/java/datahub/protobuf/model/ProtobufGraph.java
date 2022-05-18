package datahub.protobuf.model;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import datahub.integration.model.SchemaGraph;
import datahub.protobuf.ProtobufUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ProtobufGraph extends SchemaGraph<ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {
    private final transient ProtobufMessage rootProtobufMessage;
    private final transient ExtensionRegistry registry;

    public ProtobufGraph(DescriptorProtos.FileDescriptorSet fileSet) throws InvalidProtocolBufferException {
        this(fileSet, null, null, true);
    }

    public ProtobufGraph(DescriptorProtos.FileDescriptorSet fileSet, String messageName) throws InvalidProtocolBufferException {
        this(fileSet, messageName, null, true);
    }

    public ProtobufGraph(DescriptorProtos.FileDescriptorSet fileSet, String messageName, String relativeFilename) throws InvalidProtocolBufferException {
        this(fileSet, messageName, relativeFilename, true);
    }

    public ProtobufGraph(DescriptorProtos.FileDescriptorSet fileSet, String messageName, String filename,
                         boolean flattenGoogleWrapped) throws InvalidProtocolBufferException {
        super(ProtobufEdge.class);
        this.registry = ProtobufUtils.buildRegistry(fileSet);
        DescriptorProtos.FileDescriptorSet fileSetExtended = DescriptorProtos.FileDescriptorSet
                .parseFrom(fileSet.toByteArray(), this.registry);
        buildProtobufGraph(fileSetExtended);
        if (flattenGoogleWrapped) {
            flattenGoogleWrapped();
        }

        if (messageName != null) {
            this.rootProtobufMessage = findMessage(messageName);
        } else {
            DescriptorProtos.FileDescriptorProto lastFile = fileSetExtended.getFileList()
                    .stream().filter(f -> filename != null && filename.endsWith(f.getName()))
                    .findFirst().orElse(fileSetExtended.getFile(fileSetExtended.getFileCount() - 1));

            if (filename != null) {
                this.rootProtobufMessage = autodetectRootMessage(lastFile)
                        .orElse(autodetectSingleMessage(lastFile)
                                .orElse(autodetectLocalFileRootMessage(lastFile)
                                        .orElseThrow(() -> new IllegalArgumentException("Cannot autodetect protobuf Message."))));
            } else {
                this.rootProtobufMessage = autodetectRootMessage(lastFile)
                        .orElseThrow(() -> new IllegalArgumentException("Cannot autodetect root protobuf Message."));
            }
        }
    }

    public ExtensionRegistry getRegistry() {
        return registry;
    }

    @Override
    public String getFullName() {
        return rootProtobufMessage.fullName();
    }

    @Override
    public long getMajorVersion() {
        return rootProtobufMessage.majorVersion();
    }

    public String getComment() {
        return rootProtobufMessage.description();
    }

    @Override
    public ProtobufMessage root() {
        return rootProtobufMessage;
    }

    protected Optional<ProtobufMessage> autodetectRootMessage(DescriptorProtos.FileDescriptorProto targetFile) throws IllegalArgumentException {
        return vertexSet().stream()
                .filter(v -> // incoming edges of fields
                        targetFile.equals(v.fileProto())
                                && v instanceof ProtobufMessage
                                && incomingEdgesOf(v).isEmpty()
                                && outgoingEdgesOf(v).stream()
                                .flatMap(e -> incomingEdgesOf(e.edgeTarget()).stream())
                                .allMatch(e -> e.edgeSource().equals(v))) // all the incoming edges on the child vertices should be self
                .map(v -> (ProtobufMessage) v)
                .findFirst();
    }

    protected Optional<ProtobufMessage> autodetectSingleMessage(DescriptorProtos.FileDescriptorProto targetFile) throws IllegalArgumentException {
        return vertexSet().stream()
                .filter(v -> // incoming edges of fields
                        targetFile.equals(v.fileProto())
                                && v instanceof ProtobufMessage
                                && targetFile.getMessageTypeCount() == 1)
                .map(v -> (ProtobufMessage) v)
                .findFirst();
    }

    protected Optional<ProtobufMessage> autodetectLocalFileRootMessage(DescriptorProtos.FileDescriptorProto targetFile) throws IllegalArgumentException {
        return vertexSet().stream()
                .filter(v -> // incoming edges of fields
                        targetFile.equals(v.fileProto())
                                && v instanceof ProtobufMessage
                                && incomingEdgesOf(v).stream().noneMatch(e -> e.edgeSource().fileProto().equals(targetFile))
                                && outgoingEdgesOf(v).stream() // all the incoming edges on the child vertices should be self within target file
                                .flatMap(e -> incomingEdgesOf(e.edgeTarget()).stream())
                                .allMatch(e -> !e.edgeSource().fileProto().equals(targetFile) || e.edgeSource().equals(v)))
                .map(v -> (ProtobufMessage) v)
                .findFirst();
    }

    public ProtobufMessage findMessage(String messageName) throws IllegalArgumentException {
        return (ProtobufMessage) vertexSet().stream()
                .filter(v -> v instanceof ProtobufMessage && messageName.equals(v.fullName()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find protobuf Message %s", messageName)));
    }

    private void buildProtobufGraph(DescriptorProtos.FileDescriptorSet fileSet) {
        // Attach non-nested fields to messages
        fileSet.getFileList().forEach(fileProto ->
                fileProto.getMessageTypeList().forEach(messageProto -> {

                    ProtobufMessage messageVertex = ProtobufMessage.builder()
                            .fileProto(fileProto)
                            .messageProto(messageProto)
                            .build();
                    addVertex(messageVertex);

                    // Handle nested fields
                    addNestedMessage(fileProto, messageProto);

                    // Add enum types
                    addEnum(fileProto, messageProto);

                    // handle normal fields and oneofs
                    messageProto.getFieldList().forEach(fieldProto -> {
                        ProtobufField fieldVertex = ProtobufField.builder()
                                .parentSchema(messageVertex)
                                .fieldProto(fieldProto)
                                .build();

                        // Add field vertex
                        addVertex(fieldVertex);

                        if (fieldVertex.oneOfProto() != null) {
                            // Handle oneOf
                            addOneOf(messageVertex, fieldVertex);
                        } else {
                            // Add schema to field edge
                            linkMessageToField(messageVertex, fieldVertex);
                        }
                    });
                })
        );

        // attach field paths to root message
        attachNestedSchemaFields(ProtobufField.class);
    }


    private void addEnum(DescriptorProtos.FileDescriptorProto fileProto, DescriptorProtos.DescriptorProto messageProto) {
        messageProto.getEnumTypeList().forEach(enumProto -> {
            ProtobufEnum enumVertex = ProtobufEnum.enumBuilder()
                    .fileProto(fileProto)
                    .messageProto(messageProto)
                    .enumProto(enumProto)
                    .build();
            addVertex(enumVertex);
        });
    }

    private void addNestedMessage(DescriptorProtos.FileDescriptorProto fileProto, DescriptorProtos.DescriptorProto messageProto) {
        messageProto.getNestedTypeList().forEach(nestedMessageProto -> {
            ProtobufMessage nestedMessageVertex = ProtobufMessage.builder()
                    .fileProto(fileProto)
                    .parentMessageProto(messageProto)
                    .messageProto(nestedMessageProto)
                    .build();
            addVertex(nestedMessageVertex);

            nestedMessageProto.getFieldList().forEach(nestedFieldProto -> {
                ProtobufField field = ProtobufField.builder()
                        .parentSchema(nestedMessageVertex)
                        .fieldProto(nestedFieldProto)
                        .build();

                // Add field vertex
                addVertex(field);

                // Add schema to field edge
                if (!field.isMessage()) {
                    ProtobufEdge.builder()
                            .source(nestedMessageVertex)
                            .target(field)
                            .type(field.fieldPathType())
                            .build().inGraph(this);
                }
            });
        });
    }

    private Stream<ProtobufField> addOneOf(ProtobufMessage messageVertex, ProtobufField fieldVertex) {
        // Handle oneOf
        ProtobufField oneOfVertex = ProtobufOneOfField.oneOfBuilder()
                .protobufMessage(messageVertex)
                .fieldProto(fieldVertex.fieldProto())
                .build();
        addVertex(oneOfVertex);

        ProtobufEdge.builder()
                .source(messageVertex)
                .target(oneOfVertex)
                .type(oneOfVertex.fieldPathType())
                .build().inGraph(this);

        // Add oneOf field to field edge
        ProtobufEdge.builder()
                .source(oneOfVertex)
                .target(fieldVertex)
                .type(fieldVertex.fieldPathType())
                .isMessageType(fieldVertex.isMessage())
                .build().inGraph(this);

        return Stream.of(oneOfVertex);
    }

    private Stream<ProtobufField> linkMessageToField(ProtobufMessage messageVertex, ProtobufField fieldVertex) {
        ProtobufEdge.builder()
                .source(messageVertex)
                .target(fieldVertex)
                .type(fieldVertex.fieldPathType())
                .isMessageType(fieldVertex.isMessage())
                .build().inGraph(this);

        return Stream.of(fieldVertex);
    }

    @Override
    protected void attachNestedSchemaFields(Map<String, List<ProtobufField>> fieldMap, ProtobufField messageField) {
        fieldMap.getOrDefault(messageField.nativeType(), List.of()).forEach(target -> {
            ProtobufEdge.builder()
                    .source(messageField)
                    .target(target)
                    .type(target.fieldPathType())
                    .isMessageType(target.isMessage())
                    .build().inGraph(this);
        });
    }

    private static final Set<String> GOOGLE_WRAPPERS = Set.of("google/protobuf/wrappers.proto", "google/protobuf/timestamp.proto");
    private void flattenGoogleWrapped() {
        HashSet<ProtobufElement> removeVertices = new HashSet<>();
        HashSet<ProtobufEdge> removeEdges = new HashSet<>();
        HashSet<ProtobufElement> addVertices = new HashSet<>();
        HashSet<ProtobufEdge> addEdges = new HashSet<>();

        Set<ProtobufElement> googleWrapped = vertexSet().stream()
                .filter(v -> v instanceof ProtobufMessage
                        && GOOGLE_WRAPPERS.contains(v.fileProto().getName()))
                .collect(Collectors.toSet());
        removeVertices.addAll(googleWrapped);

        Set<ProtobufField> wrappedPrimitiveFields = googleWrapped.stream()
                .flatMap(wrapped -> outgoingEdgesOf(wrapped).stream())
                .map(ProtobufEdge::edgeTarget)
                .map(ProtobufField.class::cast)
                .collect(Collectors.toSet());
        removeVertices.addAll(wrappedPrimitiveFields);

        wrappedPrimitiveFields.stream().filter(fld -> fld.getNumber() == 1).forEach(primitiveField -> {
            // remove incoming old edges to primitive
            removeEdges.addAll(incomingEdgesOf(primitiveField));

            Set<ProtobufField> originatingFields = incomingEdgesOf(primitiveField).stream()
                    .map(ProtobufEdge::edgeSource)
                    .filter(edgeSource -> !googleWrapped.contains(edgeSource))
                    .map(ProtobufField.class::cast)
                    .collect(Collectors.toSet());
            removeVertices.addAll(originatingFields);

            originatingFields.forEach(originatingField -> {
                // Replacement Field
                ProtobufElement fieldVertex = originatingField.toBuilder()
                        .fieldPathType(primitiveField.fieldPathType())
                        .schemaFieldDataType(primitiveField.schemaFieldDataType())
                        .isNestedType(false)
                        .build();
                addVertices.add(fieldVertex);

                // link source field parent directly to primitive
                Set<ProtobufEdge> incomingEdges = incomingEdgesOf(originatingField);
                removeEdges.addAll(incomingEdgesOf(originatingField));
                addEdges.addAll(incomingEdges.stream().map(oldEdge ->
                        // Replace old edge with new edge to primitive
                        ProtobufEdge.builder()
                                .source(oldEdge.edgeSource())
                                .target(fieldVertex)
                                .type(primitiveField.fieldPathType())
                                .isMessageType(false) // known primitive
                                .build()).collect(Collectors.toSet()));
            });

            // remove old fields
            removeVertices.addAll(originatingFields);
        });

        // Remove edges
        removeAllEdges(removeEdges);
        // Remove vertices
        removeAllVertices(removeVertices);
        // Add vertices
        addVertices.forEach(this::addVertex);
        // Add edges
        addEdges.forEach(e -> e.inGraph(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ProtobufGraph that = (ProtobufGraph) o;

        return rootProtobufMessage.equals(that.rootProtobufMessage);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rootProtobufMessage.hashCode();
        return result;
    }

    public String getHash() {
        return String.valueOf(super.hashCode());
    }
}
