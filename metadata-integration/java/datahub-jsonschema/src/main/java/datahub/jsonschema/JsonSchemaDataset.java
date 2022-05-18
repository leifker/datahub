package datahub.jsonschema;

import com.linkedin.common.AuditStamp;
import com.linkedin.common.FabricType;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.schema.OtherSchema;
import com.linkedin.schema.SchemaMetadata;
import datahub.integration.Dataset;
import datahub.integration.visitors.FieldVisitor;
import datahub.integration.visitors.InstitutionalMemoryVisitor;
import datahub.jsonschema.model.JSEdge;
import datahub.jsonschema.model.JSElement;
import datahub.jsonschema.model.JSField;
import datahub.jsonschema.model.JSGraph;
import datahub.jsonschema.model.JSSchema;
import datahub.jsonschema.visitors.dataset.DatasetVisitor;
import datahub.jsonschema.visitors.dataset.DomainVisitor;
import datahub.jsonschema.visitors.dataset.OwnershipVisitor;
import datahub.jsonschema.visitors.dataset.PropertyVisitor;
import datahub.jsonschema.visitors.dataset.TagAssociationVisitor;
import datahub.jsonschema.visitors.dataset.TermAssociationVisitor;
import datahub.jsonschema.visitors.tags.JsonSchemaTagVisitor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;


public class JsonSchemaDataset extends Dataset<JSGraph, JsonSchemaContext, JSElement, JSSchema, JSField, JSEdge> {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DataPlatformUrn dataPlatformUrn;
        private DatasetUrn datasetUrn;
        private FabricType fabricType;
        private AuditStamp auditStamp;
        private String schemaName;
        private String uri;
        private String githubOrganization;
        private String slackTeamId;

        public Builder setGithubOrganization(@Nullable String githubOrganization) {
            this.githubOrganization = githubOrganization;
            return this;
        }

        public Builder setSlackTeamId(@Nullable String slackTeamId) {
            this.slackTeamId = slackTeamId;
            return this;
        }

        public Builder setDataPlatformUrn(@Nullable DataPlatformUrn dataPlatformUrn) {
            this.dataPlatformUrn = dataPlatformUrn;
            return this;
        }

        public Builder setDatasetUrn(@Nullable DatasetUrn datasetUrn) {
            this.datasetUrn = datasetUrn;
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

        public Builder setSchemaName(@Nullable String schemaName) {
            this.schemaName = schemaName;
            return this;
        }
        public Builder setUri(@Nullable String uri) {
            this.uri = uri;
            return this;
        }

        public JsonSchemaDataset build() throws URISyntaxException, IOException {
            JSGraph graph = new JSGraph(uri);
            DataPlatformUrn dataPlatformUrn = Optional.ofNullable(this.dataPlatformUrn).orElse(new DataPlatformUrn("OpenApi"));
            DatasetUrn datasetUrn = this.datasetUrn != null ? this.datasetUrn : new DatasetUrn(dataPlatformUrn, graph.getFullName(), fabricType);

            JsonSchemaContext context = JsonSchemaContext.builder()
                    .datasetUrn(datasetUrn)
                    .auditStamp(auditStamp)
                    .graph(graph)
                    .build();

            DatasetVisitor datasetVisitor = DatasetVisitor.builder()
                    .specVersion(graph.getSpecVersion())
                    .datasetPropertyVisitors(
                            List.of(
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

            return new JsonSchemaDataset(context, graph.getSchemaSource(), datasetVisitor);
        }
    }

    private final Optional<String> schemaSource;

    JsonSchemaDataset(JsonSchemaContext context, String schema, DatasetVisitor datasetVisitor) {
        super(context);
        this.schemaSource = Optional.ofNullable(schema);
        setMetadataChangeProposalVisitors(
                List.of(
                        new JsonSchemaTagVisitor()
                )
        );
        setFieldVisitor(new FieldVisitor<>(JSField.class));
        setDatasetVisitor(datasetVisitor);
    }

    @Override
    public SchemaMetadata.PlatformSchema createPlatformSchema() {
        SchemaMetadata.PlatformSchema platformSchema = new SchemaMetadata.PlatformSchema();
        schemaSource.ifPresent(s -> platformSchema.setOtherSchema(new OtherSchema().setRawSchema(s)));
        return platformSchema;
    }
}
