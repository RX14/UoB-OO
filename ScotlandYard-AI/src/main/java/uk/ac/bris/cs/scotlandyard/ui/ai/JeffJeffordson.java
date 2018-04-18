package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.function.Consumer;

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

        }
    }
}
