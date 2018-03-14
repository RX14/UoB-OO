package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

import com.google.common.collect.Streams;
import uk.ac.bris.cs.gamekit.graph.Graph;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {

    private final List<Boolean> rounds;
    private final Graph<Integer, Transport> graph;
    private final PlayerConfiguration mrX;
    private final List<PlayerConfiguration> detectives;

    public ScotlandYardModel(List<Boolean> rounds,
                             Graph<Integer, Transport> graph,
                             PlayerConfiguration mrX,
                             PlayerConfiguration firstDetective, PlayerConfiguration... restOfTheDetectives) {
        this.rounds = Objects.requireNonNull(rounds);
        if (rounds.isEmpty()) {
            throw new IllegalArgumentException("Rounds was empty");
        }

        this.graph = Objects.requireNonNull(graph);
        if (graph.isEmpty()) {
            throw new IllegalArgumentException("Graph was empty");
        }

        this.mrX = Objects.requireNonNull(mrX);
        if (mrX.colour != Colour.BLACK) {
            throw new IllegalArgumentException("MrX should be black");
        }

        this.detectives = new ArrayList<>();
        detectives.add(Objects.requireNonNull(firstDetective));
        detectives.addAll(Arrays.asList(Objects.requireNonNull(restOfTheDetectives)));

        detectives.forEach(detective -> Objects.requireNonNull(detective));

        List<PlayerConfiguration> allPlayers = new ArrayList<>(detectives);
        allPlayers.add(mrX);
        if (duplicates(allPlayers.stream().map(player -> player.colour))) {
            throw new IllegalArgumentException("Duplicate player colour");
        }
        if (duplicates(allPlayers.stream().map(player -> player.location))) {
            throw new IllegalArgumentException("Duplicate player location");
        }
    }

    private <T> boolean duplicates(Stream<T> stream) {
        Set<T> set = new HashSet<>();
        return stream.anyMatch(item -> {
            if (set.contains(item)) return true;
            set.add(item);
            return false;
        });
    }

    @Override
    public void registerSpectator(Spectator spectator) {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public void startRotate() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Collection<Spectator> getSpectators() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public List<Colour> getPlayers() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Set<Colour> getWinningPlayers() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public boolean isGameOver() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Colour getCurrentPlayer() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public int getCurrentRound() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public List<Boolean> getRounds(){ return rounds; }

    @Override
    public Graph<Integer, Transport> getGraph() {
        return graph;
    }

}
