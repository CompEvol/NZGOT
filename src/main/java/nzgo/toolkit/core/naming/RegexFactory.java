package nzgo.toolkit.core.naming;


/**
 * @author Walter Xie
 */
public class RegexFactory {

    public RegexType regexType;

    public RegexFactory(RegexType regexType) {
        this.regexType = regexType;
    }

    public Regex getRegex(String regex){
        if(regexType == null || regex == null){
            return null;
        }
        if(regexType == RegexType.SEPARATOR){
            return new Separator(regex);
        } else if(regexType == RegexType.MATCHER){
            return new Matcher(regex);
        }
        return new Regex(regex);
    }

    public void setValue(Regex regex, String value){
        if(regexType == RegexType.SEPARATOR){
            if (!NameUtil.isNumber(value))
                throw new IllegalArgumentException("The 2nd column is not integer : " + value);

            int splitIndex = Integer.parseInt(value);
            ((Separator) regex).setSplitIndex(splitIndex);

        } else if(regexType == RegexType.MATCHER){

            ((Matcher) regex).setName(value);

        }
    }

    /**
     * @author Walter Xie
     */
    public static enum RegexType {

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
}
