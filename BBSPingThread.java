package net.minecraft.src;

import java.io.*;
import java.net.*;

public class BBSPingThread extends Thread {

    private static final int TIMEOUT_MS = 5000;

    private final BBSServerEntry entry;

    public BBSPingThread(BBSServerEntry entry) {
        this.entry = entry;
        setDaemon(true);
        entry.pingState = BBSServerEntry.PING_PENDING;
    }

    public void run() {
        Socket socket = null;
        try {
            long before = System.currentTimeMillis();
            socket = new Socket();
            socket.setSoTimeout(TIMEOUT_MS);
            socket.connect(new InetSocketAddress(entry.host, entry.port), TIMEOUT_MS);
            long after = System.currentTimeMillis();
            entry.pingMs = (int)(after - before);

            tryStatusPing(socket);

            entry.pingState = BBSServerEntry.PING_OK;
        } catch (Exception e) {
            entry.pingState = BBSServerEntry.PING_OFFLINE;
            entry.pingMs = -1;
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception ex) {}
            }
        }
    }

    private void tryStatusPing(Socket socket) {
        try {
            socket.setSoTimeout(2000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in   = new DataInputStream(socket.getInputStream());

            out.write(0xFE);
            out.flush();

            int packetId = in.read();
            if (packetId != 0xFF) {
                return;
            }

            short len = in.readShort();
            if (len <= 0 || len > 512) {
                return;
            }

            byte[] raw = new byte[len * 2];
            in.readFully(raw);
            String data = new String(raw, "UTF-16BE");

            String[] parts = data.split("\u00a7");
            if (parts.length >= 1) {
                entry.motd = stripColorCodes(parts[0]);
            }
            if (parts.length >= 2) {
                try { entry.playersOnline = Integer.parseInt(parts[1].trim()); } catch (Exception ignored) {}
            }
            if (parts.length >= 3) {
                try { entry.playersMax = Integer.parseInt(parts[2].trim()); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }
    }

    private String stripColorCodes(String s) {
        if (s == null) return "";
        StringBuffer sb = new StringBuffer();
        boolean skip = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u00a7') {
                skip = true;
                continue;
            }
            if (skip) {
                skip = false;
                continue;
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }
}
