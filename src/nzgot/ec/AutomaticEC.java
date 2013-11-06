package nzgot.ec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jebl.evolution.align.scores.Blosum80;
import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.GaplessSequence;
import jebl.evolution.sequences.*;
import jebl.evolution.sequences.GeneticCode;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;

/**
 * Automatic error correction
 * @author Thomas Hummel
 */
public class AutomaticEC {

	//Sequence files
	final String fileSeq = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/IndirectSoil_endTrimmed.fasta";
	final String fileRef = "/Users/thum167/Documents/Curation/ReRun Clustering/1608_Sanger_translated.fasta";
	final String fileCor = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/IndirectSoil_corrected_fullRef.fasta";

	//Mapping files
	final String mapSeqOtu = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/mapping/IndirectSoil_userout.m8";
	final String mapOtuRef = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/reference/IndirectSoil_reference_userout_85.m8";

	File sequenceIn = new File(fileSeq);
	File referenceIn = new File(fileRef);
	File sequenceOut = new File(fileCor);

	List<Sequence> sequences;
	List<Sequence> references;
	List<Sequence> sequencesCor;

	String referenceLabel;
	String referenceSeq;

	double count = 0;
	double size;
	
	/**
	 *Automatic error correction of all sequences given 
	 *in the <code>fileSeq</code> file which mapped to the <code>fileRef</code> file 
	 */
	public void doEC() throws FileNotFoundException, IOException, ImportException{
		Mapping map = new Mapping();
		FastaImporter sequenceImport = new FastaImporter(sequenceIn , SequenceType.NUCLEOTIDE);
		FastaImporter referenceImport = new FastaImporter(referenceIn, SequenceType.AMINO_ACID);
		sequences = sequenceImport.importSequences();
		references =referenceImport.importSequences();
		sequencesCor = new ArrayList<Sequence>(2000);
		
		map.parseSeqOtuTable(mapSeqOtu);
		map.parseOtuRefTable(mapOtuRef);
		size = sequences.size();


		for (Sequence seq : sequences) {

			count++;
			referenceLabel = null;
			referenceSeq = null;

			referenceLabel = map.findReference(seq.getTaxon().toString());

			//reference String 
			if (referenceLabel != null) {
				referenceSeq = Mapping.getReferenceString(referenceLabel, references);
			}

			//correct sequence with reference alignment and save in list
			if (referenceSeq != null) {
				AlignAndCorrect ac = new AlignAndCorrect(new Blosum80(), -10, -10, -100, GeneticCode.INVERTEBRATE_MT);
				ac.doAlignment(seq.getString(), referenceSeq);
				try{
					Sequence correctedSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), ac.getMatch()[1].toString()); //replace gaps with '?'...	
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
		Writer write = new OutputStreamWriter(new FileOutputStream(sequenceOut));
		FastaExporter fe = new FastaExporter(write);
		fe.exportSequences(sequencesCor);
		write.flush();
		write.close();
	}


	public static void main(String[] args) throws ImportException, FileNotFoundException, IOException{
		// TODO Auto-generated method stub
		AutomaticEC ec = new AutomaticEC();
		ec.doEC();
	}

}
