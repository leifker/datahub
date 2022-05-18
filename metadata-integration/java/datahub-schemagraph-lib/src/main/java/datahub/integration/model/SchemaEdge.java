package datahub.integration.model;


public interface SchemaEdge<N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>,
        F extends FieldNode<N, S, F, E>, E extends SchemaEdge<N, S, F, E>> {

    E getInstance();
    N edgeTarget();
    N edgeSource();
    String getType();
    boolean isNestedType();

    default <G extends SchemaGraph<N, S, F, E>> E inGraph(G g) {
        g.addEdge(edgeSource(), edgeTarget(), getInstance());
        return getInstance();
    }
}
