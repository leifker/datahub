package datahub.protobuf;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.FabricType;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.schema.KafkaSchema;
import com.linkedin.schema.SchemaMetadata;
import datahub.integration.Dataset;
import datahub.integration.visitors.InstitutionalMemoryVisitor;
import datahub.protobuf.model.ProtobufEdge;
import datahub.protobuf.model.ProtobufElement;
import datahub.protobuf.model.ProtobufField;
import datahub.protobuf.model.ProtobufGraph;
import datahub.protobuf.model.ProtobufMessage;
import datahub.protobuf.visitors.dataset.DatasetVisitor;
import datahub.protobuf.visitors.dataset.DomainVisitor;
import datahub.protobuf.visitors.dataset.KafkaTopicPropertyVisitor;
import datahub.protobuf.visitors.dataset.OwnershipVisitor;
import datahub.protobuf.visitors.dataset.PropertyVisitor;
import datahub.protobuf.visitors.dataset.TagAssociationVisitor;
import datahub.protobuf.visitors.dataset.TermAssociationVisitor;
import datahub.protobuf.visitors.field.ProtobufExtensionFieldVisitor;
import datahub.protobuf.visitors.tags.ProtobufTagVisitor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.List;


public class ProtobufDataset extends Dataset<ProtobufGraph, ProtobufContext, ProtobufElement, ProtobufMessage, ProtobufField, ProtobufEdge> {

    public static ProtobufDataset.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DataPlatformUrn dataPlatformUrn;
        private DatasetUrn datasetUrn;
        private FabricType fabricType;
        private AuditStamp auditStamp;
        private byte[] protocBytes;
        private String messageName;
        private String filename;
        private String schema;
        private String githubOrganization;
        private String slackTeamId;
        private String subType;

        public Builder setGithubOrganization(@Nullable String githubOrganization) {
            this.githubOrganization = githubOrganization;
            return this;
        }

        public Builder setSlackTeamId(@Nullable String slackTeamId) {
            this.slackTeamId = slackTeamId;
            return this;
        }

        public Builder setProtocIn(InputStream protocIn) throws IOException {
            return setProtocBytes(protocIn.readAllBytes());
        }

        public Builder setDataPlatformUrn(@Nullable DataPlatformUrn dataPlatformUrn) {
            this.dataPlatformUrn = dataPlatformUrn;
            return this;
        }

        public Builder setDatasetUrn(@Nullable DatasetUrn datasetUrn) {
            this.datasetUrn = datasetUrn;
            return this;
        }

        public Builder setProtocBytes(byte[] protocBytes) {
            this.protocBytes = protocBytes;
            return this;
        }

        public Builder setFabricType(FabricType fabricType) {
            this.fabricType = fabricType;
            return this;
        }

        public Builder setAuditStamp(AuditStamp auditStamp) {
            this.auditStamp = auditStamp;
            return this;
        }

        public Builder setMessageName(@Nullable String messageName) {
            this.messageName = messageName;
            return this;
        }
        public Builder setFilename(@Nullable String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setSchema(@Nullable String schema) {
            this.schema = schema;
            return this;
        }

        public Builder setSubType(@Nullable String subType) {
            this.subType = subType;
            return this;
        }

        public ProtobufDataset build() throws IOException {
            FileDescriptorSet fileSet = FileDescriptorSet.parseFrom(this.protocBytes);
            ProtobufGraph graph = new ProtobufGraph(fileSet, this.messageName, this.filename);
            DataPlatformUrn dataPlatformUrn = Optional.ofNullable(this.dataPlatformUrn).orElse(new DataPlatformUrn("kafka"));
            DatasetUrn datasetUrn = this.datasetUrn != null ? this.datasetUrn : new DatasetUrn(dataPlatformUrn, graph.getFullName(), fabricType);

            ProtobufContext context = ProtobufContext.builder()
                    .datasetUrn(datasetUrn)
                    .auditStamp(auditStamp)
                    .graph(graph)
                    .build();

            DatasetVisitor datasetVisitor = DatasetVisitor.builder()
                    .protocBase64(Base64.getEncoder().encodeToString(protocBytes))
                    .datasetPropertyVisitors(
                            List.of(
                                    new KafkaTopicPropertyVisitor(),
                                    new PropertyVisitor()
                            )
                    )
                    .institutionalMemoryMetadataVisitors(
                            List.of(
                                    new InstitutionalMemoryVisitor<>(slackTeamId, githubOrganization)
                            )
                    )
                    .tagAssociationVisitors(
                            List.of(
                                    new TagAssociationVisitor()
                            )
                    )
                    .termAssociationVisitors(
                            List.of(
                                    new TermAssociationVisitor()
                            )
                    )
                    .ownershipVisitors(
                            List.of(
                                    new OwnershipVisitor()
                            )
                    )
                    .domainVisitors(
                            List.of(
                                    new DomainVisitor()
                            )
                    )
                    .build();

            return new ProtobufDataset(context, schema, datasetVisitor)
                    .setSubType(subType);
        }
    }

    private final Optional<String> schemaSource;

    ProtobufDataset(ProtobufContext context, String schema, DatasetVisitor datasetVisitor) {
        super(context);
        this.schemaSource = Optional.ofNullable(schema);
        setMetadataChangeProposalVisitors(
                List.of(new ProtobufTagVisitor())
        );
        setFieldVisitor(new ProtobufExtensionFieldVisitor());
        setDatasetVisitor(datasetVisitor);
    }

    @Override
    public ProtobufDataset setSubType(String subType) {
        super.setSubType(subType);
        return this;
    }

    @Override
    public SchemaMetadata.PlatformSchema createPlatformSchema() {
        SchemaMetadata.PlatformSchema platformSchema = new SchemaMetadata.PlatformSchema();
        schemaSource.ifPresent(s -> platformSchema.setKafkaSchema(new KafkaSchema().setDocumentSchema(s)));
        return platformSchema;
    }
}
