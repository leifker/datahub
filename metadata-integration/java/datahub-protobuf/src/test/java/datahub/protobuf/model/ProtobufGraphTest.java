package datahub.protobuf.model;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static datahub.protobuf.TestFixtures.getFileSet;
import static datahub.protobuf.TestFixtures.getGraph;
import static org.junit.jupiter.api.Assertions.*;

public class ProtobufGraphTest {

    @Test
    public void autodetectRootMessageTest() throws IOException {
        FileDescriptorSet fileset = getFileSet("protobuf", "messageB");
        ProtobufGraph test = getGraph("protobuf", "messageB");

        assertEquals("MessageB", test.autodetectRootMessage(
                fileset.getFileList().stream().filter(f -> f.getName().equals("protobuf/messageB.proto")).findFirst().get()).get().messageProto().getName());

        assertEquals("MessageA", test.autodetectRootMessage(
                fileset.getFileList().stream().filter(f -> f.getName().equals("protobuf/messageA.proto")).findFirst().get()).get().messageProto().getName());
    }

    @Test
    public void autodetectRootMessageFailureTest() throws IOException {
        FileDescriptorSet empty = getFileSet("protobuf", "messageEmpty");
        assertThrows(IllegalArgumentException.class, () -> new ProtobufGraph(empty));
    }

    @Test
    public void findMessageTest() throws IOException {
        FileDescriptorSet fileset = getFileSet("protobuf", "messageB");
        ProtobufGraph test = getGraph("protobuf", "messageB");

        assertEquals("MessageA",
                test.findMessage("protobuf.MessageA").messageProto().getName());
        assertEquals("MessageB",
                test.findMessage("protobuf.MessageB").messageProto().getName());

        assertThrows(IllegalArgumentException.class, () -> test.findMessage("not found"));
        assertThrows(IllegalArgumentException.class, () -> new ProtobufGraph(fileset, "not found"));
        assertEquals(test, new ProtobufGraph(fileset, "protobuf.MessageB"));
    }

    @Test
    public void commentTest() throws IOException {
        ProtobufGraph test = getGraph("protobuf", "messageC");
        assertEquals("Test for one of", test.getComment());
    }

    @Test
    public void equalityHashCodeTest() throws IOException {
        ProtobufGraph testA = getGraph("protobuf", "messageA");
        ProtobufGraph testB = getGraph("protobuf", "messageB");
        FileDescriptorSet filesetB = getFileSet("protobuf", "messageB");

        assertEquals(testB, new ProtobufGraph(filesetB));
        assertNotEquals(testA, new ProtobufGraph(filesetB));
        assertEquals(testA, testA);
        assertNotEquals(testA, testB);

        HashSet<ProtobufGraph> graphs = new HashSet<>();
        graphs.add(testA);
        graphs.add(testB);
        graphs.add(new ProtobufGraph(filesetB));
        assertEquals(2, graphs.size());
    }

    @Test
    public void duplicateNestedTest() throws IOException {
        FileDescriptorSet fileset = getFileSet("protobuf", "messageB");
        ProtobufGraph test = getGraph("protobuf", "messageB");

        List<ProtobufElement> nestedMessages = test.vertexSet().stream().filter(f -> f.name().endsWith("nested"))
                .collect(Collectors.toList());

        assertEquals(2, nestedMessages.size(), "Expected 2 nested fields");
    }
}
