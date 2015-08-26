package nzgo.toolkit.core.naming;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.NumberUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Assembler
 * @author Walter Xie
 */
public class Command {

    protected final Assembler.CommandType commandType;
    protected final Integer[] indexesInCommand;
    protected final Map<String,String> traitMap;

    private final Separator indexesSeparator = new Separator(","); // for indexes between ()

    public Command(String command) {
        this(command, null);
    }

    /**
     * traitMap is null if not add
     * @param command
     * @param traitMap
     */
    public Command(String command, Map<String,String> traitMap) {
        this.commandType = getCommandType(command);
        this.indexesInCommand = getIndexesInCommand(command);
        this.traitMap = traitMap;

        if (commandType == Assembler.CommandType.ADD_ITEM_MAPPED && traitMap == null)
            throw new IllegalArgumentException("Need trait map to use \"" + Assembler.CommandType.ADD_ITEM_MAPPED + "\" command !");

    }

    public void proceed(List<String> items) {
        switch(commandType) {
            case MOVE:
                move(items, indexesInCommand[0], indexesInCommand[1]);
                break;
            case DELETE:
                delete(items, indexesInCommand);
                break;
            case COMBINE:
                combine(items, indexesInCommand[0], Arrays.copyOfRange(indexesInCommand, 1, indexesInCommand.length));
                break;
            case ADD_ITEM_MAPPED:
                addItemMapped(items, indexesInCommand[0]);
                break;
            default:
                throw new IllegalArgumentException("Try to proceed an illegal command : " + commandType);
        }
    }

    /**
     * e.g. "move" in "move(1,5)"
     * @param command
     * @return
     */
    public Assembler.CommandType getCommandType(String command) {
        String com = command.substring(0, command.indexOf("("));

        return Assembler.CommandType.valueOf(com);
    }

    /**
     * e.g. "1,5" in "move(1,5)"
     * @param command
     * @return
     */
    public Integer[] getIndexesInCommand(String command) {
        String ids = command.substring(command.indexOf("("), command.indexOf(")"));
        String[] idArray = indexesSeparator.parse(ids);
        return NumberUtil.parseNumbers(idArray);
    }

    public void move(List<String> items, int from, int to) {
        validate(items.size(), from);
        validateToIndex(items.size(), to);
        String item = items.remove(from);

        if (to <= from) {
            items.add(to, item);
        } else if (to-1 < items.size()) { //items.size() = pre size -1, becuase of items.remove(from)
            items.add(to-1, item);
        } else {
            items.add(item);
        }
    }

    public void delete(List<String> items, Integer... indexes) {
        validate(items.size(), indexes);
        for (int i = 0; i < indexes.length; i++) {
            items.remove(indexes[i]-i);
        }
    }

    /**
     * if 1st item null or empty, take 2nd item, otherwise always take 1st
     * and so on ...
     * @param indexes
     */
    public void combine(List<String> items, int combineTo, Integer... indexes) {
        validate(items.size(), combineTo);
        validate(items.size(), indexes);
        String item = items.get(combineTo);
        if (!NameUtil.isEmptyNull(item)) {
            delete(items, indexes);
            return;
        }
        for (int i = 0; i < indexes.length; i++) {
            item = items.get(indexes[i] - i);
            if (!NameUtil.isEmptyNull(item)) {
                items.set(combineTo, item);
                delete(items, indexes);
                return;
            }
        }
    }

    /**
     * add the mapped trait of items.get(indexOfKeyInMap) into last items
     * @param items
     * @param indexOfKeyInMap
     */
    public void addItemMapped(List<String> items, int indexOfKeyInMap) {
        validate(items.size(), indexOfKeyInMap);
        String item = items.get(indexOfKeyInMap);
        String trait = traitMap.get(item);
        if (!NameUtil.isEmptyNull(trait)) {
            items.add(trait);
        } else {
            items.add(NameParser.OTHER);
            MyLogger.warn("Find no mapping for item : " + item);
        }
    }

    private void validate(int size, Integer... fromIndexes) {
        for (int i : fromIndexes) {
            if (i >= size)
                throw new IllegalArgumentException("Item index " + i + " has to < items size " + size);
        }
    }

    private void validateToIndex(int size, int toIndex) {
        if (toIndex > size)
            throw new IllegalArgumentException("Move to index " + toIndex + " has to <= items size " + size);
    }

}
