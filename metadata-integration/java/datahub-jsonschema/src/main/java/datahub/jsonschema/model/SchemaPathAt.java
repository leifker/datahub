package datahub.jsonschema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import datahub.jsonschema.JSchemaUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@Getter
public class SchemaPathAt {
    @Builder.Default
    private String schemaPath = "#";
    @Builder.Default
    private String at = "$.";
    @Builder.Default
    private Integer idx = null;

    public static SchemaPathAt indexed(SchemaPathAt schemaPathAt, int index) {
        return schemaPathAt.toBuilder().idx(index).build();
    }

    public static SchemaPathAt from(JsonSchema parentJsonSchema, JsonSchema currentSchema) {
        return from(parentJsonSchema, currentSchema.getSchemaNode(), SchemaPathAt.builder().schemaPath(currentSchema.getSchemaPath()).build());
    }

    public static SchemaPathAt from(@Nullable JsonSchema parentJsonSchema, JsonNode jsonNode, SchemaPathAt schemaPathAt) {
        if (schemaPathAt.schemaPath().startsWith("oneOf")) {
            return JSchemaUtils.getOneOfIndex(parentJsonSchema, jsonNode)
                    .map(idx -> SchemaPathAt.indexed(schemaPathAt, idx))
                    .orElse(schemaPathAt);
        } else {
            return schemaPathAt;
        }
    }

    private Stream<String> pathStream() {
        final Stream<String> path;
        if (at != null && !at.isEmpty() && !at.equals("$.")) {
            path = Arrays.stream(at.split("[.]"));
        } else {
            path = Arrays.stream(schemaPath.split("/"));
        }

        return path.filter(JSchemaUtils::idFilter);
    }

    public Stream<String> stream() {
        return stream(null);
    }

    public Stream<String> stream(@Nullable String mask) {
        LinkedBlockingQueue<String> path = pathStream().collect(Collectors.toCollection(LinkedBlockingQueue::new));

        if (!path.isEmpty() && path.peek().equals(mask)) {
            path.remove();
            return Stream.concat(Optional.ofNullable(idx).map(String::valueOf).stream(), path.stream());
        } else {
            return Stream.concat(Stream.concat(
                    path.isEmpty() ? Stream.of() : Stream.of(path.remove()),
                    Optional.ofNullable(idx).map(String::valueOf).stream()),
                    path.stream());
        }
    }

    public String schemaPathWithIndex() {
        return idx == null ? schemaPath() : String.format("%s/%s", schemaPath(), idx);
    }

    @Override
    public String toString() {
        return stream().collect(Collectors.joining("."));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaPathAt that = (SchemaPathAt) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }
}
