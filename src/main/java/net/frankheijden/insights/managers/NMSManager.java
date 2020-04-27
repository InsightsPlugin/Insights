package net.frankheijden.insights.managers;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

public class NMSManager {

    private static NMSManager instance;

    public static String NMS;
    private final boolean post1_8_R2;
    private final boolean post1_9;
    private final boolean post1_12;
    private final boolean post1_13;

    public NMSManager() {
        instance = this;

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        NMS = packageName.substring(packageName.lastIndexOf(".") + 1);

        post1_8_R2 = PaperLib.getMinecraftVersion() >= 8 && !NMS.equalsIgnoreCase("v1_8_R1");
        post1_9 = PaperLib.getMinecraftVersion() >= 9;
        post1_12 = PaperLib.getMinecraftVersion() >= 12;
        post1_13 = PaperLib.getMinecraftVersion() >= 13;
    }

    public static NMSManager getInstance() {
        return instance;
    }

    public boolean isPost1_8_R2() {
        return post1_8_R2;
    }

    public boolean isPost1_9() {
        return post1_9;
    }

    public boolean isPost1_12() {
        return post1_12;
    }

    public boolean isPost1_13() {
        return post1_13;
    }
}
