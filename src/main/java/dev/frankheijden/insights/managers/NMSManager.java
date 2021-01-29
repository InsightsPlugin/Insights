package dev.frankheijden.insights.managers;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

public class NMSManager {

    private static NMSManager instance;

    public static String NMS;
    private final boolean post1_8_R2;

    public NMSManager() {
        instance = this;

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        NMS = packageName.substring(packageName.lastIndexOf(".") + 1);

        post1_8_R2 = PaperLib.getMinecraftVersion() >= 8 && !NMS.equalsIgnoreCase("v1_8_R1");
    }

    public static NMSManager getInstance() {
        return instance;
    }

    public boolean isPost1_8_R2() {
        return post1_8_R2;
    }

    public boolean isPost(int minor) {
        return PaperLib.getMinecraftVersion() >= minor;
    }
}
