package modules.repair;

import net.dv8tion.jda.api.JDA;

public class MainRepair {

    public static void start(JDA jda, int minutes) {
        if (jda != null) {
            AutoChannelRepair.getInstance().start(jda);
            RolesRepair.getInstance().start(jda, minutes);
        }
    }

}
