package com.mcylm.coi.realm.utils;

import com.mcylm.coi.realm.Entry;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ServerUtils {

    public static void teleport(Player p, String server) {
        if (p != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            try {
                out.writeUTF("Connect");
                out.writeUTF(server);
            } catch (Exception exception) {}
            p.sendPluginMessage(Entry.getInstance(), "BungeeCord", baos.toByteArray());
        }
    }

}
