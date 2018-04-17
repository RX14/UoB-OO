package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

// TODO name the AI
@ManagedAI("Jeff")
public class MyAI implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer(colour);
	}

	// TODO A sample player that selects a random move
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
			// TODO do something interesting here; find the best move
			// picks a random move
			callback.accept(new ArrayList<>(moves).get(random.nextInt(moves.size())));

		}
	}
}
