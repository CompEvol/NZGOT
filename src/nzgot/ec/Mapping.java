package nzgot.ec;

import jebl.evolution.sequences.Sequence;
import nzgot.core.logger.MyLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


/**
 * This class parse the new mapping files
 * <br>two mapping files required: read-otu-map, otu-reference-map
 * <br>both files are produced with USEARCH with default option parameters
 * @author Thomas Hummel
 */
public class Mapping {
	
	
	//Initial capacity?
	static HashMap<String, String> seqOtuMap = new HashMap<String, String>(10000);
	static HashMap<String, String> otuRefMap = new HashMap<String, String>(100); 
	
	/**
	 *Creates HashMap of Sequence OTU Table
	 *@param filePath file path of the sequence-OTU-table
	 */
	public void parseSeqOtuTable(String filePath) {
		
		try{
			FileReader file = new FileReader(filePath);
			final BufferedReader reader = new BufferedReader(file); 
			String current = reader.readLine();
			while (current != null) { 
				final StringTokenizer st = new StringTokenizer(current, "\t");
				while (st.hasMoreTokens()) {
					
					seqOtuMap.put(st.nextToken(), st.nextToken()); //key: read, value: otu
			
				}
				current = reader.readLine();
			}
			
			reader.close();
			file.close();

		} catch (FileNotFoundException e) {
            MyLogger.error(e.toString());
        } catch (IOException e) {
            MyLogger.error("I/O-error");
		}
	}
	
	/**
	*creates HashMap of Identity OTU Reference Table
	*
	*@param filePath file path of the OTU-reference-table
	*/
	public void parseOtuRefTable(String filePath) {
				
		try{
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file); 
			String current = reader.readLine();
			while (current != null) { 
				final StringTokenizer st = new StringTokenizer(current, "\t");
				while (st.hasMoreTokens()) {
					
					st.nextToken();	//ignore id column
					otuRefMap.put(st.nextToken(), st.nextToken());	
				}
				
				current = reader.readLine();
			}
			
			reader.close();
			file.close();


		} catch (FileNotFoundException e) {
			MyLogger.error(e.toString());
		} catch (IOException e) {
			MyLogger.error("I/O-error");
		}
	}
	
	/**
	 * find reference sequence with highest Identity to OTU of query sequence
	 * @param seq query sequence
	 * @return the reference sequence
	 */
	public String findReference(String seq) {
		
		String seqLabel = seq; 
		String otu = seqOtuMap.get(seqLabel);
		if (otu == null) {
			return null;
		}
		String reference = otuRefMap.get(otu);
		return reference;
		
	}
	
	/**
	 * return sequences of given OTU file
	 * 
	 */
	public void searchOTU(String filePathOTU, String filePathMap) {
		
	}
	
	/**
	 * get reference sequence string of given reference label
	 * @param referenceLabel Label of query reference sequence
	 * @param references Sequence list of reference sequences
	 * @return reference amino acid sequence
	 */
	public static String getReferenceString(String referenceLabel, List<Sequence> references) {
		
		String result = null;
		String refName;
		
		for (Sequence ref : references) {
			refName = ref.getTaxon().toString();
			if (refName.contentEquals(referenceLabel)) {
				result = ref.getString();
			}
		}
		return result;
			
	}
	
}

	