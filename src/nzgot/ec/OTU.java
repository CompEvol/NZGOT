package nzgot.ec;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Thomas Hummel
 */
public class OTU {
	
	public static void countOTU(HashMap<String,String> seqOtuMap) {
		Collection<String> otu = seqOtuMap.values();
		Set<String> uniqueSet = new HashSet<String>(otu);
		
		for(String temp : uniqueSet) {
			System.out.println(temp+": "+ Collections.frequency(otu, temp));
		}
	}
/*	public static void sequenceOtuList(HashMap<String,String> seqOtuMap) {
		Set<Map.Entry<String, String>> set = seqOtuMap.entrySet();
		Collection<String> otu = seqOtuMap.values();
		Set<String> uniqueSet = new HashSet<String>(otu);
		
		Set<String> keySet = seqOtuMap.keySet();
		
		for(String head : uniqueSet) {
			System.out.print("\n"+ head+": ");
			for (String key : keySet) {
				if (seqOtuMap.get(key) == head) {
					System.out.print(key+",");
				}
			}
		}
	}*/
	
	
	

	public static void main(String[] args) {
		Mapping map = new Mapping();
		map.parseSeqOtuTable("/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/mapping/IndirectSoil_userout.m8");
		//countOTU(Mapping.seqOtuMap);
		//sequenceOtuList(Mapping.seqOtuMap);
	}

}
