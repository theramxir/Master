package commands

enum class Category(val id: String, val emoji: String, val isIndependent: Boolean) {

    GIMMICKS("gimmicks", "đĒ", true),
    AI_TOYS("aitoys", "đ¤", true),
    CONFIGURATION("configuration", "âī¸", true),
    UTILITY("utility", "đ¨", true),
    MODERATION("moderation", "đŽ", true),
    INFORMATION("information", "âšī¸", true),
    FISHERY_SETTINGS("fishery_settings_category", "âī¸", true),
    FISHERY("fishery_category", "đŖ", true),
    CASINO("casino", "đ°", true),
    INTERACTIONS("interactions", "đĢ", true),
    EXTERNAL("external_services", "đ¤", true),
    NSFW("nsfw", "đ", true),
    SPLATOON_2("splatoon_2", "đĻ", true),
    PATREON_ONLY("patreon_only", "â­", false);

    companion object {

        @JvmStatic
        fun independentValues(): Array<Category> {
            return values()
                .filter { obj: Category -> obj.isIndependent }
                .toTypedArray()
        }

        @JvmStatic
        fun findCategoryByCommand(c: Class<out Command>): Category? {
            val categoryName = c.getPackage().name.split(".")[2]
            return when(categoryName) {
                "gimmickscategory" -> GIMMICKS
                "nsfwcategory" -> NSFW
                "configurationcategory" -> CONFIGURATION
                "utilitycategory" -> UTILITY
                "informationcategory" -> INFORMATION
                "splatoon2category" -> SPLATOON_2
                "interactionscategory" -> INTERACTIONS
                "externalcategory" -> EXTERNAL
                "fisherysettingscategory" -> FISHERY_SETTINGS
                "fisherycategory" -> FISHERY
                "casinocategory" -> CASINO
                "moderationcategory" -> MODERATION
                "aitoyscategory" -> AI_TOYS
                else -> null
            }
        }

    }

}