package cs.distributedsystems.memorydao;

import java.util.ArrayList;
import java.util.List;


import cs.distributedsystems.dao.PlayerDAO;
import gr.softeng.distributedsystems.Entities.Player;


public class PlayerDAOMemory implements PlayerDAO {

    protected static List<Player> players = new ArrayList<>();

    //Αρχικοποίηση με δεδομένα
    static {
        players.add(new Player("papas", "1234"));
    }
    @Override
    public void save(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

   @Override
   public Player findByUsername(String username) {
       for (Player player : players) {
           if (player.getUsername().equals(username)) {
               return player;
           }
       }
       return null;
   }
}
