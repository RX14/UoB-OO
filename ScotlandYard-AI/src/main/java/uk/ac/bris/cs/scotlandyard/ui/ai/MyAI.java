package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;

import org.omg.PortableInterceptor.INACTIVE;
import sun.font.CompositeGlyphMapper;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

// TODO name the AI
@ManagedAI("Jeff")
public class MyAI implements PlayerFactory {

    @Override
    public Player createPlayer(Colour colour) {
        return new MyPlayer(colour);
    }

    private static class MyPlayer implements Player {
        private final Colour colour;
        private final Random random = new Random();

        public MyPlayer(Colour colour) {
            this.colour = colour;
            if (colour != Colour.BLACK) throw new IllegalArgumentException("The AI is for MrX only");
        }

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                             Consumer<Move> callback) {

        }

        private int distanceBetweenLocations(Graph<Integer, Transport> graph, int from, int to) {
            Map<Node<Integer>,Integer> distances = new HashMap<>();
            PriorityQueue<Node<Integer>> nodes = new PriorityQueue<>();

            distances.put(graph.getNode(from), 0);

            //graph.getEdgesFrom(graph.getNode(from));

            return 0;
        }
    }
}
