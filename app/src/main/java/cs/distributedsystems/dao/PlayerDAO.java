package cs.distributedsystems.dao;


import gr.softeng.distributedsystems.Entities.Player;

public interface PlayerDAO {
    void save(Player player);

    Player findByUsername(String username);

}
