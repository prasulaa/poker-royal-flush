package poker.server.data;

import poker.properties.PlayerStateProperties;
import poker.server.data.cards.Card;
import poker.server.data.cards.Deck;

import java.util.Iterator;

public class GameTable {

    private static GameTable instance;

    public static GameTable getInstance() {
        if (instance == null) {
            instance = new GameTable();
        }
        return instance;
    }

    private final Card[] tableCards;
    private final Player[] players;
    private int potValue;
    private int starterPlayer;

    private GameTable() {
        tableCards = new Card[5];
        players = new Player[6];
        potValue = 0;
        starterPlayer = 0;
    }

    public int getStarterPlayerIndex() {
        return starterPlayer;
    }

    public void moveStarterPlayerIndex() {
        for (int i = (starterPlayer + 1) % players.length; i < players.length; i = (i + 1) % players.length) {
            if (players[i] != null) {
                starterPlayer = i;
                return;
            }
        }
    }

    public int getPotValue() {
        return potValue;
    }

    public void setPotValue(int potValue) {
        this.potValue = potValue;
    }

    public Card[] getTableCards() {
        return tableCards;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int addPlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                return i;
            }
        }
        return -1;
    }

    public void deletePlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (player.equals(players[i])) {
                players[i] = null;
                return;
            }
        }
    }

    public void addTableCard(Card card) {
        for (int i = 0; i < tableCards.length; i++) {
            if (tableCards[i] == null) {
                tableCards[i] = card;
                return;
            }
        }
    }

    public void resetTableCards() {
        for (int i = 0; i < tableCards.length; i++) {
            tableCards[i] = null;
        }
    }

    public int numberOfPlayers() {
        int counter = 0;
        for (Player player : players) {
            if (player != null) {
                counter++;
            }
        }
        return counter;
    }

    public int numberOfActivePlayers() {
        int amount = 0;

        for (Player player : players) {
            if (player != null && player.getState() != PlayerStateProperties.AFTERFOLD) {
                amount++;
            }
        }

        return amount;
    }

    public boolean hasFreeSeat() {
        return numberOfPlayers() < players.length;
    }

    public Iterator<Player> playersIterator() {
        return new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                for (int j = i; j < players.length; j++) {
                    if (players[j] != null) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Player next() {
                while (i < players.length) {
                    i++;
                    if (players[i - 1] != null) {
                        return players[i - 1];
                    }
                }
                return null;
            }
        };
    }
}