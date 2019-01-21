package cc.funkemunky.anticheat.api.mongo;


import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;

@Getter
@Setter
@Init
public class Mongo {

    private MongoDatabase mongoDatabase;
    private MongoClient client;

    private String database = "kauri";

    private String ip = "127.0.0.1";

    private int port = 27017;

    private boolean enabled = false;

    private String username = "username";

    private String password = "password";

    @Getter
    private boolean connected = false;

    public void connect() {
        try {
            this.client = new MongoClient(ip, port);
            if(enabled) {
                val credential = MongoCredential.createCredential(username, database, password.toCharArray());
                client.getCredentialsList().add(credential);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getServer().getLogger().severe("Could not connect to the database!");
            this.connected = false;
            return;
        }
        Bukkit.getServer().getConsoleSender().sendMessage(Color.translate("&aConnected to the Mongo database."));
        this.mongoDatabase = client.getDatabase(database);
        this.connected = true;
    }

    public void disconnect() {
        client.close();
    }

}
