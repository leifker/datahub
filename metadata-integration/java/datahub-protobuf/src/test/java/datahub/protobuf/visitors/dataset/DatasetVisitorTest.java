package datahub.protobuf.visitors.dataset;

import com.linkedin.common.urn.DatasetUrn;
import com.linkedin.data.template.RecordTemplate;
import datahub.protobuf.ProtobufDataset;
import datahub.protobuf.visitors.ProtobufVisitor;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import datahub.protobuf.ProtobufContext;
import datahub.event.MetadataChangeProposalWrapper;

import static datahub.protobuf.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class DatasetVisitorTest {

    @Test
    public void protocBase64Test() throws URISyntaxException, IOException {
        String expected = "23454345452345233455";
        DatasetVisitor test = DatasetVisitor.builder().protocBase64(expected).build();

        List<MetadataChangeProposalWrapper<? extends RecordTemplate>> changes =
                test.visitGraph(
                        ProtobufContext.builder()
                                .auditStamp(TEST_AUDIT_STAMP)
                                .datasetUrn(DatasetUrn.createFromString("urn:li:dataset:(urn:li:dataPlatform:kafka,protobuf.MessageA,TEST)"))
                                .graph(getGraph("protobuf", "messageA")).build()
                ).collect(Collectors.toList());

        assertEquals(expected, extractCustomProperty(changes.get(0), "protoc"));
    }

    @Test
    public void customDescriptionVisitors() throws IOException {
        ProtobufDataset testDataset = getDataset("protobuf", "messageA");

        DatasetVisitor test = DatasetVisitor.builder()
                .descriptionVisitor(new ProtobufVisitor<String>() {
                    @Override
                    public Stream<String> visitGraph(ProtobufContext context) {
                        return Stream.of("Test Description");
                    }
                })
                .build();
        testDataset.setDatasetVisitor(test);

        assertEquals("Test Description", extractAspect(testDataset.getDatasetMCPs().get(0), "description"));
    }
}
