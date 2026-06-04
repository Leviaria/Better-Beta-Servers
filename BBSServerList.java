package net.minecraft.src;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class BBSServerList {

    private static final int MAX_HISTORY = 10;

    private final File serversFile;
    private final File historyFile;

    private List servers = new ArrayList();
    private List history = new ArrayList();

    public BBSServerList(File mcDir) {
        serversFile = new File(mcDir, "bbs_servers.txt");
        historyFile = new File(mcDir, "bbs_history.txt");
    }

    public void load() {
        servers.clear();
        history.clear();
        loadFile(serversFile, servers);
        loadFile(historyFile, history);
    }

    private void loadFile(File file, List list) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 3) {
                    continue;
                }
                try {
                    String name = parts[0];
                    String host = parts[1];
                    int port = Integer.parseInt(parts[2].trim());
                    boolean fav = parts.length >= 4 && parts[3].trim().equals("1");
                    list.add(new BBSServerEntry(name, host, port, fav));
                } catch (Exception e) {
                    // skip malformed line
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("[BBS] Failed to load: " + file.getName());
        }
    }

    public void save() {
        saveFile(serversFile, servers);
        saveFile(historyFile, history);
    }

    private void saveFile(File file, List list) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            for (int i = 0; i < list.size(); i++) {
                BBSServerEntry e = (BBSServerEntry) list.get(i);
                writer.println(e.name + "|" + e.host + "|" + e.port + "|" + (e.favorite ? "1" : "0"));
            }
            writer.close();
        } catch (Exception ex) {
            System.out.println("[BBS] Failed to save: " + file.getName());
        }
    }

    public List getServers() {
        return servers;
    }

    public List getHistory() {
        return history;
    }

    public void addServer(BBSServerEntry entry) {
        servers.add(entry);
        save();
    }

    public void removeServer(int index) {
        if (index >= 0 && index < servers.size()) {
            servers.remove(index);
            save();
        }
    }

    public void updateServer(int index, BBSServerEntry entry) {
        if (index >= 0 && index < servers.size()) {
            servers.set(index, entry);
            save();
        }
    }

    public void toggleFavorite(int index) {
        if (index >= 0 && index < servers.size()) {
            BBSServerEntry e = (BBSServerEntry) servers.get(index);
            e.favorite = !e.favorite;
            save();
        }
    }

    public void moveUp(int index) {
        if (index > 0 && index < servers.size()) {
            Object tmp = servers.get(index - 1);
            servers.set(index - 1, servers.get(index));
            servers.set(index, tmp);
            save();
        }
    }

    public void moveDown(int index) {
        if (index >= 0 && index < servers.size() - 1) {
            Object tmp = servers.get(index + 1);
            servers.set(index + 1, servers.get(index));
            servers.set(index, tmp);
            save();
        }
    }

    public void pushHistory(String host, int port) {
        String entryHost = host;
        String displayName = host + (port != 25565 ? ":" + port : "");
        for (int i = history.size() - 1; i >= 0; i--) {
            BBSServerEntry e = (BBSServerEntry) history.get(i);
            if (e.host.equalsIgnoreCase(entryHost) && e.port == port) {
                history.remove(i);
            }
        }
        history.add(0, new BBSServerEntry(displayName, entryHost, port, false));
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
        save();
    }
}
