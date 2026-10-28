package graph;

import datastructures.HashMap;
import datastructures.LinkedList;
import java.util.ArrayList;
import java.util.List;


// adjacency list implementation of the Graph interface
// uses our custom HashMap and LinkedList instead of java's built-in ones
// edges are stored in both directions since the graph is undirected
public class AdjacencyList implements Graph {

    private HashMap<Integer, Node> nodeMap;            // maps node id to the node object
    private HashMap<Integer, LinkedList<Edge>> adjList; // maps node id to its list of edges
    private int edgeCount;


    /** creates an empty adjacency list graph */
    public AdjacencyList() {
        nodeMap = new HashMap<>();
        adjList = new HashMap<>();
        edgeCount = 0;
    }


    // node operations

    /**
     * adds a node to the graph and creates an empty edge list for it
     *
     * @param node the node to add
     */
    @Override
    public void addNode(Node node) {
        nodeMap.put(node.getId(), node);
        adjList.putIfAbsent(node.getId(), new LinkedList<>());
    }


    /**
     * removes a node and all the edges connected to it
     *
     * @param nodeId the id of the node to remove
     */
    @Override
    public void removeNode(int nodeId) {
        if (!nodeMap.containsKey(nodeId)) {
            return; // node doesn't exist so nothing to remove
        }

        LinkedList<Edge> edges = adjList.get(nodeId);
        if (edges == null) {
            edges = new LinkedList<>();
        }

        // go through all edges of this node and remove the reverse side from neighbours
        for (Edge edge : edges) {
            Node neighbour = edge.getOtherNode(nodeMap.get(nodeId));
            if (neighbour != null) {
                LinkedList<Edge> neighbourEdges = adjList.get(neighbour.getId());
                if (neighbourEdges != null) {
                    // remove any edge that points back to the node being deleted
                    neighbourEdges.removeIf(e ->
                        e.getOtherNode(neighbour) != null &&
                        e.getOtherNode(neighbour).getId() == nodeId
                    );
                }
            }
            edgeCount--;
        }

        // finally remove the node itself from both maps
        nodeMap.remove(nodeId);
        adjList.remove(nodeId);
    }

    /**
     * returns the node with the given id, or null if it doesn't exist
     *
     * @param nodeId the id to look up
     * @return the node, or null
     */
    @Override
    public Node getNode(int nodeId) { return nodeMap.get(nodeId); }

    /**
     * checks if a node with the given id exists in the graph
     *
     * @param nodeId the id to check
     * @return true if found, false otherwise
     */
    @Override
    public boolean containsNode(int nodeId) { return nodeMap.containsKey(nodeId); }

    /**
     * returns all nodes currently in the graph as a list
     *
     * @return list of all nodes
     */
    @Override
    public List<Node> getAllNodes() {
        List<Node> result = new ArrayList<>();
        for (Node n : nodeMap.values()) {
            result.add(n);
        }
        return result;
    }


    // edge operations

    /**
     * adds an edge between two nodes with the given weight
     * skips if either node doesn't exist, if its a self-loop, or if the edge already exists
     *
     * @param sourceId the starting node id
     * @param destinationId the ending node id
     * @param weight the edge weight (euclidean distance between the nodes' feature vectors)
     */
    @Override
    public void addEdge(int sourceId, int destinationId, double weight) {
        if (!nodeMap.containsKey(sourceId) || !nodeMap.containsKey(destinationId)) {
            return; // can only add edge if both nodes exist
        }
        if (sourceId == destinationId) {
            return; // no self-loops
        }
        if (containsEdge(sourceId, destinationId)) {
            return; // avoid duplicate edges
        }

        Node src = nodeMap.get(sourceId);
        Node dest = nodeMap.get(destinationId);

        // add in both directions since graph is undirected
        adjList.get(sourceId).add(new Edge(src, dest, weight));
        adjList.get(destinationId).add(new Edge(dest, src, weight));
        edgeCount++;
    }

