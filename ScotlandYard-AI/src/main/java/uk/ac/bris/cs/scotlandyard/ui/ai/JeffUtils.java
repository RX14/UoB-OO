package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class JeffUtils {

    public static int score(Move move, Graph<Integer, Transport> graph, Map<Integer, Integer> distances, int currentLocation) {
        Integer moveDestination = JeffUtils.getDestination(move, currentLocation);
        int destinationConnectivity = graph.getEdgesFrom(graph.getNode(moveDestination)).size();
        int distance = distances.get(moveDestination);

        return destinationConnectivity * distance;
    }

    /**
     * Generate the distances at each node on a graph from the closest of a set of possible starting locations. Each
     * edge is assumed to have a weight of one.
     *
     * @param graph             the graph for which to calculate the distance map
     * @param startingLocations a set of possible starting locations to calculate the distances at each node from
     * @return a map of graph keys to the distance at the node with that key from the closest of the starting locations
     */
    public static <Key, Value> Map<Key, Integer> generateDistances(Graph<Key, Value> graph, Set<Key> startingLocations) {
        List<NodeWrapper<Key>> startingNodes = startingLocations.stream()
                .map(number -> new NodeWrapper<>(graph.getNode(number), 0))
                .collect(Collectors.toList());

        Map<Key, Integer> nodeDistances = new HashMap<>();
        PriorityQueue<NodeWrapper<Key>> unmarkedNodes = new PriorityQueue<>(startingNodes);

        while (!unmarkedNodes.isEmpty()) {
            // Take the lowest-distance potentially-unmarked node
            NodeWrapper<Key> node = unmarkedNodes.poll();

            // If we've already marked the node, ignore it
            if (nodeDistances.containsKey(node.node.value())) continue;

            // Mark the node
            nodeDistances.put(node.node.value(), node.distance);

            // Add it's neighbours to the list of unmarked nodes
            graph.getEdgesFrom(node.node).forEach(edge -> {
                if (!nodeDistances.containsKey(edge.destination().value())) {
                    int newDistance = node.distance + 1;
                    unmarkedNodes.add(new NodeWrapper<Key>(edge.destination(), newDistance));
                }
            });
        }

        return nodeDistances;
    }

    /**
     * Find whether the move should be played or not, depending on the current distance to the closest detective.
     *
     * @param move            the candidate move to play
     * @param currentDistance current distance from the closest detective
     * @param allMoves        a set of all the moves that can be played
     * @return whether to consider the move for play
     */
    public static boolean shouldPlayMove(Move move, int currentDistance, Set<Move> allMoves) {
        if (move instanceof PassMove) {
            // We can always play pass moves (if available)
            return true;
        } else if (move instanceof TicketMove) {
            boolean hasOnlySecretTickets = allMoves.stream()
                    .allMatch(move1 -> move1 instanceof TicketMove && ((TicketMove) move1).ticket() == Ticket.SECRET);

            if (((TicketMove) move).ticket() == Ticket.SECRET) {
                // Only use secret tickets when we're close (3 moves away) or the only option
                return currentDistance <= 3 || hasOnlySecretTickets;
            } else {
                return true;
            }
        } else if (move instanceof DoubleMove) {
            // Only use double tickets when we're very close (2 moves away)
            return currentDistance <= 2;
        } else {
            throw new IllegalStateException("Unknown move type");
        }
    }

    /**
     * Gets the destination of the given move.
     *
     * @param move            the move to find the destination off
     * @param currentLocation the player's current location
     * @return the number of the move destination on the board
     */
    public static Integer getDestination(Move move, int currentLocation) {
        if (move instanceof PassMove) {
            return currentLocation;
        } else if (move instanceof TicketMove) {
            return ((TicketMove) move).destination();
        } else if (move instanceof DoubleMove) {
            return ((DoubleMove) move).finalDestination();
        } else {
            throw new IllegalStateException("Unknown move type");
        }
    }

    private static class NodeWrapper<T> implements Comparable<NodeWrapper> {
        final Node<T> node;
        final int distance;

        NodeWrapper(Node<T> node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeWrapper other) {
            return Integer.compare(distance, other.distance);
        }
    }
}
