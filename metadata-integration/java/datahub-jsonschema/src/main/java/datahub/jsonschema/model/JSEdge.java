package datahub.jsonschema.model;

import datahub.integration.model.SchemaEdge;
import lombok.Builder;
import lombok.Getter;
import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;

@Builder
@Getter
public class JSEdge extends DefaultEdge implements SchemaEdge<JSElement, JSSchema, JSField, JSEdge> {

    @Builder.Default
    protected final String type = "";
    @Builder.Default
    protected final boolean isNestedType = false;

    private final JSElement source;
    private final JSElement target;

    @Override
    public JSElement edgeTarget() {
        return target;
    }
    @Override
    public JSElement edgeSource() {
        return source;
    }

    @Override
    public JSEdge getInstance() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSEdge that = (JSEdge) o;
        return isNestedType == that.isNestedType && type.equals(that.type) && source.equals(that.source) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isNestedType, source, target);
    }
}
