

Last updated: walter@cs.auckland.ac.nz - 5th November 2013

BLAST+ 2.2.28
blastn -db nt -query ?.fasta -out ?.xml -outfmt "5 staxids"

NCBI BLAST OUTPUT DTD: 
http://www.ncbi.nlm.nih.gov/dtd/NCBI_BlastOutput.dtd

XJC binding compiler: xjc 2.2.4-2

xjc -p blast -dtd "http://www.ncbi.nlm.nih.gov/dtd/NCBI_BlastOutput.dtd"

parsing a schema...
compiling a schema...

blast/BlastOutput.java
blast/BlastOutputIterations.java
blast/BlastOutputMbstat.java
blast/BlastOutputParam.java
blast/Hit.java
blast/HitHsps.java
blast/Hsp.java
blast/Iteration.java
blast/IterationHits.java
blast/IterationStat.java
blast/ObjectFactory.java
blast/Parameters.java
blast/Statistics.java

Example of XML output:

<?xml version="1.0"?>
<!DOCTYPE BlastOutput PUBLIC "-//NCBI//NCBI BlastOutput/EN" "NCBI_BlastOutput.dtd">
<BlastOutput>
  <BlastOutput_program>blastn</BlastOutput_program>
  <BlastOutput_version>blastn 2.2.3 [May-13-2002]</BlastOutput_version>
  <BlastOutput_reference>~Reference: Altschul, Stephen F., Thomas L. Madden, Alejandro A. Schaffer, ~Jinghui Zhang, Zheng Zhang, Web
b Miller, and David J. Lipman (1997), ~&quot;Gapped BLAST and PSI-BLAST: a new generation of protein database search~programs&quot;,
  Nucleic Acids Res. 25:3389-3402.</BlastOutput_reference>
  <BlastOutput_db>embl</BlastOutput_db>
  <BlastOutput_query-ID>lcl|QUERY</BlastOutput_query-ID>
  <BlastOutput_query-def>AF178033</BlastOutput_query-def>
  <BlastOutput_query-len>811</BlastOutput_query-len>
  <BlastOutput_param>
    <Parameters>
      <Parameters_expect>10</Parameters_expect>
      <Parameters_include>0</Parameters_include>
      <Parameters_sc-match>1</Parameters_sc-match>
      <Parameters_sc-mismatch>-3</Parameters_sc-mismatch>
      <Parameters_gap-open>5</Parameters_gap-open>
      <Parameters_gap-extend>2</Parameters_gap-extend>
      <Parameters_filter>D</Parameters_filter>
    </Parameters>
  </BlastOutput_param>
  <BlastOutput_iterations>
    <Iteration>
      <Iteration_iter-num>1</Iteration_iter-num>
      <Iteration_hits>
        <Hit>
          <Hit_num>1</Hit_num>
          <Hit_id>gnl|BL_ORD_ID|142512</Hit_id>
          <Hit_def>EMORG:AF178033 Af178033 Poecilia reticulata from Trinidad and Tobago NADH dehydrogenase subunit 2 (NADH2) gene, p
artial cds; mitochondrial gene for mitochondrial product. 3/2002</Hit_def>
          <Hit_accession>142512</Hit_accession>
          <Hit_len>811</Hit_len>
          <Hit_hsps>
            <Hsp>
              <Hsp_num>1</Hsp_num>
              <Hsp_bit-score>1566.56</Hsp_bit-score>
              <Hsp_score>790</Hsp_score>
              <Hsp_evalue>0</Hsp_evalue>
              <Hsp_query-from>1</Hsp_query-from>
              <Hsp_query-to>811</Hsp_query-to>
              <Hsp_hit-from>1</Hsp_hit-from>
              <Hsp_hit-to>811</Hsp_hit-to>
              <Hsp_pattern-from>0</Hsp_pattern-from>
              <Hsp_pattern-to>0</Hsp_pattern-to>
              <Hsp_query-frame>1</Hsp_query-frame>
              <Hsp_hit-frame>1</Hsp_hit-frame>
              <Hsp_identity>811</Hsp_identity>
              <Hsp_positive>811</Hsp_positive>
              <Hsp_gaps>0</Hsp_gaps>
              <Hsp_align-len>811</Hsp_align-len>
              <Hsp_density>0</Hsp_density>
              <Hsp_qseq>AGCACCCACTGGTATCTTGCCTGAATAGGAATTGAAATTAACACATTAGCCATTATCCCCCTAATATCACAAAACCACACCCCACGAGCAACTGAGGCCACCACTAAA
