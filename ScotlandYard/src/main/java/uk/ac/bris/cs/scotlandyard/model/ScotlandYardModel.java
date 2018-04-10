package uk.ac.bris.cs.scotlandyard.model;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {
    private final List<Boolean> rounds;
    private final Graph<Integer, Transport> graph;
    private final List<ScotlandYardPlayer> players;
    private final List<Spectator> spectators = new ArrayList<>();
    private Set<Colour> winners = new HashSet<>();
    private int currentRound = 0;
    private int currentPlayerIndex = 0;
    private int mrXLastSeenLocation = 0;

    public ScotlandYardModel(List<Boolean> rounds,
                             Graph<Integer, Transport> graph,
                             PlayerConfiguration mrXConfiguration,
                             PlayerConfiguration firstDetectiveConfiguration, PlayerConfiguration... restOfTheDetectivesConfigurations) {
        this.rounds = Objects.requireNonNull(rounds);
        if (rounds.isEmpty()) {
            throw new IllegalArgumentException("Rounds was empty");
        }

        this.graph = Objects.requireNonNull(graph);
        if (graph.isEmpty()) {
            throw new IllegalArgumentException("Graph was empty");
        }

        Objects.requireNonNull(mrXConfiguration);
        if (mrXConfiguration.colour != Colour.BLACK) {
            throw new IllegalArgumentException("MrX should be black");
        }

        List<PlayerConfiguration> detectiveConfigurations = new ArrayList<>();
        detectiveConfigurations.add(Objects.requireNonNull(firstDetectiveConfiguration));
        detectiveConfigurations.addAll(Arrays.asList(Objects.requireNonNull(restOfTheDetectivesConfigurations)));

        detectiveConfigurations.forEach(detective -> Objects.requireNonNull(detective));

        List<PlayerConfiguration> playerConfigurations = new ArrayList<>(detectiveConfigurations);
        playerConfigurations.add(0, mrXConfiguration);
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

        detectiveConfigurations.forEach(detective -> {
            if (detective.tickets.get(Ticket.SECRET) != 0) {
                throw new IllegalArgumentException("Detective " + detective + " has a secret ticket, but shouldn't");
            }
            if (detective.tickets.get(Ticket.DOUBLE) != 0) {
                throw new IllegalArgumentException("Detective " + detective + " has a double ticket, but shouldn't");
            }
        });

        checkGameOver(true, getMrX());
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

    private void makeMove() {
        ScotlandYardPlayer currentPlayer = players.get(currentPlayerIndex);
        Set<Move> playerMoves = generateValidMoves(currentPlayer);
        currentPlayer.player().makeMove(this, currentPlayer.location(), playerMoves, move -> moveMade(playerMoves, move));
    }

    @Override
    public void startRotate() {
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over");
        }
        makeMove();
    }

    private void moveMade(Set<Move> allowedMoves, Move move) {
        Objects.requireNonNull(move);
        if (!allowedMoves.contains(move)) {
            throw new IllegalArgumentException("Player did not play a valid move");
        }

        ScotlandYardPlayer movePlayer = players.stream()
                .filter(player -> player.colour() == move.colour())
                .findFirst().orElseThrow(() -> new IllegalStateException("BUG: Move made for non-existent player"));

        boolean roundOver = currentPlayerIndex == players.size() - 1;

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Move visibleMove = null;

        if (move instanceof TicketMove) {
            Ticket ticket = ((TicketMove) move).ticket();

            movePlayer.removeTicket(ticket);
            if (move.colour() != Colour.BLACK) {
                getMrX().addTicket(ticket);
            }

            movePlayer.location(((TicketMove) move).destination());

            if (move.colour() == Colour.BLACK && !isMrXPositionKnownToPlayers()) {
                visibleMove = new TicketMove(move.colour(), ticket, 0);
            } else {
                visibleMove = move;
            }
        }

        if (move instanceof PassMove) {
            visibleMove = move;
        }

        //Below needs to be cleaned up
        if (move instanceof DoubleMove) {
            DoubleMove doubleMove = (DoubleMove) move;

            final DoubleMove hiddenMove;
            if (isMrXPositionKnownToPlayers()) {
                hiddenMove = doubleMove;
            } else {
                hiddenMove = new DoubleMove(move.colour(),
                        doubleMove.firstMove().ticket(), 0,
                        doubleMove.secondMove().ticket(), 0);
            }

            movePlayer.removeTicket(Ticket.DOUBLE);
            spectators.forEach(spectator -> spectator.onMoveMade(this, hiddenMove));

            currentRound++;
            spectators.forEach(spectator -> spectator.onRoundStarted(this, currentRound));

            movePlayer.removeTicket(doubleMove.firstMove().ticket());
            movePlayer.location(doubleMove.firstMove().destination());
            spectators.forEach(spectator -> spectator.onMoveMade(this, hiddenMove.firstMove()));

            movePlayer.removeTicket(doubleMove.secondMove().ticket());
            movePlayer.location(doubleMove.secondMove().destination());
            visibleMove = hiddenMove.secondMove();
        }

        if (move.colour() == Colour.BLACK) {
            currentRound++;
            spectators.forEach(spectator -> spectator.onRoundStarted(this, currentRound));
        }

        Objects.requireNonNull(visibleMove, "BUG: Visible move was not initialized");
        final Move javaIsTheBigGay = visibleMove;
        spectators.forEach(spectator -> spectator.onMoveMade(this, javaIsTheBigGay));

        if (checkGameOver(roundOver, movePlayer)) return;

        if (roundOver) {
            spectators.forEach(spectator -> spectator.onRotationComplete(this));
        } else {
            makeMove();
        }
    }

    private boolean checkGameOver(boolean roundOver, ScotlandYardPlayer player) {
        boolean mrXWon = false;
        boolean detectivesWon = false;

        if (player.colour() != Colour.BLACK && player.location() == getMrX().location()) {
            detectivesWon = true;
        }

        if (roundOver) {
            mrXWon = currentRound == rounds.size() ||
                    players.stream()
                            .filter(player1 -> !player1.isMrX())
                            .map(this::generateValidMoves)
                            .allMatch(set -> set.stream().allMatch(playerMove -> playerMove instanceof PassMove));

            detectivesWon = detectivesWon || generateValidMoves(getMrX()).isEmpty();
        }

        if (mrXWon) {
            winners.add(Colour.BLACK);
        }

        if (detectivesWon) {
            players.forEach(player1 -> {
                if (player1.colour() != Colour.BLACK) {
                    winners.add(player1.colour());
                }
            });
        }

        if (mrXWon || detectivesWon) {
            spectators.forEach(spectator -> spectator.onGameOver(this, winners));
            return true;
        } else {
            return false;
        }
    }

    private Set<Move> generateValidMoves(ScotlandYardPlayer currentPlayer) {
        Set<Move> playerMoves = new HashSet<>();
        Set<DoubleMove> doubles = new HashSet<>();

        //This will need to be cleaned up and re written
        Set<TicketMove> ticketmoves = generateTicketMoves(currentPlayer);
        if (currentPlayer.hasTickets(Ticket.DOUBLE) && currentPlayer.colour() == Colour.BLACK && currentRound + 2 < rounds.size()) {
            ticketmoves.forEach(move -> {
                doubles.addAll(generateDoubleMoves(currentPlayer, move));
            });

        }

        playerMoves.addAll(ticketmoves);
        playerMoves.addAll(doubles);
        //if moves still empty here mrx loses
        if (currentPlayer.colour() != Colour.BLACK && playerMoves.isEmpty()) {
            playerMoves.add(new PassMove(currentPlayer.colour()));
        }
        return playerMoves;
    }

    private Set<TicketMove> generateTicketMoves(ScotlandYardPlayer currentPlayer) {
        Set<TicketMove> ticketMoves = new HashSet<>();
        Collection<Edge<Integer, Transport>> edges = this.graph.getEdgesFrom(this.graph.getNode(currentPlayer.location()));
        edges.forEach(edgeling -> {
            if (isNodeUnoccupied(edgeling.destination().value())) {
                if (edgeling.data() != Transport.FERRY) {
                    if (currentPlayer.hasTickets(Ticket.fromTransport(edgeling.data()))) {
                        ticketMoves.add(new TicketMove(currentPlayer.colour(), Ticket.fromTransport(edgeling.data()), edgeling.destination().value()));
                    }
                }
                if (currentPlayer.hasTickets(Ticket.SECRET) && currentPlayer.colour() == Colour.BLACK) {
                    ticketMoves.add(new TicketMove(currentPlayer.colour(), Ticket.SECRET, edgeling.destination().value()));
                }
            }
        });
        return ticketMoves;
    }

    private Set<DoubleMove> generateDoubleMoves(ScotlandYardPlayer currentPlayer, TicketMove fmove) {
        ScotlandYardPlayer jeff = new ScotlandYardPlayer(currentPlayer.player(), currentPlayer.colour(),
                currentPlayer.location(), currentPlayer.tickets());
        jeff.removeTicket(fmove.ticket());
        jeff.location(fmove.destination());

        return generateTicketMoves(jeff).stream()
                .map(move -> new DoubleMove(currentPlayer.colour(), fmove, move))
                .collect(Collectors.toSet());
    }


    private boolean isNodeUnoccupied(int local) {
        for (ScotlandYardPlayer persons : players) {
            if (persons.location() == local && persons.colour() != Colour.BLACK) {
                return false;
            }
        }
        return true;
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
        return Collections.unmodifiableSet(winners);
    }

    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        if (colour == Colour.BLACK) {
            if (isMrXPositionKnownToPlayers()) {
                mrXLastSeenLocation = getMrX().location();
            }
            return Optional.of(mrXLastSeenLocation);
        }

        return getPlayer(colour).map(player -> player.location());
    }

    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        return getPlayer(colour).map(player -> player.tickets().get(ticket));
    }

    @Override
    public boolean isGameOver() {
        return !getWinningPlayers().isEmpty();
    }

    @Override
    public Colour getCurrentPlayer() {
        return this.getPlayers().get(currentPlayerIndex);
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

    private boolean isMrXPositionKnownToPlayers() {
        if (currentRound >= getRounds().size()) {
            return true;
        } else {
            return getRounds().get(currentRound);
        }
    }

    private Optional<ScotlandYardPlayer> getPlayer(Colour colour) {
        return players.stream()
                .filter(player -> player.colour() == colour)
                .findFirst();
    }

    private ScotlandYardPlayer getMrX() {
        return getPlayer(Colour.BLACK).orElseThrow(() -> new RuntimeException("BUG: MrX no longer a player!"));
    }

}
