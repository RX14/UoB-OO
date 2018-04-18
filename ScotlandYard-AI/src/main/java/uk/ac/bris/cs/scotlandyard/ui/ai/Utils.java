package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Transport;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    public static Map<Integer, Integer> generateDistances(Graph<Integer, Transport> graph, List<Integer> startingLocations) {
        List<NodeWrapper> startingNodes = startingLocations.stream()
                .map(number -> new NodeWrapper(graph.getNode(number), 0))
                .collect(Collectors.toList());

        Map<Integer, Integer> nodeDistances = new HashMap<>();
        PriorityQueue<NodeWrapper> unmarkedNodes = new PriorityQueue<>(startingNodes);

        while (!unmarkedNodes.isEmpty()) {
            // Take the lowest-distance potentially-unmarked node
            NodeWrapper node = unmarkedNodes.poll();

            // If we've already marked the node, ignore it
            if (nodeDistances.containsKey(node.node.value())) continue;

            // Mark the node
            nodeDistances.put(node.node.value(), node.distance);

            // Add it's neighbours to the list of unmarked nodes
            graph.getEdgesFrom(node.node).forEach(edge -> {
                if (!nodeDistances.containsKey(edge.destination().value())) {
                    int newDistance = node.distance + 1;
                    unmarkedNodes.add(new NodeWrapper(edge.destination(), newDistance));
                }
            });
        }

        return nodeDistances;
    }

    public static int distanceBetween(Graph<Integer, Transport> graph, int from, int to) {
        Set<Node<Integer>> markedNodes = new HashSet<>();
        PriorityQueue<NodeWrapper> unmarkedNodes = new PriorityQueue<>();

        unmarkedNodes.add(new NodeWrapper(graph.getNode(from), 0));

        while (true) {
            NodeWrapper node = unmarkedNodes.poll();
            Objects.requireNonNull(node);

            if (node.node.value() == to) return node.distance;

            markedNodes.add(node.node);

            graph.getEdgesFrom(node.node).forEach(edge -> {
                if (!markedNodes.contains(edge.destination())) {
                    int newDistance = node.distance + 1;
                    unmarkedNodes.add(new NodeWrapper(edge.destination(), newDistance));
                }
            });
        }
    }

    private static class NodeWrapper implements Comparable<NodeWrapper> {
        final Node<Integer> node;
        final int distance;

        NodeWrapper(Node<Integer> node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeWrapper other) {
            return Integer.compare(distance, other.distance);
        }
    }
}
