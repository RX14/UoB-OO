package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ManagedAI("Jeff")
public class JeffJeffordson implements PlayerFactory {

    @Override
    public Player createPlayer(Colour colour) {
        return new MyPlayer(colour);
    }

    private static class MyPlayer implements Player {
        private final Colour colour;

        MyPlayer(Colour colour) {
            this.colour = colour;
            if (colour != Colour.BLACK) throw new IllegalArgumentException("The AI is for MrX only");
        }

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
            List<Integer> detectiveLocations = view.getPlayers().stream()
                    .filter(colour -> colour != Colour.BLACK)
                    .map(colour -> view.getPlayerLocation(colour).get())
                    .collect(Collectors.toList());

            Map<Integer, Integer> distances = Utils.generateDistances(view.getGraph(), detectiveLocations);

            Move moveToMake = moves.stream().max(Comparator.comparingInt(o -> distances.get(getDestination(location, o)))).get();

            callback.accept(moveToMake);
        }

        private Integer getDestination(int location, Move move) {
            if (move instanceof PassMove) {
                return location;
            } else if (move instanceof TicketMove) {
                return ((TicketMove) move).destination();
            } else if (move instanceof DoubleMove) {
                return ((DoubleMove) move).finalDestination();
            } else {
                throw new IllegalStateException("Unknown move type");
            }
        }
    }
}
