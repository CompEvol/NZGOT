package nzgo.toolkit.core.naming;

/**
 * @author Walter Xie
 */
public enum RegexType {

    SEPARATOR("separator"),
    MATCHER  ("matcher"),
    REGEX    ("regular expression");

    private String type;

    private RegexType(String type) {
        this.type = type;
    }

    public static String[] getRegexTypes() {
        return new String[]{SEPARATOR.toString(), MATCHER.toString()};
    }

    @Override
    public String toString() {
        return type;
    }
}
