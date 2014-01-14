package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.XMLUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Walter Xie
 */
public class NCBIEUtils {

    //++++++++++++ eSearch +++++++++++++

    public static String E_SEARCH = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=\"";

    public static URL eSearch(String scientificName) throws MalformedURLException {
        return new URL(E_SEARCH + scientificName + "\"");
    }

    public static boolean isCount(String name) {
        return XMLUtil.isTag("Count", name);
    }

    public static boolean isIdList(String name) {
        return XMLUtil.isTag("IdList", name);
    }

    public static boolean isId(String name) {
        return XMLUtil.isTag("Id", name);
    }

    //++++++++++++ eFetch +++++++++++++

    public static String E_FETCH = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=";

    public static URL eFetch(String taxonId) throws MalformedURLException {
        return new URL(E_FETCH + taxonId);
    }

    public static boolean isTaxId(String name) {
        return XMLUtil.isTag("TaxId", name);
    }

    public static boolean isRank(String name) {
        return XMLUtil.isTag("Rank", name);
    }

    public static boolean isLineageEx(String name) {
        return XMLUtil.isTag("LineageEx", name);
    }

    public static boolean isTaxon(String name) {
        return XMLUtil.isTag("Taxon", name);
    }

}
