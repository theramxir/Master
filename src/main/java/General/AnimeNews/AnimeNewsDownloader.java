package General.AnimeNews;

import Constants.Language;
import General.PostBundle;
import General.Shortcuts;
import General.Tools;
import General.Internet.URLDataContainer;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

public class AnimeNewsDownloader {
    public static AnimeNewsPost getPost(Locale locale) throws IOException, InterruptedException {
        String downloadUrl;
        if (Tools.getLanguage(locale) == Language.DE) downloadUrl = "https://www.animenachrichten.de/";
        else downloadUrl = "https://www.animenewsnetwork.com/news/";
        String dataString = URLDataContainer.getInstance().getData(downloadUrl, Instant.now().plusSeconds(60 * 14));

        if (dataString == null) return null;

        if (Tools.getLanguage(locale) == Language.DE) return getPostDE(getCurrentPostStringDE(dataString));
        else return getPostEN(getCurrentPostStringEN(dataString));
    }

    public static PostBundle<AnimeNewsPost> getPostTracker(Locale locale, String newestPostId) throws IOException, InterruptedException {
        String downloadUrl;
        if (Tools.getLanguage(locale) == Language.DE) downloadUrl = "https://www.animenachrichten.de/";
        else downloadUrl = "https://www.animenewsnetwork.com/news/";
        String dataString = URLDataContainer.getInstance().getData(downloadUrl, Instant.now().plusSeconds(60 * 14));

        if (dataString == null) return null;

        ArrayList<AnimeNewsPost> postList = new ArrayList<>();
        for(int i=0; i < 5; i++) {
            String postString;
            if (Tools.getLanguage(locale) == Language.DE) postString = getCurrentPostStringDE(dataString);
            else postString = getCurrentPostStringEN(dataString);

            AnimeNewsPost post;
            if (Tools.getLanguage(locale) == Language.DE) post = getPostDE(postString);
            else post = getPostEN(postString);
            if (post.getId().equals(newestPostId)) break;

            postList.add(post);
            if (Tools.getLanguage(locale) == Language.DE) dataString = dataString.replaceFirst("class=\"td-block-span12\">", "");
            else dataString = dataString.replaceFirst("<div class=\"herald box news\"", "");
        }

        ArrayList<AnimeNewsPost> postSendList = new ArrayList<>();
        if (newestPostId != null) {
            for(int i=postList.size()-1; i >= 0; i--) {
                postSendList.add(postList.get(i));
            }
        }

        if (postList.size() > 0) newestPostId = postList.get(0).getId();
        return new PostBundle<>(postSendList, newestPostId);
    }

    private static AnimeNewsPost getPostDE(String data) {
        AnimeNewsPost post = new AnimeNewsPost();

        post.setTitle(Shortcuts.decryptString(Tools.cutString(data, "title=\"", "\"")));
        post.setDescription(Shortcuts.decryptString(Tools.cutString(data + "</div>", "<div class=\"td-excerpt\">", "</div>")));
        post.setImage(Tools.cutString(data, "data-lazy-srcset=\"", " "));
        post.setLink(Tools.cutString(data, "<a href=\"", "\""));

        if (data.contains("#comments\">")) post.setComments(Integer.parseInt(Tools.cutString(data, "#comments\">", "<")));
        else post.setComments(Integer.parseInt(Tools.cutString(data, "#respond\">", "<")));

        post.setAuthor(Shortcuts.decryptString(Tools.cutString(data, "class=\"td-post-author-name\">", "</a>").split(">")[1]));
        post.setDate(Shortcuts.decryptString(Tools.cutString(data, "datetime=\"", "</time>").split(">")[1]));
        post.setId(Tools.cutString(data, "datetime=\"", "\""));
        post.setCategory("");

        return post;
    }

    private static AnimeNewsPost getPostEN(String data) {
        AnimeNewsPost post = new AnimeNewsPost();

        data = data.replace("<cite>", "").replace("</cite>", "").replaceFirst("&amp;from=I.MF\">", "").replaceFirst("<a href=\"", "");

        post.setTitle(Shortcuts.decryptString(Tools.cutString(data, "&amp;from=I.MF\">", "</a>")));
        post.setDescription(Shortcuts.decryptString(Tools.cutString(data, "<span class=\"full\">― ", "</span>")));
        post.setImage("https://www.animenewsnetwork.com" + Tools.cutString(data, "data-src=\"", "\">"));
        post.setLink("https://www.animenewsnetwork.com" + Tools.cutString(data, "<a href=\"", "\""));
        post.setComments(Integer.parseInt(Tools.cutString(Tools.cutString(data, "<div class=\"comments\"><a href=\"", "</a></div>"), ">", " ")));
        post.setAuthor("");
        post.setDate(Shortcuts.decryptString(Tools.cutString(Tools.cutString(data, "<time datetime=\"", "/time>"), ">", "<")));
        post.setId(Tools.cutString(data, "data-track=\"id=", "</a>"));
        post.setCategory(Shortcuts.decryptString(Tools.cutString(data, "<span class=\"topics\">", "</div>")));

        return post;
    }

    private static String getCurrentPostStringDE(String str) {
        if (!str.contains("class=\"td-block-span12\">")) return null;
        return Tools.cutString(str, "class=\"td-block-span12\">", "</div></div></div>");
    }

    private static String getCurrentPostStringEN(String str) {
        if (!str.contains("<div class=\"herald box news\"")) return null;
        return Tools.cutString(str, "<div class=\"herald box news\"", "<div class=\"herald box news\"");
    }
}