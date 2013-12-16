package nzgo.toolkit.ec;

import nzgo.toolkit.core.logger.MyLogger;

import java.util.*;

/**
 * 
 * @author Thomas Hummel
 */
public class OTU {
	
	public static void countOTU(HashMap<String,String> seqOtuMap) {
		Collection<String> otu = seqOtuMap.values();
		Set<String> uniqueSet = new HashSet<String>(otu);
		
		for(String temp : uniqueSet) {
			MyLogger.info(temp + ": " + Collections.frequency(otu, temp));
		}
	}
	
	public static void sequenceOtuList(HashMap<String,String> seqOtuMap) {
		
		
		
		
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
