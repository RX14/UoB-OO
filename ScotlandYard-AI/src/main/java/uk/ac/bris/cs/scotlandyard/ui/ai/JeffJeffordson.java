package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.Comparator;
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
        public void makeMove(ScotlandYardView view, int currentLocation, Set<Move> moves, Consumer<Move> callback) {
            Set<Integer> detectiveLocations = view.getPlayers().stream()
                    .filter(colour -> colour != Colour.BLACK)
                    .map(colour -> view.getPlayerLocation(colour)
                            .orElseThrow(() -> new IllegalStateException("Player " + colour + " has no location")))
                    .collect(Collectors.toSet());

            Map<Integer, Integer> distances = JeffUtils.generateDistances(view.getGraph(), detectiveLocations);
            int currentDistance = distances.get(currentLocation);

            Move moveToMake = moves.stream()
                    .filter(move -> JeffUtils.shouldPlayMove(move, currentDistance, moves))
                    .max(Comparator.comparingInt(move -> distances.get(JeffUtils.getDestination(move, currentLocation))))
                    .orElseThrow(() -> new RuntimeException("Could not find a move to make!"));

            callback.accept(moveToMake);
        }
    }
}
