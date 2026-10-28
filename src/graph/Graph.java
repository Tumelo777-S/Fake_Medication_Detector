package graph;
import java.util.List;


// interface that defines all the operations a graph must support
// the actual implementation is in AdjacencyList
public interface Graph {

    // node operations

    /** adds a node to the graph */
    void addNode(Node node);

    /** removes a node and all its connected edges by id */
    void removeNode(int nodeId);

    /** returns the node with the given id, or null if not found */
    Node getNode(int nodeId);

    /** returns true if a node with the given id exists */
    boolean containsNode(int nodeId);

    /** returns a list of all nodes in the graph */
    List<Node> getAllNodes();

    // edge operations

    /** adds a weighted edge between two nodes */
    void addEdge(int sourceId, int destinationId, double weight);

    /** removes the edge between two nodes */
    void removeEdge(int sourceId, int destinationId);

    /** updates the weight of an existing edge, returns true if it was found */
    boolean updateEdge(int sourceId, int destinationId, double newWeight);

    /** returns true if an edge exists between the two given nodes */
    boolean containsEdge(int sourceId, int destinationId);

    /** returns all edges in the graph, each included only once */
    List<Edge> getAllEdges();

    /** returns all edges connected to the given node */
    List<Edge> getEdgesOfNode(int nodeId);

    // query methods

    /** returns all nodes that are directly connected to the given node */
    List<Node> getNeighbours(int nodeId);

    /** returns the total number of nodes */
    int nodeCount();

    /** returns the total number of edges */
    int edgeCount();

    /** removes everything from the graph */
    void clear();
}