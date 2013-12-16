package nzgo.tookit.ec;

import nzgo.tookit.core.logger.MyLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This class uses the old mapping files, which are not redundant
 * @author Thomas Hummel
 */
public class oldMapping {
	
	
	//Initial capacity?
	HashMap<String, String> seqOtuMap = new HashMap<String, String>(1000);
	ChooseReference elements = new ChooseReference(1000);
	
	
	//creates HashMap of Sequence OTU Table
	public void parseSeqOtuTable(String filePath) {
		
		try{
			FileReader file = new FileReader(filePath);
			final BufferedReader reader = new BufferedReader(file); 
			String current = reader.readLine();
			while (current != null) { 
				final StringTokenizer st = new StringTokenizer(current, "\t");
				while (st.hasMoreTokens()) {
					
					st.nextToken();
					seqOtuMap.put(st.nextToken(), st.nextToken());
			
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
	
	//creates HashMap of Identity OTU Reference Table
	public void parseBestOtuRefTable(String filePath) {
		
		String otuToken;
		String refToken;
		double idToken;
		
		
		try{
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file); 
			String current = reader.readLine();
			while (current != null) { 
				final StringTokenizer st = new StringTokenizer(current, "\t");
				while (st.hasMoreTokens()) {
					
					idToken = Double.parseDouble(st.nextToken());
					otuToken = st.nextToken();
					refToken = st.nextToken();
					
					elements.addBestElement(otuToken, idToken, refToken);
					
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
	
	public void parseWorstOtuRefTable(String filePath) {
		
		String otuToken;
		String refToken;
		double idToken;
		
		
		try{
			FileReader file = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(file); 
			String current = reader.readLine();
			while (current != null) { 
				final StringTokenizer st = new StringTokenizer(current, "\t");
				while (st.hasMoreTokens()) {
					
					idToken = Double.parseDouble(st.nextToken());
					otuToken = st.nextToken();
					refToken = st.nextToken();
					
					elements.addWorstElement(otuToken, idToken, refToken);
					
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
	
	//returns Reference sequence with highest Identity to OTU
	public String searchBestReference(String seq) {
		
		String seqLabel = seq; 
		String otu = seqOtuMap.get(seqLabel);
		if (otu == null) {
			return null;
		}
		String reference = elements.getReference(otu);
		return reference;
		
	}
public String searchWorstReference(String seq) {
		
		String seqLabel = seq; 
		String otu = seqOtuMap.get(seqLabel);
		if (otu == null) {
			return null;
		}
		String reference = elements.getReference(otu);
		return reference;
		
	}

	
}

	