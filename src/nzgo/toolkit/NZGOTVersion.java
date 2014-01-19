package nzgo.toolkit;

import beast.app.util.Version;

/**
 * implement Version in beast package
 *
 * @author Walter Xie
 */
public class NZGOTVersion extends Version {

    /**
     * Version string: assumed to be in format x.x.x
     */
    private static final String VERSION = "0.0.1";

    private static final String DATE_STRING = "2013-2014";

    private static final boolean IS_PRERELEASE = false;

    private static final String REVISION = "$Rev: 1040 $";

    public String getVersion() {
        return VERSION;
    }

    public String getVersionString() {
        return "v" + VERSION + (IS_PRERELEASE ? " Prerelease " + getBuildString() : "");
    }

    public String[] getCredits() {
        return new String[]{
                "Designed and developed by",
                "Dong (Walter) Xie & Alexei J. Drummond",
                "",
                "Department of Computer Science",
                "University of Auckland",
                "walter@cs.auckland.ac.nz",
                "alexei@cs.auckland.ac.nz",
                "",
                "Downloads, Help & Resources:",
                "\thttps://github.com/CompEvol/NZGOT",
                "",
                "Source code distributed under the GNU Lesser General Public License:",
                "\thttps://github.com/CompEvol/NZGOT.git",
                "",
                "Other developers:",
                "\tThomas Hummel",
                "",
                "Thanks to:",
                "\t"};
    }

    public String getHTMLCredits() {
        return null;//TODO
    }

    public String getDateString() {
        return DATE_STRING;
    }

    public String getBuildString() {
        return "r" + REVISION.split(" ")[1];
    }
}
