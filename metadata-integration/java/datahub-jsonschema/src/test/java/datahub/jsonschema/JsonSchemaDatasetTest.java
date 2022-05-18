package datahub.jsonschema;

import com.linkedin.common.FabricType;
import com.linkedin.common.InstitutionalMemory;
import com.linkedin.common.InstitutionalMemoryMetadata;
import com.linkedin.common.InstitutionalMemoryMetadataArray;
import com.linkedin.common.Status;
import com.linkedin.common.url.Url;
import com.linkedin.common.urn.DataPlatformUrn;
import com.linkedin.data.template.StringArray;
import com.linkedin.schema.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static datahub.jsonschema.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonSchemaDatasetTest {

    @Test
    public void simpleSchema() throws URISyntaxException, IOException {
        JsonSchemaDataset dataset = JsonSchemaDataset.builder()
                .setDataPlatformUrn(new DataPlatformUrn("OpenApi"))
                .setUri(getUri("draft2019-09", "simpleExample"))
                .setAuditStamp(TEST_AUDIT_STAMP)
                .setFabricType(FabricType.DEV)
                .build();

        assertNotNull(dataset);
        assertEquals(2, dataset.getAllMetadataChangeProposals().count());
        assertEquals(8, dataset.getDatasetMCPs().size());
        assertEquals(0, dataset.getVisitorMCPs().size());
    }

    @Test
    public void platformSchemaTest() throws URISyntaxException, IOException {
        assertEquals(getSchemaSource("draft2019-09", "simpleExample"),
                extractDocumentSchema(getDataset("draft2019-09", "simpleExample")));
    }

    @Test
    public void simpleExample() throws IOException, URISyntaxException {
        JsonSchemaDataset test = getDataset("draft2019-09", "simpleExample");

        assertEquals("urn:li:dataset:(urn:li:dataPlatform:OpenApi,example.com.person,TEST)",
                test.getDatasetUrn().toString());

        SchemaMetadata testMetadata = test.getSchemaMetadata();

        assertEquals(1, testMetadata.getVersion());
        assertEquals(15, testMetadata.getFields().size());

        assertEquals("V201909", extractCustomProperty(test.getDatasetMCPs().get(0), "specVersion"));

        assertEquals(new InstitutionalMemory().setElements(new InstitutionalMemoryMetadataArray(
                        new InstitutionalMemoryMetadata()
                                .setDescription("Github Team")
                                .setCreateStamp(TEST_AUDIT_STAMP)
                                .setUrl(new Url("https://github.com/orgs/myOrg/teams/teama")),
                        new InstitutionalMemoryMetadata()
                                .setDescription("Slack Channel")
                                .setCreateStamp(TEST_AUDIT_STAMP)
                                .setUrl(new Url("https://slack.com/app_redirect?channel=test-slack&team=SLACK123")),
                        new InstitutionalMemoryMetadata()
                                .setCreateStamp(TEST_AUDIT_STAMP)
                                .setDescription("Person Reference 1")
                                .setUrl(new Url("https://some/link")),
                        new InstitutionalMemoryMetadata()
                                .setCreateStamp(TEST_AUDIT_STAMP)
                                .setDescription("Person.address Reference 1")
                                .setUrl(new Url("https://json-schema.org")))).data(),
                test.getDatasetMCPs().get(1).getAspect().data());

        assertEquals(new Status().setRemoved(false).data(), test.getDatasetMCPs().get(test.getDatasetMCPs().size() - 1).getAspect().data());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=string].firstName")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(false)
                        .setIsPartOfKey(false)
                        .setDescription("The person's first name.")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=string].firstName")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=string].lastName")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(false)
                        .setIsPartOfKey(false)
                        .setDescription("The person's last name.")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=string].lastName")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=int].age")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new NumberType())))
                        .setNativeDataType("{\"type\":\"integer\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("Age in years which must be equal to or greater than zero.")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=int].age")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=string].birthday")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("The person's birthday as a string.\nPrefer age field.")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=string].birthday")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=boolean].has_children")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new BooleanType())))
                        .setNativeDataType("{\"type\":\"boolean\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("Has children.")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=boolean].has_children")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=double].children_count")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new NumberType())))
                        .setNativeDataType("{\"type\":\"number\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("Number of children")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=double].children_count")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=array].[type=int].children_ages")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new ArrayType().setNestedType(new StringArray()))))
                        .setNativeDataType("{\"type\":\"array\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=array].[type=int].children_ages")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=array].[type=string].children_names")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new ArrayType().setNestedType(new StringArray()))))
                        .setNativeDataType("{\"type\":\"array\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=array].[type=string].children_names")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=enum].employment_status")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new EnumType())))
                        .setNativeDataType("{\"enum\":[\"not_eligible\",\"unemployed\",\"part-time\",\"full-time\"]}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=enum].employment_status")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=example_com_person_address].address")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new RecordType())))
                        .setNativeDataType("{\"$ref\":\"https://example.com/person.json#/properties/address\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("See some link here https://json-schema.org")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=example_com_person_address].address")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=example_com_person_address].address.[type=string].street_address")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=example_com_person_address].address.[type=string].street_address")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=example_com_person_address].address.[type=string].country")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(false)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=example_com_person_address].address.[type=string].country")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_person].[type=string].token")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\",\"contentMediaType\":\"application/jwt\",\"contentSchema\":\"{\"type\":\"array\",\"minItems\":2,\"items\":[{\"const\":{\"typ\":\"JWT\",\"alg\":\"HS256\"}},{\"type\":\"object\",\"required\":[\"iss\",\"exp\"],\"properties\":{\"iss\":{\"type\":\"string\"},\"exp\":{\"type\":\"integer\"}}}]}\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_person].[type=string].token")).findFirst().orElseThrow());

    }

    @Test
    public void unionTest() throws IOException, URISyntaxException {
        JsonSchemaDataset test = getDataset("draft2019-09", "complexExample");

        assertEquals("urn:li:dataset:(urn:li:dataPlatform:OpenApi,example.com.v2.person,TEST)",
                test.getDatasetUrn().toString());

        SchemaMetadata testMetadata = test.getSchemaMetadata();

        assertEquals(2, testMetadata.getVersion());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=string].firstName1")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=string].firstName1")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=string].firstName2")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=string].firstName2")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=string].lastName")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("A def for a person's name.")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=string].lastName")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=string].birthday")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=string].birthday")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].has_children")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new UnionType())))
                        .setNativeDataType("{\"oneOf\":[{\"type\":\"string\"},{\"type\":\"boolean\"}]}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].has_children")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].has_children.[type=string].0")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].has_children.[type=string].0")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].has_children.[type=boolean].1")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new BooleanType())))
                        .setNativeDataType("{\"type\":\"boolean\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].has_children.[type=boolean].1")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new UnionType())))
                        .setNativeDataType("{\"oneOf\":[{\"type\":\"object\",\"properties\":{\"street_address\":{\"type\":\"string\"},\"city\":{\"type\":\"string\"},\"State\":{\"type\":\"string\"},\"ZipCode\":{\"type\":\"string\"}},\"required\":[\"ZipCode\"]},{\"type\":\"object\",\"properties\":{\"street_address\":{\"type\":\"string\"},\"city\":{\"type\":\"string\"},\"County\":{\"type\":\"string\"},\"PostCode\":{\"type\":\"string\"}},\"required\":[\"PostCode\"]}]}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("See some link here https://json-schema.org")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new RecordType())))
                        .setNativeDataType("{\"$ref\":\"https://example.com/v2/person.json#/properties/address/0\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].street_address")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].street_address")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].city")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].city")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].State")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].State")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].ZipCode")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(false)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_0].0.[type=string].ZipCode")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new RecordType())))
                        .setNativeDataType("{\"$ref\":\"https://example.com/v2/person.json#/properties/address/1\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].street_address")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].street_address")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].city")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].city")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].County")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].County")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].PostCode")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(false)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_v2_person].[type=union].address.[type=example_com_v2_person_address_1].1.[type=string].PostCode")).findFirst().orElseThrow());
    }

    @Test
    public void recursiveTest() throws IOException, URISyntaxException {
        JsonSchemaDataset test = getDataset("draft2019-09", "recursiveExample");

        assertEquals("urn:li:dataset:(urn:li:dataPlatform:OpenApi,example.com.recursiveExample,TEST)",
                test.getDatasetUrn().toString());

        SchemaMetadata testMetadata = test.getSchemaMetadata();

        assertEquals(1, testMetadata.getVersion());
        assertEquals(2, testMetadata.getFields().size());

        assertEquals("V201909", extractCustomProperty(test.getDatasetMCPs().get(0), "specVersion"));

        assertEquals(new Status().setRemoved(false).data(), test.getDatasetMCPs().get(test.getDatasetMCPs().size() - 1).getAspect().data());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_recursiveExample].[type=string].name")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_recursiveExample].[type=string].name")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_recursiveExample].[type=array].children")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new ArrayType().setNestedType(new StringArray()))))
                        .setNativeDataType("{\"type\":\"array\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_recursiveExample].[type=array].children")).findFirst().orElseThrow());

    }

    @Test
    @Ignore
    public void bundlingTest() throws IOException, URISyntaxException {
        JsonSchemaDataset test = getDataset("draft2020-12", "bundlingExample");

        assertEquals("urn:li:dataset:(urn:li:dataPlatform:OpenApi,example.com.recursiveExample,TEST)",
                test.getDatasetUrn().toString());

        SchemaMetadata testMetadata = test.getSchemaMetadata();

        assertEquals(1, testMetadata.getVersion());
        assertEquals(2, testMetadata.getFields().size());

        assertEquals("V201909", extractCustomProperty(test.getDatasetMCPs().get(0), "specVersion"));

        assertEquals(new Status().setRemoved(false).data(), test.getDatasetMCPs().get(test.getDatasetMCPs().size() - 1).getAspect().data());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_recursiveExample].[type=string].name")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new StringType())))
                        .setNativeDataType("{\"type\":\"string\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //       .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //       .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_recursiveExample].[type=string].name")).findFirst().orElseThrow());

        assertEquals(new SchemaField()
                        .setFieldPath("[version=2.0].[type=example_com_recursiveExample].[type=array].children")
                        .setType(new SchemaFieldDataType().setType(SchemaFieldDataType.Type.create(new ArrayType().setNestedType(new StringArray()))))
                        .setNativeDataType("{\"type\":\"array\"}")
                        .setNullable(true)
                        .setIsPartOfKey(false)
                        .setDescription("")
                //        .setGlobalTags(new GlobalTags().setTags(new TagAssociationArray()))
                //        .setGlossaryTerms(new GlossaryTerms().setTerms(new GlossaryTermAssociationArray()).setAuditStamp(TEST_AUDIT_STAMP))
                , testMetadata.getFields().stream().filter(f -> f.getFieldPath()
                        .equals("[version=2.0].[type=example_com_recursiveExample].[type=array].children")).findFirst().orElseThrow());

    }

}
