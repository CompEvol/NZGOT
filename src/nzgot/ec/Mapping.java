package nzgot.ec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import jebl.evolution.align.scores.Blosum80;
import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.*;


/**
 * This class uses the new mapping file...
 * two mapping files required: read-otu-map, otu-reference-map
 * both files are produced with USEARCH with default option parameters
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
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("I/O-error");
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
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("I/O-error");
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

	
	public static void main(String[] args) throws IOException, ImportException {
		
		//Sequence files
		String fileSeq = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/IndirectSoil_endTrimmed.fasta";
		String fileRef = "/Users/thum167/Documents/Curation/ReRun Clustering/1608_Sanger_translated.fasta";
		String fileCor = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/IndirectSoil_corrected_fullRef.fasta";
		
		//Mapping files
		String mapSeqOtu = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/mapping/IndirectSoil_userout.m8";
		String mapOtuRef = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/reference/IndirectSoil_reference_userout_85.m8";
		
		File sequenceIn = new File(fileSeq);
		File referenceIn = new File(fileRef);
		File sequenceOut = new File(fileCor);

		Mapping map = new Mapping();
		map.parseSeqOtuTable(mapSeqOtu);
		map.parseOtuRefTable(mapOtuRef);
		
		//create sequence lists
		FastaImporter sequenceImport = new FastaImporter(sequenceIn , SequenceType.NUCLEOTIDE);
		FastaImporter referenceImport = new FastaImporter(referenceIn, SequenceType.AMINO_ACID);

		List<Sequence> sequences = sequenceImport.importSequences();
		List<Sequence> references =referenceImport.importSequences();
		List<Sequence> sequencesCor = new ArrayList<Sequence>(2000);

		
		String referenceLabel;
		String referenceSeq;
		
		double count = 0;
		double size = sequences.size();

		//Align and correct loop
		for (Sequence seq : sequences) {
			
			count++;
			referenceLabel = null;
			referenceSeq = null;
			
			referenceLabel = map.findReference(seq.getTaxon().toString());
			
			//reference String 
			if (referenceLabel != null) {
				referenceSeq = getReferenceString(referenceLabel, references);
			}
			
			//correct sequence with reference alignment and save in list
			if (referenceSeq != null) {
				AlignAndCorrect ac = new AlignAndCorrect(new Blosum80(), -10, -10, -100, GeneticCode.INVERTEBRATE_MT);
				ac.doAlignment(seq.getString(), referenceSeq);
				try{
					Sequence correctedSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), ac.getMatch()[1].toString().replace('-', '\0')); //replace gaps with '?'...	
					sequencesCor.add(correctedSeq);
					Debugger.log( String.format("%.5g", ((count/size))*100) +"%" );
				}
				catch (NullPointerException e) {
					Debugger.log(seq.getTaxon().toString());
				}
				
			}

			//ac.getCorrected(seq);
			//ac.doMatch(new SystemOut(), "");
		}
		
		//export corrected sequences
		Writer write = new OutputStreamWriter(new FileOutputStream(sequenceOut));
		FastaExporter fe = new FastaExporter(write);
		fe.exportSequences(sequencesCor);
		write.flush();
		write.close();

	}
	
}

	