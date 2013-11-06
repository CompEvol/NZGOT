

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


Reference:

[1] BLAST Command Line Applications User Manual, http://www.ncbi.nlm.nih.gov/books/NBK1762/
[2] How to perform a Blast Search from a Java Application, http://www.biostars.org/p/2887/#2890
[3] JAXB project, https://jaxb.java.net
[4] BioJava CookBook3.0, http://biojava.org/wiki/BioJava:CookBook






