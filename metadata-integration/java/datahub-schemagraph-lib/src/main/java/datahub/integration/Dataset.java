package datahub.integration;

import com.linkedin.common.AuditStamp;
import com.linkedin.common.Status;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.schema.SchemaField;
import com.linkedin.schema.SchemaFieldArray;
import com.linkedin.schema.SchemaMetadata;
import com.linkedin.util.Pair;
import datahub.event.MetadataChangeProposalWrapper;
import datahub.integration.model.FieldNode;
import datahub.integration.model.Node;
import datahub.integration.model.SchemaEdge;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class Dataset<G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>, N extends Node<N, S, F, E>,
        S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> {

    protected final C graphContext;

    protected SchemaVisitor<MetadataChangeProposalWrapper<? extends RecordTemplate>, G, C, N, S, F, E> datasetVisitor;
    protected SchemaVisitor<Pair<SchemaField, Double>, G, C, N, S, F, E> fieldVisitor;
    protected List<? extends SchemaVisitor<MetadataChangeProposalWrapper<? extends RecordTemplate>, G, C, N, S, F, E>> mcpwVisitors;

    public Dataset(C graphContext) {
        this.graphContext = graphContext;
        mcpwVisitors = List.of();
    }

    public <V extends SchemaVisitor<MetadataChangeProposalWrapper<? extends RecordTemplate>, G, C, N, S, F, E>>
    Dataset<G, C, N, S, F, E> setMetadataChangeProposalVisitors(List<V> visitors) {
        this.mcpwVisitors = visitors;
        return this;
    }

    public <V extends SchemaVisitor<MetadataChangeProposalWrapper<? extends RecordTemplate>, G, C, N, S, F, E>>
    Dataset<G, C, N, S, F, E> setDatasetVisitor(V datasetVisitor) {
        this.datasetVisitor = datasetVisitor;
        return this;
    }

    public <V extends SchemaVisitor<Pair<SchemaField, Double>, G, C, N, S, F, E>>
    Dataset<G, C, N, S, F, E> setFieldVisitor(V visitor) {
        this.fieldVisitor = visitor;
        return this;
    }

    public SchemaGraph<N, S, F, E> getGraph() {
        return graphContext.graph();
    }

    public AuditStamp getAuditStamp() {
        return graphContext.getAuditStamp();
    }

    public DatasetUrn getDatasetUrn() {
        return graphContext.getDatasetUrn();
    }

    public Stream<Collection<MetadataChangeProposalWrapper<? extends RecordTemplate>>> getAllMetadataChangeProposals() {
        return Stream.of(getVisitorMCPs(), getDatasetMCPs());
    }

    public List<MetadataChangeProposalWrapper<? extends RecordTemplate>> getVisitorMCPs() {
        return graphContext.accept(mcpwVisitors).collect(Collectors.toList());
    }

    public List<MetadataChangeProposalWrapper<? extends RecordTemplate>> getDatasetMCPs() {
        return Stream.concat(
                graphContext.accept(List.of(datasetVisitor)),
                Stream.of(
                        new MetadataChangeProposalWrapper<>(DatasetUrn.ENTITY_TYPE, getDatasetUrn().toString(), ChangeType.UPSERT,
                                getSchemaMetadata(), "schemaMetadata"),
                        new MetadataChangeProposalWrapper<>(DatasetUrn.ENTITY_TYPE, getDatasetUrn().toString(), ChangeType.UPSERT,
                                new Status().setRemoved(false), "status")
                )
        ).collect(Collectors.toList());
    }

    abstract public SchemaMetadata.PlatformSchema createPlatformSchema();

    public SchemaMetadata getSchemaMetadata() {
        SchemaMetadata.PlatformSchema platformSchema = createPlatformSchema();

        List<SchemaField> schemaFields = graphContext.accept(List.of(fieldVisitor))
                .sorted(COMPARE_BY_ROOT_MESSAGE_FIELD_WEIGHT.thenComparing(COMPARE_BY_FIELD_PATH))
                .map(Pair::getFirst)
                .collect(Collectors.toList());

        return new SchemaMetadata()
                .setSchemaName(getGraph().getFullName())
                .setPlatform(getDatasetUrn().getPlatformEntity())
                .setCreated(getAuditStamp())
                .setLastModified(getAuditStamp())
                .setVersion(getGraph().getMajorVersion())
                .setHash(getGraph().getHash())
                .setPlatformSchema(platformSchema)
                .setFields(new SchemaFieldArray(schemaFields));
    }

    public static final Comparator<Pair<SchemaField, Double>> COMPARE_BY_ROOT_MESSAGE_FIELD_WEIGHT = Comparator.comparing(Pair::getSecond);
    public static final Comparator<Pair<SchemaField, Double>> COMPARE_BY_FIELD_PATH = Comparator
            .comparing(p -> p.getFirst().getFieldPath());
}
