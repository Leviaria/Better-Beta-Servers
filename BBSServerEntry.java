package net.minecraft.src;

public class BBSServerEntry {

    public String name;
    public String host;
    public int port;
    public boolean favorite;

    public int pingMs = -1;
    public int playersOnline = -1;
    public int playersMax = -1;
    public String motd = "";
    public int pingState = 0;

    public static final int PING_UNKNOWN = 0;
    public static final int PING_PENDING = 1;
    public static final int PING_OK = 2;
    public static final int PING_OFFLINE = 3;

    public BBSServerEntry(String name, String host, int port, boolean favorite) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.favorite = favorite;
    }

    public String getAddress() {
        if (port == 25565) {
            return host;
        }
        return host + ":" + port;
    }
}