    /**
     * removes the edge between two nodes in both directions
     *
     * @param sourceId one end of the edge
     * @param destinationId the other end of the edge
     */
    @Override
    public void removeEdge(int sourceId, int destinationId) {
        if (!nodeMap.containsKey(sourceId) || !nodeMap.containsKey(destinationId)) {
            return;
        }

        boolean removed = false;

        // remove the forward direction
        LinkedList<Edge> srcEdges = adjList.get(sourceId);
        if (srcEdges != null) {
            removed = srcEdges.removeIf(e -> e.getDestination().getId() == destinationId);
        }

        // remove the reverse direction
        LinkedList<Edge> destEdges = adjList.get(destinationId);
        if (destEdges != null) {
            destEdges.removeIf(e -> e.getDestination().getId() == sourceId);
        }

        if (removed) {
            edgeCount--;
        }
    }

    /**
     * updates the weight of an existing edge between two nodes
     *
     * @param sourceId one end of the edge
     * @param destinationId the other end of the edge
     * @param newWeight the new weight to set
     * @return true if the edge was found and updated, false if it didn't exist
     */
    @Override
    public boolean updateEdge(int sourceId, int destinationId, double newWeight) {
        if (!containsEdge(sourceId, destinationId)) {
            return false;
        }

        boolean updated = false;

        // update forward direction
        LinkedList<Edge> srcEdges = adjList.get(sourceId);
        if (srcEdges != null) {
            for (Edge e : srcEdges) {
                if (e.getDestination().getId() == destinationId) {
                    e.setWeight(newWeight);
                    updated = true;
                    break;
                }
            }
        }

        // update reverse direction
        LinkedList<Edge> destEdges = adjList.get(destinationId);
        if (destEdges != null) {
            for (Edge e : destEdges) {
                if (e.getDestination().getId() == sourceId) {
                    e.setWeight(newWeight);
                    updated = true;
                    break;
                }
            }
        }

        return updated;
    }

    /**
     * checks if an edge exists between two nodes
     *
     * @param sourceId one end of the edge
     * @param destinationId the other end
     * @return true if the edge exists, false otherwise
     */
    @Override
    public boolean containsEdge(int sourceId, int destinationId) {
        LinkedList<Edge> edges = adjList.get(sourceId);
        if (edges == null) {
            return false;
        }
        for (Edge e : edges) {
            if (e.getDestination().getId() == destinationId) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns every edge in the graph, each edge included only once
     *
     * @return list of all edges
     */
    @Override
    public List<Edge> getAllEdges() {
        List<Edge> all = new ArrayList<>();
        for (LinkedList<Edge> edges : adjList.values()) {
            for (Edge e : edges) {
                // only add once per edge by taking the one where source id < destination id
                if (e.getSource().getId() < e.getDestination().getId()) {
                    all.add(e);
                }
            }
        }
        return all;
    }

    /**
     * returns all edges connected to a specific node
     *
     * @param nodeId the id of the node
     * @return list of edges attached to that node
     */
    @Override
    public List<Edge> getEdgesOfNode(int nodeId) {
        List<Edge> result = new ArrayList<>();
        LinkedList<Edge> edges = adjList.get(nodeId);
        if (edges != null) {
            for (Edge e : edges) {
                result.add(e);
            }
        }
        return result;
    }


    // query methods

    /**
     * returns all nodes directly connected to the given node
     *
     * @param nodeId the id of the node to check
     * @return list of neighbouring nodes
     */
    @Override
    public List<Node> getNeighbours(int nodeId) {
        List<Node> neighbours = new ArrayList<>();
        LinkedList<Edge> edges = adjList.get(nodeId);
        if (edges != null) {
            for (Edge e : edges) {
                neighbours.add(e.getDestination());
            }
        }
        return neighbours;
    }

    /**
     * returns how many nodes are in the graph
     *
     * @return total number of nodes
     */
    @Override
    public int nodeCount() { return nodeMap.size(); }

    /**
     * returns how many edges are in the graph
     *
     * @return total number of edges
     */
    @Override
    public int edgeCount() { return edgeCount; }

    /**
     * removes all nodes and edges from the graph
     */
    @Override
    public void clear() {
        nodeMap.clear();
        adjList.clear();
        edgeCount = 0;
    }

    @Override
    public String toString() {
        return "AdjacencyList[nodes=" + nodeCount() + ", edges=" + edgeCount() + "]";
    }
}