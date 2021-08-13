package commands.runnables;

import java.util.*;
import constants.Category;
import core.TextManager;
import modules.porn.BooruImage;
import modules.porn.IllegalBooruTagException;
import modules.porn.TooManyTagsException;

public abstract class PornSearchAbstract extends PornAbstract {

    private String notice = null;

    public PornSearchAbstract(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public Optional<String> getNoticeOptional() {
        return Optional.ofNullable(notice);
    }

    public int getMaxTags() {
        return -1;
    }

    @Override
    public List<BooruImage> getBooruImages(long guildId, Set<String> nsfwFilters, String search, int amount, ArrayList<String> usedResults) throws IllegalBooruTagException, TooManyTagsException {
        if (search.isEmpty()) {
            search = "animated";
            notice = TextManager.getString(getLocale(), Category.NSFW, "porn_nokey");
        }

        switch (search.toLowerCase()) {
            case "hinata" -> search = "hyuuga_hinata";
            case "konosuba" -> search = "kono_subarashii_sekai_ni_shukufuku_wo!";
        }

        int maxTags = getMaxTags();
        int tags = countTags(search);
        if (maxTags != -1 && tags > maxTags) {
            throw new TooManyTagsException(maxTags);
        }

        nsfwFilters = new HashSet<>(nsfwFilters);
        nsfwFilters.addAll(getAdditionalFilters());

        return downloadPorn(guildId, nsfwFilters, amount, getDomain(), search, false, isExplicit(),
                usedResults);
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

    private int countTags(String search) {
        return search.replace("+", " ").split(" ").length;
    }

}
