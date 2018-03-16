package uk.ac.bris.cs.scotlandyard.model;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

import java.util.*;
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
    private int currentRound = 0;
    private int currentPlayer = 0;
    private int mrxLastlocation = 0;

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
        playerConfigurations.add(0, mrX);
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
        if (spectator == null) {
            throw new NullPointerException("The spectator is null");
        }
        spectators.forEach(existingspectator -> {
            if (spectator == existingspectator) {
                throw new IllegalArgumentException("");
            }
        });
        spectators.add(spectator);
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        if (spectator == null) {
            throw new NullPointerException("Not sure what's happening");
        }
        if (spectators.isEmpty()) {
            throw new IllegalArgumentException("There are no spectators to remove");
        }

        spectators.remove(spectator);
    }

    @Override
    public void startRotate() {
        // TODO
        throw new RuntimeException("Implement me");
    }

    @Override
    public Collection<Spectator> getSpectators() {
        return Collections.unmodifiableList(spectators);
    }

    @Override
    public List<Colour> getPlayers() {

        List<Colour> playerColours = players.stream()
                .map(player -> player.colour())
                .collect(Collectors.toList());
        return Collections.unmodifiableList(playerColours);

    }

    @Override
    public Set<Colour> getWinningPlayers() {
        return Collections.emptySet();
    }


    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        if (getCurrentRound() == 3 || getCurrentRound() == 8 || getCurrentRound() == 12) {
            mrxLastlocation = players.get(0).location();
        }
        if (colour == Colour.BLACK) {
            return Optional.of(mrxLastlocation);
        }
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
        return false;
    }

    @Override
    public Colour getCurrentPlayer() {
        return this.getPlayers().get(currentPlayer);
    }

    @Override
    public int getCurrentRound() {
        return currentRound;
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
