package nzgot.ec;

import jebl.evolution.align.SystemOut;
import jebl.evolution.align.scores.Blosum80;
import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.GeneticCode;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import nzgot.core.community.OTU;
import nzgot.core.community.OTUs;
import nzgot.core.community.io.CommunityImporter;
import nzgot.core.util.SequenceUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatic error correction
 * @author Thomas Hummel
 * @author Walter Xie
 */
public class AutomaticEC {

//	//Sequence files
//	final String fileSeq = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/IndirectSoil_endTrimmed.fasta";
//	final String fileRef = "/Users/thum167/Documents/Curation/ReRun Clustering/1608_Sanger_translated.fasta";
//	final String fileCor = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/IndirectSoil_test_Sanger.fasta";
//	final String fileControl = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/IndirectSoil_test_Sanger_Control.fasta";
//
//	//Mapping files
//	final String mapSeqOtu = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/mapping/IndirectSoil_userout.m8";
//	final String mapOtuRef = "/Users/thum167/Documents/Curation/ReRun Clustering/IndirectSoil/reference/IndirectSoil_reference_userout_85.m8";

	
//	//Sequence files
//	final String fileSeq = "/Users/thum167/Documents/Curation/ReRun Clustering/DirectSoil/SoilKit_endTrimmed.fasta";
//	final String fileRef = "/Users/thum167/Documents/Curation/ReRun Clustering/iBold/ibol.all.frame0.translation.fasta";
//	final String fileCor = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/corrected.fasta";
//	final String fileControl = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/control.fasta";
//	
//	//Mapping files
//	final String mapSeqOtu = "/Users/thum167/Documents/Curation/ReRun Clustering/DirectSoil/mapping/DirectSoil_userout.m8";
//	final String mapOtuRef = "/Users/thum167/Documents/Curation/ReRun Clustering/iBold/SoilKit/SoilKit_userout_75_ibold.m8";
	
	//Sequence files
	final String fileSeq = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/seq.fasta";
	final String fileRef = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/ref.fasta";
	final String fileCor = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/corrected.fasta";
	final String fileControl = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/control.fasta";
	
	//Mapping files
	final String mapSeqOtu = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/seqOtuMap.txt";
	final String mapOtuRef = "/Users/thum167/Documents/Curation/ReRun Clustering/Automatic error correction/TestScenario/otuRefMap.txt";
	
	List<Sequence> sequences;
	List<Sequence> references;
	List<Sequence> sequencesCor;
	List<Sequence> sequencesControl;

	/**
	 *Automatic error correction of all sequences given 
	 *in the <code>fileSeq</code> file which mapped to the <code>fileRef</code> file 
	 */
	public void doEC() throws IOException, ImportException{
        File sequenceIn = new File(fileSeq);
        File referenceIn = new File(fileRef);
        File sequenceOut = new File(fileCor);
        File sequenceOut2 = new File(fileControl);

//		Mapping map = new Mapping();
		FastaImporter sequenceImport = new FastaImporter(sequenceIn , SequenceType.NUCLEOTIDE);
		FastaImporter referenceImport = new FastaImporter(referenceIn, SequenceType.AMINO_ACID);
		sequences = sequenceImport.importSequences();
		references =referenceImport.importSequences();
		sequencesCor = new ArrayList<Sequence>(2000);
		sequencesControl = new ArrayList<Sequence>(2000);

        File file = new File(mapSeqOtu);
        OTUs otus = new OTUs(file.getName());
        CommunityImporter.importOTUsAndMapping(file, otus);

        file = new File(mapOtuRef);
        CommunityImporter.importReferenceSequenceMapping(file, otus);


//		map.parseSeqOtuTable(mapSeqOtu);
//		map.parseOtuRefTable(mapOtuRef);

        double count = 0;
        double size = sequences.size();

        String referenceLabel;
        String referenceSeq;

        for (Sequence seq : sequences) {

			count++;
			referenceLabel = null;
			referenceSeq = null;

            OTU otu = (OTU) otus.getOTUOfSeq(seq.getTaxon().toString());
            if (otu != null && otu.getReference() != null)
                referenceLabel = otu.getReference().toString();
//			referenceLabel = map.findReference(seq.getTaxon().toString());

			//reference String 
			if (referenceLabel != null)
				referenceSeq = SequenceUtil.getSequenceStringFrom(referenceLabel, references);
//				referenceSeq = Mapping.getReferenceString(referenceLabel, references);
				
			//correct sequence with reference alignment and save in list
			if (referenceSeq != null) {
				AlignAndCorrect ac = new AlignAndCorrect(new Blosum80(), -10, -10, -100, myGeneticCode.INVERTEBRATE_MT);
				ac.doAlignment(seq.getString(), referenceSeq);
				try{
					Sequence correctedSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), ac.getMatch()[1].toString()); //replace gaps with '?'...	
					ac.doMatch(new SystemOut(), "");
					sequencesCor.add(correctedSeq);
					sequencesControl.add(seq);
					Debugger.log( String.format("%.5g", ((count/size))*100) +"%" );
				}
				catch (NullPointerException e) {
					Debugger.log(seq.getTaxon().toString());
				}

			}

			//ac.getCorrected(seq);
			//ac.doMatch(new SystemOut(), "");
		}
        
        //Corrected sequences
        Writer write = new OutputStreamWriter(new FileOutputStream(sequenceOut));
		FastaExporter fe = new FastaExporter(write);
		fe.exportSequences(sequencesCor);
		write.flush();
		write.close();
		
		//Control sequences
		Writer write2 = new OutputStreamWriter(new FileOutputStream(sequenceOut2));
		FastaExporter fe2 = new FastaExporter(write2);
		fe2.exportSequences(sequencesControl);
		write2.flush();
		write2.close();
	}


	public static void main(String[] args) throws ImportException, IOException{
		// TODO Auto-generated method stub
		AutomaticEC ec = new AutomaticEC();
		ec.doEC();
	}

}
