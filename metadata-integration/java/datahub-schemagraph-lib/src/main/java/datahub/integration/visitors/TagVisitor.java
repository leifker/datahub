package datahub.integration.visitors;

import com.linkedin.common.urn.TagUrn;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.tag.TagProperties;
import datahub.event.MetadataChangeProposalWrapper;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.FieldNode;
import datahub.integration.model.Node;
import datahub.integration.model.SchemaEdge;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;


public class TagVisitor<G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>,
        N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>,
        E extends SchemaEdge<N, S, F, E>> implements SchemaVisitor<MetadataChangeProposalWrapper<? extends RecordTemplate>, G, C, N, S, F, E> {
    protected static final String TAG_PROPERTIES_ASPECT = "tagProperties";

    protected static MetadataChangeProposalWrapper<TagProperties> wrapTagProperty(TagProperties tagProperty) {
        return new MetadataChangeProposalWrapper<>(
                "tag",
                new TagUrn(tagProperty.getName()).toString(),
                ChangeType.UPSERT,
                tagProperty,
                TAG_PROPERTIES_ASPECT);
    }
}
