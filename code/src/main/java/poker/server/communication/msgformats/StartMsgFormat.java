package poker.server.communication.msgformats;

import org.json.JSONObject;
import poker.server.data.cards.Card;

import java.util.HashMap;

public class StartMsgFormat {

    public static String getMsg(Card[] cards){
        HashMap<String, Object> msgMap = new HashMap<>();

        msgMap.put("name", "start");

        HashMap<String, Object> card1;
        HashMap<String, Object> card2;

        if(cards[0] != null && cards[1] != null) {
            card1 = new HashMap<>();
            card2 = new HashMap<>();
            card1.put("number", cards[0].number);
            card1.put("color", cards[0].color);
            card2.put("number", cards[1].number);
            card2.put("color", cards[1].color);
        } else {
            card1 = null;
            card2 = null;
        }

        msgMap.put("cards", new HashMap[]{card1, card2});

        return new JSONObject(msgMap).toString();
    }

}
