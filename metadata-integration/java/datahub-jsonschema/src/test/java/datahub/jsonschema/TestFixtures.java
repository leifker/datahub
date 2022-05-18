package datahub.jsonschema;

import com.linkedin.common.AuditStamp;
import com.linkedin.common.FabricType;
import com.linkedin.common.urn.CorpuserUrn;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.data.DataMap;
import com.linkedin.data.template.RecordTemplate;
import datahub.event.MetadataChangeProposalWrapper;
import datahub.jsonschema.model.JSGraph;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class TestFixtures {
    private TestFixtures() { }

    public static final DataPlatformUrn TEST_DATA_PLATFORM = new DataPlatformUrn("OpenApi");
    public static final AuditStamp TEST_AUDIT_STAMP = new AuditStamp()
            .setTime(System.currentTimeMillis())
            .setActor(new CorpuserUrn("datahub"));

    public static String getUri(String path, String filename) throws URISyntaxException {
        return Objects.requireNonNull(TestFixtures.class.getClassLoader()
                .getResource(String.format("%s/%s.json", path, filename))).toURI().toString();
    }

    public static String getSchemaSource(String path, String filename) throws IOException {
        return new String(Objects.requireNonNull(TestFixtures.class.getClassLoader()
                .getResourceAsStream(String.format("%s/%s.json", path, filename))).readAllBytes());
    }

    public static JsonSchemaDataset getDataset(String path, String filename) throws URISyntaxException, IOException {
        return JsonSchemaDataset.builder()
                .setDataPlatformUrn(TEST_DATA_PLATFORM)
                .setUri(getUri(path, filename))
                .setAuditStamp(TEST_AUDIT_STAMP)
                .setFabricType(FabricType.TEST)
                .setGithubOrganization("myOrg")
                .setSlackTeamId("SLACK123")
                .build();
    }

    public static JsonSchemaContext getContext(String path, String filename, String name) throws URISyntaxException, IOException {
        return JsonSchemaContext.builder()
                .datasetUrn(new DatasetUrn(TEST_DATA_PLATFORM, name, FabricType.TEST))
                .auditStamp(TEST_AUDIT_STAMP)
                .graph(getGraph(path, filename))
                .build();
    }

    public static JSGraph getGraph(String path, String filename) throws URISyntaxException, IOException {
        return new JSGraph(getUri(path, filename));
    }

    public static Object extractAspect(MetadataChangeProposalWrapper<? extends RecordTemplate> mcp, String aspect) {
        return mcp.getAspect().data().get(aspect);
    }

    public static Object extractCustomProperty(MetadataChangeProposalWrapper<? extends RecordTemplate> mcp, String key) {
        return ((DataMap) extractAspect(mcp, "customProperties")).get(key);
    }

    public static String extractDocumentSchema(JsonSchemaDataset protobufDataset) {
        return String.valueOf(((DataMap) ((DataMap) protobufDataset.getSchemaMetadata().getPlatformSchema().data())
                .get("com.linkedin.schema.OtherSchema")).get("rawSchema"));
    }
}