GCCATAAAAATTGGACTTGCCCCCCTTCACAGCTGAATACCAGAAGTAATACAAGGCTTAAGCCTACTTAATGGATTAATTCTATCCACTTGACAAAAACTTGCCCCCCTTTACCTCATCTACCAAATTCAA
CCAACCAACTCCAACATTTTTATTACCCTAGGACTTCTATCCATTATTGTAGGGGGGTGAGGGGGATTTAACCAAGTACAACTCCGAAAAATCCTAGCATACTCATCAATTGCCCACTTAGGGTGAATAATT
TTAATTCTTTCATTCTCACCTCCACTAGCCCTACTCACAATCCTAATTTATCTCCTAATAACCTTCTCACTATTTTCTTCTTTTATATTAACCCGAACCACACACATCAACTCTCTAGCCACTACATGGGCC
AAAATCCCAATTCTAACCATCTCAGCCCCCCTAGTCCTATTATCCCTAGGAGGATTGCCCCCTCTTACAGGATTTATACCAAAATGACTTATTCTCCAAGAATTAACAAAGCAAGACCTAGCCCCAATTGCC
ACTCTAGCCGCACTTTCATCCCTATTCAGCCTATATTTTTATC</Hsp_qseq>
              <Hsp_hseq>AGCACCCACTGGTATCTTGCCTGAATAGGAATTGAAATTAACACATTAGCCATTATCCCCCTAATATCACAAAACCACACCCCACGAGCAACTGAGGCCACCACTAAA
GCCATAAAAATTGGACTTGCCCCCCTTCACAGCTGAATACCAGAAGTAATACAAGGCTTAAGCCTACTTAATGGATTAATTCTATCCACTTGACAAAAACTTGCCCCCCTTTACCTCATCTACCAAATTCAA
CCAACCAACTCCAACATTTTTATTACCCTAGGACTTCTATCCATTATTGTAGGGGGGTGAGGGGGATTTAACCAAGTACAACTCCGAAAAATCCTAGCATACTCATCAATTGCCCACTTAGGGTGAATAATT
TTAATTCTTTCATTCTCACCTCCACTAGCCCTACTCACAATCCTAATTTATCTCCTAATAACCTTCTCACTATTTTCTTCTTTTATATTAACCCGAACCACACACATCAACTCTCTAGCCACTACATGGGCC
AAAATCCCAATTCTAACCATCTCAGCCCCCCTAGTCCTATTATCCCTAGGAGGATTGCCCCCTCTTACAGGATTTATACCAAAATGACTTATTCTCCAAGAATTAACAAAGCAAGACCTAGCCCCAATTGCC
ACTCTAGCCGCACTTTCATCCCTATTCAGCCTATATTTTTATC</Hsp_hseq>
              <Hsp_midline>|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
||||||||||||||||||||||||||||||||||||||||||||||</Hsp_midline>
            </Hsp>
          </Hit_hsps>
        </Hit>
        <Hit>
          <Hit_num>2</Hit_num>
        ...

Reference:

[1] BLAST Command Line Applications User Manual, http://www.ncbi.nlm.nih.gov/books/NBK1762/
[2] How to perform a Blast Search from a Java Application, http://www.biostars.org/p/2887/#2890
[3] JAXB project, https://jaxb.java.net
[4] BioJava CookBook3.0, http://biojava.org/wiki/BioJava:CookBook






