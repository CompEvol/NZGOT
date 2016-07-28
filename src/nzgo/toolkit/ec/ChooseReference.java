package nzgo.toolkit.ec;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Thomas Hummel
 */ 
public class ChooseReference {
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
 
        public ChooseReference(int initialCapacity) {
                elements = new HashMap<String,Element>(initialCapacity);
        }
 
        public void addBestElement(String key, double value, String string) {
                Element el = elements.get(key);
                if (el == null || el.getValue() < value)
                        elements.put(key, new Element(string, value));
        }
     
        public void addWorstElement(String key, double value, String string) {
            Element el = elements.get(key);
            if (el == null || el.getValue() > value)
                    elements.put(key, new Element(string, value));
    }
 
        public String getReference(String key) {
                Element el = elements.get(key);
                if (el == null)
                        return null;
                return el.getString();
        }
 
}
