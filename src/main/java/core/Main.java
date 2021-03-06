package core;

import java.time.Instant;
import com.jockie.jda.memory.MemoryOptimizations;
import core.emoji.EmojiTable;
import core.utils.BotUtil;
import mysql.MySQLManager;
import mysql.modules.version.DBVersion;
import mysql.modules.version.VersionData;
import mysql.modules.version.VersionSlot;
import org.apache.commons.lang3.SystemUtils;
import websockets.syncserver.SyncManager;

public class Main {

    public static void main(String[] args) {
        installMemoryOptimizations();
        try {
            Program.init();
            createTempDir();

            Console.start();
            FontManager.init();
            MySQLManager.connect();
            EmojiTable.load();
            if (Program.publicVersion()) {
                initializeUpdate();
            }

            if (Program.productionMode() || SystemUtils.IS_OS_WINDOWS) {
                MainLogger.get().info("Waiting for sync server");
                SyncManager.connect();
            }

            if (!Program.productionMode()) {
                DiscordConnector.connect(0, 0, 1);
            } else {
                Runtime.getRuntime().addShutdownHook(new Thread(Program::onStop, "Shutdown Bot-Stop"));
            }
        } catch (Throwable e) {
            MainLogger.get().error("EXIT - Error on startup", e);
            System.exit(4);
        }
    }

    private static void installMemoryOptimizations() {
        try {
            MemoryOptimizations.installOptimizations();
        } catch (Throwable e) {
            MainLogger.get().error("Unable to install byte-buddy", e);
        }
    }

    private static void createTempDir() {
        LocalFile tempDir = new LocalFile("temp");
        if (!tempDir.exists() && !tempDir.mkdir()) {
            throw new RuntimeException("Could not create temp dir");
        }
    }

    private static void initializeUpdate() {
        VersionData versionData = DBVersion.getInstance().retrieve();
        String currentVersionDB = versionData.getCurrentVersion().getVersion();
        if (!BotUtil.getCurrentVersion().equals(currentVersionDB)) {
            Program.setNewVersion();
            versionData.getSlots().add(new VersionSlot(BotUtil.getCurrentVersion(), Instant.now()));
        }
    }

}
