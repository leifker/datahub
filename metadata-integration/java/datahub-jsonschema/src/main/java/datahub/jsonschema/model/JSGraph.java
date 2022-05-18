package datahub.jsonschema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedin.util.Pair;
import com.networknt.schema.*;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import datahub.integration.model.SchemaGraph;
import datahub.jsonschema.JSchemaUtils;
import lombok.Builder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static datahub.jsonschema.JSchemaUtils.id;


public class JSGraph extends SchemaGraph<JSElement, JSSchema, JSField, JSEdge> {
    private final JSSchema rootJSSchema;
    private final JsonSchema rootJsonSchema;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final String schemaSource;
    private final SpecVersion.VersionFlag specVersion;

    private final ConcurrentHashMap<String, JSSchema> visitedJSSchemas;
    private final ConcurrentHashMap<String, JSField> visitedJSFields;

    public JSGraph(String uri) throws URISyntaxException, IOException {
        super(JSEdge.class);

        this.visitedJSSchemas = new ConcurrentHashMap<>();
        this.visitedJSFields = new ConcurrentHashMap<>();

        URI schemaURI = new URI(uri);
        JsonNode schema = JSchemaUtils.mapper.readTree(schemaURI.toURL());
        schemaSource = schema.toPrettyString();
        this.specVersion = SpecVersionDetector.detect(schema);

        this.jsonSchemaFactory = JsonSchemaFactory.getInstance(this.specVersion);

        this.rootJsonSchema = jsonSchemaFactory.getSchema(schemaURI);
        this.rootJSSchema = addSchema(
                this.rootJsonSchema,
                this.rootJsonSchema.getSchemaNode(),
                SchemaPathAt.builder().schemaPath(this.rootJsonSchema.getSchemaPath()).build()).get();

        walk(this.rootJsonSchema.getSchemaNode());
        attachNestedSchemaFields(JSField.class);
    }

    private Optional<JSSchema> addSchema(@Nullable JsonSchema parentSchema, JsonNode schemaNode, SchemaPathAt schemaPathAt) {
        JsonSchema parentJsonSchema = Optional.ofNullable(parentSchema).orElse(this.rootJsonSchema);
        SchemaPathAt modifiedSchemaPath = SchemaPathAt.from(parentSchema, schemaNode, schemaPathAt);

        String schemaId = id(parentJsonSchema, modifiedSchemaPath);

        return Optional.ofNullable(visitedJSSchemas.computeIfAbsent(schemaId, id -> {
            JSSchema parentJSSchema = visitedJSSchemas.get(id(parentJsonSchema, SchemaPathAt.builder().schemaPath(parentJsonSchema.getSchemaPath()).build()));

            JSSchema jsSchema = JSSchema.builder()
                    .jsonSchema(parentJsonSchema)
                    .parentSchema(parentJSSchema)
                    .schemaPathAt(modifiedSchemaPath)
                    .isOneAnyOf("oneOf".equals(schemaPathAt.schemaPath()))
                    .jsonNode(schemaNode)
                    .build();

            // Recursion protection
            if (parentJSSchema == null || !parentJSSchema.jsonSchema().equals(jsSchema.jsonSchema())) {
                addVertex(jsSchema);
                return jsSchema;
            }

            return null;
        }));
    }

