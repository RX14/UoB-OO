package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Transport;

import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class Utils {
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
        int distance;

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
