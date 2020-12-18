package websockets.webcomserver.events;

import constants.FisheryStatus;
import core.DiscordApiManager;
import core.cache.PatreonCache;
import modules.Fishery;
import mysql.modules.autoclaim.DBAutoClaim;
import mysql.modules.bannedusers.DBBannedUsers;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryUserBean;
import mysql.modules.server.DBServer;
import mysql.modules.upvotes.DBUpvotes;
import mysql.modules.upvotes.UpvotesBean;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import websockets.webcomserver.EventAbstract;
import websockets.webcomserver.WebComServer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OnTopGG extends EventAbstract {

    private final static Logger LOGGER = LoggerFactory.getLogger(OnTopGG.class);

    public OnTopGG(WebComServer webComServer, String event) {
        super(webComServer, event);
    }

    @Override
    protected JSONObject processData(JSONObject requestJSON, WebComServer webComServer) throws Exception {
        long userId = requestJSON.getLong("user");
        if (DBBannedUsers.getInstance().getBean().getUserIds().contains(userId))
            return null;

        String type = requestJSON.getString("type");
        boolean isWeekend = requestJSON.getBoolean("isWeekend");

        if (type.equals("upvote")) {
            UpvotesBean upvotesBean = DBUpvotes.getInstance().getBean(userId);
            if (upvotesBean.getLastUpvote().plus(11, ChronoUnit.HOURS).isBefore(Instant.now())) {
                DiscordApiManager.getInstance().fetchUserById(userId).get().ifPresent(user -> {
                    LOGGER.info("UPVOTE | {}", user.getName());

                    DiscordApiManager.getInstance().getLocalMutualServers(user).stream()
                            .filter(server -> DBServer.getInstance().getBean(server.getId()).getFisheryStatus() == FisheryStatus.ACTIVE)
                            .forEach(server -> {
                                int value = isWeekend ? 2 : 1;
                                FisheryUserBean userBean = DBFishery.getInstance().getBean(server.getId()).getUserBean(userId);

                                if (PatreonCache.getInstance().getPatreonLevel(userId) >= 2 &&
                                        DBAutoClaim.getInstance().getBean(userId).isActive()
                                ) {
                                    userBean.changeValues(Fishery.getClaimValue(userBean) * value, 0);
                                } else {
                                    userBean.addUpvote(value);
                                }
                            });
                });
                upvotesBean.updateLastUpvote();
            }

            return new JSONObject();
        } else {
            LOGGER.error("Wrong type: " + type);
            return new JSONObject();
        }
    }

}