package nzgot.ec.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.AminoAcidState;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;

//TODO Fix the class, is not working yet
public class FastaReferenceCleaner  {
	
	static List<Sequence> refSeqs;
	
	
	public List<Sequence> removeStopCodons() {
		
		List<Sequence> codingRefSeqs = refSeqs;
		for(Sequence seq : refSeqs) {
			AminoAcidState[] seqStates = (AminoAcidState[]) seq.getStates();
			for (AminoAcidState st : seqStates) {
				if (st.isStop() == true) {
					codingRefSeqs.remove(seq);
					break;
				}
			}
		}
		return codingRefSeqs;
	}
	public void exportCleanRef(List<Sequence> refSeqs, String filePath) throws FileNotFoundException, ImportException, IOException {
		
		File fileOut = new File(filePath);
		List<Sequence> exportRefSeqs = removeStopCodons();
		Writer write = new OutputStreamWriter(new FileOutputStream(fileOut));
		FastaExporter fe = new FastaExporter(write);
		fe.exportSequences(exportRefSeqs);
		write.flush();
		write.close();
		System.out.println("Done!");
		
	}
	
	public static void main(String args[]) throws IOException, ImportException, FileNotFoundException{
		final String REF_IN = "/Users/thum167/Documents/Curation/ReRun Clustering/iBold/ibol.first400.translate.fasta";
		final String REF_OUT = "/Users/thum167/Documents/Curation/ReRun Clustering/iBold/clean.ibol.first400.translate.fasta";
		
		File fileIn = new File(REF_IN);
		
		
		FastaReferenceCleaner cleaner = new FastaReferenceCleaner();
		FastaImporter fa = new FastaImporter(fileIn, SequenceType.AMINO_ACID);
		refSeqs = fa.importSequences();
		
		cleaner.exportCleanRef(refSeqs, REF_OUT);
		
	}
	
	
}
