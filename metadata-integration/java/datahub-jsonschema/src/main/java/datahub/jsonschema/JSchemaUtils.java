package datahub.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import datahub.jsonschema.model.SchemaPathAt;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JSchemaUtils {
    public final static ObjectMapper mapper = new ObjectMapper();

    private JSchemaUtils() {
    }

    public static Optional<String> lookup(JsonNode jsonNode, String fieldName) {
        return Optional.ofNullable(jsonNode.get(fieldName)).map(JsonNode::asText);
    }

    public static boolean isRequired(JsonNode parentNode, String fieldName) {
        return Optional.ofNullable(parentNode.get("required")).map(required ->
                StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(required.elements(), Spliterator.ORDERED), true)
                        .anyMatch(n -> n.asText().equals(fieldName))
        ).orElse(false);
    }

    public static Optional<Integer> getOneOfIndex(@Nullable JsonSchema oneOfSchema, JsonNode element) {
        return Optional.ofNullable(oneOfSchema).map(n -> {
            final JsonNode target;
            final JsonNode hasOneOf;
            if (n.getSchemaNode().has("oneOf")) {
                hasOneOf = n.getSchemaNode();
                target = element;
            } else if (n.getParentSchema().getSchemaNode().has("oneOf")) {
                hasOneOf = n.getParentSchema().getSchemaNode();
                target = n.getSchemaNode();
            } else {
                return null;
            }

            Iterator<JsonNode> itr = hasOneOf.get("oneOf").iterator();
            int idx = 0;
            while (itr.hasNext()) {
                JsonNode e = itr.next();
                if (e.equals(target)) {
                    return idx;
                }
                if (e.has("type")) idx += 1;
            }
            return null;
        });
    }

    public static boolean idFilter(String id) {
        return id != null && !Set.of("properties", "#", "", "oneOf", "$").contains(id);
    }

    private static void addIdComponents(JsonSchema jsonSchema, ArrayDeque<String> idComponents) {
        if (jsonSchema.getParentSchema() == null) {
            URI uri = jsonSchema.getCurrentUri();

            Arrays.stream(uri.getPath().replaceFirst("\\.json$", "").split("/"))
                    .filter(JSchemaUtils::idFilter)
                    .forEach(idComponents::add);

            idComponents.addFirst(uri.getHost());
        } else {
            addIdComponents(jsonSchema.getParentSchema(), idComponents);
            SchemaPathAt modifiedSchemaPath = SchemaPathAt.from(jsonSchema.getParentSchema(), jsonSchema);
            modifiedSchemaPath.stream().forEach(idComponents::add);
        }
    }

    public static String id(JsonSchema parentSchema, SchemaPathAt schemaPathAt) {
        return id(parentSchema, schemaPathAt, false);
    }
    public static String id(JsonSchema parentSchema, SchemaPathAt schemaPathAt, boolean parent) {
        ArrayDeque<String> idComponents = new ArrayDeque<>();
        addIdComponents(parentSchema, idComponents);
        schemaPathAt.stream(idComponents.getLast()).forEach(idComponents::add);
        if (parent) {
            idComponents.removeLast();
        }
        return String.join(".", idComponents);
    }

    public static String name(String fullName) {
        return Arrays.stream(fullName.split("[.]")).reduce((first, second) -> second).get();
    }

    public static Optional<JsonNode> extractTypeJsonNode(JsonNode jsonNode) {
        Optional<JsonNode> withType = Optional.empty();
        if (jsonNode.has("type") || jsonNode.has("enum") || jsonNode.has("oneOf")) {
            withType = Optional.of(jsonNode);
        } else if (jsonNode.has("allOf")) {
            withType = StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(jsonNode.get("allOf").elements(), Spliterator.ORDERED), false)
                    .filter(j -> j.has("type") || j.has("enum"))
                    .findFirst();
        }
        return withType;
    }

    public static String nativeType(JsonSchema jsonSchema, JsonNode jsonNode, SchemaPathAt schemaPathAt) {
        Optional<JsonNode> jsonNodeTypeOpt = JSchemaUtils.extractTypeJsonNode(jsonNode);

        Optional<String> typeComponents = jsonNodeTypeOpt.map(jsonNodeWithType -> {
            if (jsonNodeWithType.has("enum")) {
                return jsonNodeWithType.toString().replaceAll("^[{]|[}]$", "");
            } else if (jsonNodeWithType.has("oneOf")) {
                return String.format("\"oneOf\":%s", jsonNodeWithType.get("oneOf"));
            } else {
                List<String> optionalFields = Stream.of("format", "contentMediaType", "contentEncoding", "contentSchema")
                        .filter(jsonNodeWithType::has).collect(Collectors.toList());

                final Map.Entry<String, String> typeEntry;
                if (!jsonNodeWithType.get("type").asText().equals("object")) {
                    typeEntry = Map.entry("type", jsonNodeWithType.get("type").asText());
                } else {
                    typeEntry = Map.entry("$ref", String.format("%s%s", jsonSchema.getCurrentUri().toString(), schemaPathAt.schemaPathWithIndex()));
                }
                return Stream.concat(Stream.of(typeEntry),
                                optionalFields.stream().map(k -> Map.entry(k, jsonNodeWithType.get(k).toString().replaceAll("^\"|\"$", ""))))
                        .map(e -> String.format("\"%s\":\"%s\"", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(","));
            }
        });

        return String.format("{%s}", typeComponents.orElse(null));
    }
}
