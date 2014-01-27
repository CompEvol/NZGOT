package nzgo.toolkit.core.naming;


import java.util.Arrays;
import java.util.List;

/**
 * Assembler
 * @author Walter Xie
 */
public class Assembler {

    private final Separator commandsSeparator = new Separator("\\|"); // for commands arg string
    private Separator separator;
    private Matcher matcher; // match every labels if null
    private Command[] commands;

    public Assembler(String regex, String commandsArg) {
        // set to same as separator of commands arg string
        setSeparator(commandsSeparator);
        if (regex != null) {
            Matcher matcher = new Matcher(regex);
            setMatcher(matcher);
        }
        setCommands(commandsArg);
    }

    public Assembler(String regex1, String regex2, String commandsArg, String traitsMap) {
        this(regex2, commandsArg);
        if (regex1 != null) {
            Separator separator = new Separator(regex1);
            // overwrite separator
            setSeparator(separator);
        }
    }

    /**
     * set commands from a string separated by |
     * @param commandsArg
     */
    public void setCommands(String commandsArg) {
        if (NameUtil.isEmptyNull(commandsArg))
            throw new IllegalArgumentException("Cannot find commands : " + commandsArg);

        String[] coms = commandsSeparator.parse(commandsArg);

        commands = new Command[coms.length];
        for (int i = 0; i < coms.length; i++) {
            Command command = new Command(coms[i]);
            commands[i] = command;
        }
    }

    /**
     * get new label after proceeding all commands
     * @param label
     * @return
     */
    public String getAssembledLabel(String label) {
        if (matcher == null || matcher.isMatched(label)) {
            List<String> items = getItems(label);

            for (Command command : commands) {
                command.proceed(items);
            }

            String assembledLabel = items.get(0);
            for (int i = 1; i < items.size(); i++) {
                assembledLabel += getSeparator() + items.get(i);
            }
            return assembledLabel;
        }
        return label;
    }

    public List<String> getItems(String label) {
        String[] items = separator.parse(label);
        return Arrays.asList(items);
    }

    public Separator getSeparator() {
        if (separator == null)
            return commandsSeparator;
        return separator;
    }

    public void setSeparator(Separator separator) {
        this.separator = separator;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Command[] getCommands() {
        return commands;
    }

    public void setCommands(Command[] commands) {
        this.commands = commands;
    }

    public static enum CommandType {
        MOVE    ("move"),
        DELETE  ("delete"),
        COMBINE ("combine"),
        ADD     ("add_map_of");

        private String type;

        private CommandType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static String[] valuesToString() {
            return Arrays.copyOf(CommandType.values(), CommandType.values().length, String[].class);
        }

        public static String getExample() {
            return MOVE + "(1,5)|" + DELETE + "(2,3,4)|" + COMBINE + "(5,6,7)|" + ADD + "(2)";
        }

    }
}
