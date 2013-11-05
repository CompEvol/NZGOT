package nzgot.ec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Hummel
 */ 
public class BestReference {
        private static class Element {
                private final String string;
                public final double value;
 
                public Element(String string, double value) {
                        this.string = string;
                        this.value = value;
                }
 
                public String getString() {
                        return string;
                }
 
                public double getValue() {
                        return value;
                }
 
        }
 
        private final Map<String, Element> elements;
 
        public BestReference(int initialCapacity) {
                elements = new HashMap<String,Element>(initialCapacity);
        }
 
        public void addElement(String key, double value, String string) {
                Element el = elements.get(key);
                if (el == null || el.getValue() < value)
                        elements.put(key, new Element(string, value));
        }
 
        public String getBestReference(String key) {
                Element el = elements.get(key);
                if (el == null)
                        return null;
                return el.getString();
        }
 
}
