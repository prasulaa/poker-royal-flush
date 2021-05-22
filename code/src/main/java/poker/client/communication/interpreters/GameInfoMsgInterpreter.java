package poker.client.communication.interpreters;

import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import poker.client.Game;
import poker.client.data.GameTable;
import poker.client.data.Player;
import poker.client.data.cards.Card;
import poker.client.data.cards.Deck;
import poker.properties.PlayerStateProperties;

public class GameInfoMsgInterpreter implements MsgInterpreter {

	@Override
	public void interpret(JSONObject msg, GameTable gameTable){
		JSONObject table = msg.getJSONObject("table");

		gameTable.setPotValue(table.getInt("pot"));
		setPlayers(table.getJSONArray("players"), gameTable);
		setCards(table.getJSONArray("cards"), gameTable);
	}

	private void setPlayers(JSONArray players, GameTable gameTable){
		int n = players.length();
		for(int i = 0; i < n; i++){
			try {
				JSONObject playerJSON = players.getJSONObject(i);
				Player player = new Player(playerJSON.getString("nickname"));

				player.setState(playerJSON.getInt("state"));
				player.setMoney(playerJSON.getInt("money"));
				player.setPotValue(playerJSON.getInt("pot"));

				gameTable.setPlayer(player, i);
				checkIfMainPlayerFolded(gameTable, player);
			} catch (JSONException e){
				gameTable.setPlayer(null, i);
			}
		}
	}

	private void setCards(JSONArray cards, GameTable gameTable){
		int n = cards.length();
		Deck deck = Deck.getInstance();
		for (int i = 0; i < n; i++){
			try{
				JSONObject cardJSON = cards.getJSONObject(i);
				Card card = deck.getCard(cardJSON.getInt("color"), cardJSON.getInt("number"));
				gameTable.setTableCard(card, i);
			} catch (JSONException e){
				gameTable.setTableCard(null, i);
			}
		}
	}

	private void checkIfMainPlayerFolded(GameTable gameTable, Player player){
		Player mainPlayer = gameTable.getMainPlayer();

		if(mainPlayer != null && mainPlayer.equals(player) && player.getState() == PlayerStateProperties.AFTERFOLD){
			Platform.runLater(() -> Game.getGameMenuController().disableButtons());
		}
	}
}