    private JSField addField(JsonSchema parentJsonSchema, JsonNode schemaNode, SchemaPathAt schemaPathAt) {
        Pair<String, String> ids = extractFieldIds(parentJsonSchema, schemaPathAt);
        String parentId = ids.getFirst();
        String fieldId = ids.getSecond();

        Optional<JSSchema> parentJSSchema = Optional.ofNullable(visitedJSSchemas.get(parentId));
        Optional<JSField> parentJSField = Optional.ofNullable(visitedJSFields.get(parentId));

        return visitedJSFields.computeIfAbsent(fieldId, id -> {
            boolean isNested = schemaNode.has("type") && schemaNode.get("type").asText().equals("object");

            Optional<JSField> withSchemaParent = parentJSSchema.map(parentSchema -> {
                JSField fieldNode = JSField.builder()
                        .parentSchema(parentSchema)
                        .isNestedType(isNested)
                        .schemaPathAt(SchemaPathAt.from(parentJsonSchema, schemaNode, schemaPathAt))
                        .jsonNode(schemaNode)
                        .isOneAnyOf(schemaNode.has("oneOf"))
                        .nullable(!JSchemaUtils.isRequired(parentSchema.jsonNode(), JSchemaUtils.name(id)))
                        .build();

                addVertex(fieldNode);

                if (schemaNode.has("oneOf")) {
                    Iterator<JsonNode> itr = schemaNode.get("oneOf").iterator();
                    int idx = 0;
                    while (itr.hasNext()) {
                        JsonNode e = itr.next();
                        JSField child = addField(parentJsonSchema, e, SchemaPathAt.indexed(schemaPathAt, idx));

                        JSEdge.builder()
                                .isNestedType(child.isNestedType())
                                .source(fieldNode)
                                .target(child)
                                .type(child.fieldPathType())
                                .build()
                                .inGraph(this);

                        idx += 1;
                    }
                }

                return fieldNode;
            });

            Optional<JSField> withFieldParent = parentJSField.map(parentField -> {
                JSField fieldNode = JSField.builder()
                        .parentSchema(parentField.parentSchema())
                        .isNestedType(isNested)
                        .schemaPathAt(SchemaPathAt.from(parentJsonSchema, schemaNode, schemaPathAt))
                        .jsonNode(schemaNode)
                        .isOneAnyOf(schemaNode.has("oneOf"))
                        .nullable(!JSchemaUtils.isRequired(parentField.jsonNode(), JSchemaUtils.name(id)))
                        .build();

                addVertex(fieldNode);

                JSEdge.builder()
                        .isNestedType(fieldNode.isNestedType())
                        .source(parentField)
                        .target(fieldNode)
                        .type(fieldNode.fieldPathType())
                        .build()
                        .inGraph(this);

                return fieldNode;
            });

            return Stream.of(withSchemaParent, withFieldParent).flatMap(Optional::stream).findFirst().get();
        });
    }

    private void walk(JsonNode jsonNode) {
        SchemaValidatorsConfig validatorsConfig = new SchemaValidatorsConfig();
        validatorsConfig.addKeywordWalkListener("properties",
                JSSchemaKeywordListener.builder()
                        .visitedJSSchemas(this.visitedJSSchemas)
                        .visitedJSFields(this.visitedJSFields)
                        .graph(this)
                        .build());
        validatorsConfig.addKeywordWalkListener("type",
                JSSchemaKeywordListener.builder()
                        .visitedJSSchemas(this.visitedJSSchemas)
                        .visitedJSFields(this.visitedJSFields)
                        .graph(this)
                        .build());
        validatorsConfig.addKeywordWalkListener("enum",
                JSSchemaKeywordListener.builder()
                        .visitedJSSchemas(this.visitedJSSchemas)
                        .visitedJSFields(this.visitedJSFields)
                        .graph(this)
                        .build());
        validatorsConfig.addKeywordWalkListener("allOf",
                JSSchemaKeywordListener.builder()
                        .visitedJSSchemas(this.visitedJSSchemas)
                        .visitedJSFields(this.visitedJSFields)
                        .graph(this)
                        .build());
        validatorsConfig.addKeywordWalkListener("oneOf",
                JSSchemaKeywordListener.builder()
                        .visitedJSSchemas(this.visitedJSSchemas)
                        .visitedJSFields(this.visitedJSFields)
                        .graph(this)
                        .build());
        jsonSchemaFactory.getSchema(jsonNode, validatorsConfig).walk(jsonNode, false);
    }

