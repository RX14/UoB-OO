package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

import com.google.common.collect.Streams;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {

    private final List<Boolean> rounds;
    private final Graph<Integer, Transport> graph;
    private final PlayerConfiguration mrX;
    private final List<PlayerConfiguration> detectives = new ArrayList<>();
    private final List<ScotlandYardPlayer> players;
    private final List<Spectator> spectators = new ArrayList<>();

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

        detectives.add(Objects.requireNonNull(firstDetective));
        detectives.addAll(Arrays.asList(Objects.requireNonNull(restOfTheDetectives)));

        detectives.forEach(detective -> Objects.requireNonNull(detective));

        List<PlayerConfiguration> playerConfigurations = new ArrayList<>(detectives);
        playerConfigurations.add(mrX);
        if (duplicates(playerConfigurations.stream().map(player -> player.colour))) {
            throw new IllegalArgumentException("Duplicate player colour");
        }
        if (duplicates(playerConfigurations.stream().map(player -> player.location))) {
            throw new IllegalArgumentException("Duplicate player location");
        }

        playerConfigurations.forEach(player -> {
            for (Ticket ticket : Ticket.values()) {
                if (!player.tickets.containsKey(ticket)) {
                    throw new IllegalArgumentException("Player " + player + " does not have ticket type " + ticket);
                }
            }
        });

        players = playerConfigurations.stream().map(playerConfiguration ->
                new ScotlandYardPlayer(
                        playerConfiguration.player,
                        playerConfiguration.colour,
                        playerConfiguration.location,
                        playerConfiguration.tickets
                )
        ).collect(Collectors.toList());

        detectives.forEach(detective -> {
            if (detective.tickets.get(Ticket.SECRET) != 0) {
                throw new IllegalArgumentException("Detective " + detective + " has a secret ticket, but shouldn't");
            }
            if (detective.tickets.get(Ticket.DOUBLE) != 0) {
                throw new IllegalArgumentException("Detective " + detective + " has a double ticket, but shouldn't");
            }
        });
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
        spectators.add(spectator);
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        spectators.remove(spectator);
    }

    @Override
    public void startRotate() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Collection<Spectator> getSpectators() {
        return spectators;
    }

    @Override
    public List<Colour> getPlayers() {
        return players.stream().map(player -> player.colour()).collect(Collectors.toList());
    }

    @Override
    public Set<Colour> getWinningPlayers() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        return players.stream()
                .filter(player -> player.colour() == colour)
                .findFirst()
                .map(player -> player.location());
    }

    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        return players.stream()
                .filter(player -> player.colour() == colour)
                .findFirst()
                .map(player -> player.tickets().get(ticket));
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
    public List<Boolean> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    @Override
    public Graph<Integer, Transport> getGraph() {
        return new ImmutableGraph<>(graph);
    }

}
