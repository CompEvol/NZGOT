package nzgo.toolkit.core.taxonomy;

/**
 * Taxonomic Rank
 * @author Walter Xie
 */
public enum Rank {
    // compares this enum with the specified object for order.Enum constants
    // SPECIES.compareTo(GENUS) < 0

    SUB_SPECIES("subspecies"),
    SPECIES("species"),
    SUPER_SPECIES("superspecies"),
    SUB_GENUS("subgenus"),
    GENUS("genus"),
    INFRA_FAMILY("infrafamily"),
    SUB_FAMILY("subfamily"),
    FAMILY("family"),
    SUPER_FAMILY("superfamily"),
    INFRA_ORDER("infraorder"),
    SUB_ORDER("suborder"),
    ORDER("order"),
    SUPER_ORDER("superorder"),
    INFRA_CLASS("infraclass"),
    SUB_CLASS("subclass"),
    CLASS("class"),
    SUPER_CLASS("superclass"),
    INFRA_PHYLUM("infraphylum"),
    SUB_PHYLUM("subphylum"),
    PHYLUM("phylum"),
    SUPER_PHYLUM("superphylum"),
    KINGDOM("kingdom"),
    NO_RANK("no rank");

    private String rank;

    private Rank(String rank) {
        this.rank = rank;
    }

    public static boolean contains(String rank) {
        for (Rank r : Rank.class.getEnumConstants()) {
            if (r.rank.equalsIgnoreCase(rank))
                return true;
        }
        return false;
    }

    public static Rank fromString(String rank) {
        if (rank != null) {
            for (Rank r : Rank.class.getEnumConstants()) {
                if (r.rank.equalsIgnoreCase(rank))
                    return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return rank;
    }

    public static String[] mainRanksToString() {
        return new String[]{SPECIES.toString(), GENUS.toString(), FAMILY.toString(),
                ORDER.toString(), CLASS.toString(), PHYLUM.toString()};
    }
}
