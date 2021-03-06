package poker.server.gamecontrollers;

import poker.properties.PlayerStateProperties;
import poker.server.Game;
import poker.server.communication.ClientConnector;
import poker.server.communication.msgformats.EndMsgFormat;
import poker.server.communication.msgformats.GameInfoMsgFormat;
import poker.server.communication.msgformats.StartMsgFormat;
import poker.server.data.GameTable;
import poker.server.data.Player;
import poker.server.data.cards.Deck;
import poker.server.gamecontrollers.hands.PokerHandCalculator;

import java.util.*;

public class RoundController {

    private final GameTable gameTable;
    private final Deck deck;
    private final ClientConnector clientConnector;

    private static final int CYCLEDELAY = 1500;

    public RoundController(GameTable gameTable, ClientConnector clientConnector) {
        this.gameTable = gameTable;
        this.clientConnector = clientConnector;
        deck = new Deck();
    }

    public void playRound() {
        if (gameTable.numberOfPlayers() <= 1) {
            throw new IllegalStateException("Not enough players for the game");
        }

        CycleController cycle = new CycleController(gameTable, clientConnector);
        updateGameTableAndPlayers();

        playRound(cycle);
    }

    private void playRound(CycleController cycle) {
        try {
            startRound(cycle);
            playAllCyclesInRound(cycle);
            endRoundByHandCardsWinner();
        } catch (IllegalStateException e) {
            timeDelay(CYCLEDELAY);
            endRoundByFoldWinner();
        } finally {
            updateGameTableAndPlayers();
        }
    }

    private void playAllCyclesInRound(CycleController cycle) {
        playCycle(true, cycle);

        for (int i = 0; i < 3; i++) {
            drawTableCards(i == 0 ? 3 : 1);
            cycle.setMinPot(0);
            playCycle(false, cycle);
        }
    }

    private void playCycle(boolean firstCycle, CycleController cycle) {
        cycle.playCycle(firstCycle);
        timeDelay(CYCLEDELAY);
    }

    private void startRound(CycleController cycle) {
        Iterator<Player> players = gameTable.playersIterator();

        while (players.hasNext()) {
            Player player = players.next();
            player.setHandCards(deck.getRandomCard(), deck.getRandomCard());
            clientConnector.sendMsg(StartMsgFormat.getMsg(player.getHandCards()), player);
        }

        gameTable.setPotValue(0);
        cycle.setMinPot(Game.getBlind());
    }

    private void drawTableCards(int amount) {
        for (int i = 0; i < amount; i++) {
            gameTable.addTableCard(deck.getRandomCard());
        }
    }

    private void endRoundByHandCardsWinner() {
        Set<Player> winners = getWinnersByHandStrength(getPlayersSortedByHandStrength());
        clientConnector.sendMsgToAll(EndMsgFormat.getMsg(gameTable, winners));
        givePrizeToWinners(winners);
    }

    private void endRoundByFoldWinner() {
        Set<Player> winners = getWinnersByNotFolded();
        clientConnector.sendMsgToAll(EndMsgFormat.getMsg(gameTable, winners));
        givePrizeToWinners(winners);
    }

    private Set<Player> getWinnersByNotFolded() {
        Set<Player> winners = new HashSet<>();

        for (Player player : gameTable.getPlayers()) {
            if (player != null && player.getState() != PlayerStateProperties.AFTERFOLD) {
                winners.add(player);
            }
        }

        return winners;
    }

    private Set<Player> getWinnersByHandStrength(Player[] players) {
        Set<Player> winnersSet = new HashSet<>();

        for (int i = 1; i < players.length || players[i - 1] != null; i++) {
            winnersSet.add(players[i - 1]);

            if (players[i] == null || players[i - 1].getHandStrength() > players[i].getHandStrength()) {
                break;
            }
        }

        return winnersSet;
    }

    private Player[] getPlayersSortedByHandStrength() {
        PokerHandCalculator calculator = new PokerHandCalculator(gameTable.getTableCards());
        Player[] players = gameTable.getPlayers().clone();

        for (Player player : players) {
            if (player != null && player.getState() != PlayerStateProperties.AFTERFOLD) {
                player.setHandStrength(calculator.handStrength(player.getHandCards()));
            }
        }

        Arrays.sort(players, (o1, o2) -> {
            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
                return Double.compare(o2.getHandStrength(), o1.getHandStrength());
            }
        });

        return players;
    }

    private void givePrizeToWinners(Set<Player> winners) {
        int n = winners.size();
        if (n == 0) {
            return;
        }

        int prize = gameTable.getPotValue() / n;

        for (Player player : gameTable.getPlayers()) {
            if (player != null) {
                if (winners.contains(player)) {
                    player.setMoney(player.getMoney() + prize);
                }
            }
        }
    }

    private void updateGameTableAndPlayers() {
        for (Player player : gameTable.getPlayers()) {
            if (player != null) {
                player.setPotValue(0);
                player.setState(PlayerStateProperties.INGAME);
            }
        }

        gameTable.resetTableCards();
        gameTable.setPotValue(0);
    }

    private void timeDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            return;
        }
    }
}