    private static Pair<String, String> extractFieldIds(JsonSchema parentJsonSchema, SchemaPathAt schemaPathAt) {
        final String parentId;
        if (parentJsonSchema.getSchemaNode().has("$ref")) {
            parentId = id(parentJsonSchema, SchemaPathAt.builder().schemaPath(parentJsonSchema.getSchemaPath()).build(), true);
        } else {
            parentId = id(parentJsonSchema, SchemaPathAt.builder().schemaPath(parentJsonSchema.getSchemaPath()).build());
        }
        String fieldId = id(parentJsonSchema, schemaPathAt);
        return Pair.of(parentId, fieldId);
    }

    private String extractSchemaIds(JsonSchema parentSchema, JsonNode schemaNode, SchemaPathAt schemaPathAt) {
        JsonSchema schema = Optional.ofNullable(parentSchema).orElse(this.rootJsonSchema);
        SchemaPathAt modifiedSchemaPath = SchemaPathAt.from(parentSchema, schemaNode, schemaPathAt);
        return id(schema, modifiedSchemaPath);
    }

    @Builder
    private static class JSSchemaKeywordListener implements JsonSchemaWalkListener {
        private final JSGraph graph;
        private final ConcurrentHashMap<String, JSSchema> visitedJSSchemas;
        private final ConcurrentHashMap<String, JSField> visitedJSFields;

        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            System.out.printf("start walk= keyword name:%s, schemaPath:%s, at:%s%n",
                    keywordWalkEvent.getKeyWordName(),
                    keywordWalkEvent.getSchemaPath(),
                    keywordWalkEvent.getAt());

            SchemaPathAt schemaPathAt = SchemaPathAt.builder()
                    .schemaPath(keywordWalkEvent.getSchemaPath())
                    .at(keywordWalkEvent.getAt())
                    .build();

            switch(keywordWalkEvent.getKeyWordName()) {
                case "properties":
                    if (keywordWalkEvent.getSchemaNode().has("$id") || !keywordWalkEvent.getSchemaPath().startsWith("oneOf")) {
                        Optional<JSSchema> schema = graph.addSchema(
                                keywordWalkEvent.getParentSchema(),
                                keywordWalkEvent.getSchemaNode(),
                                schemaPathAt);

                        // Break infinite recursion
                        if (schema.isEmpty()) {
                            return WalkFlow.SKIP;
                        }
                    }
                    break;
                case "oneOf":
                case "allOf":
                case "type":
                case "enum":
                    if (keywordWalkEvent.getParentSchema() != null &&
                            !keywordWalkEvent.getSchemaPath().endsWith("/items")) {

                        JSField field = graph.addField(keywordWalkEvent.getParentSchema(), keywordWalkEvent.getSchemaNode(), schemaPathAt);

                        JSEdge.builder()
                                .isNestedType(field.isNestedType())
                                .source(field.parentSchema())
                                .target(field)
                                .type(field.fieldPathType())
                                .build()
                                .inGraph(graph);

                    }
                    break;
                default:
                    throw new IllegalStateException(String.format("Unhandled keyword %s", keywordWalkEvent.getKeyWordName()));
            }

            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {
        }
    }

    @Override
    protected void attachNestedSchemaFields(Map<String, List<JSField>> fieldMap, JSField nestedSchemaField) {
        fieldMap.getOrDefault(nestedSchemaField.fullName(), List.of()).forEach(target -> {
            JSEdge.builder()
                    .source(nestedSchemaField)
                    .target(target)
                    .type(target.fieldPathType())
                    .isNestedType(target.isNestedType())
                    .build().inGraph(this);
        });
    }

    public String getSchemaSource() {
        return schemaSource;
    }

    @Override
    public String getFullName() {
        return rootJSSchema.fullName();
    }

    public SpecVersion.VersionFlag getSpecVersion() {
        return specVersion;
    }

    @Override
    public JSSchema root() {
        return rootJSSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        JSGraph that = (JSGraph) o;

        return rootJSSchema.equals(that.rootJSSchema);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rootJSSchema.hashCode();
        return result;
    }

    public String getHash() {
        return String.valueOf(super.hashCode());
    }
}
