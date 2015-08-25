                   NZGOT v0.1.0 2013-2015
            New Zealand Genomic Observatory Toolkit
                             by
                         Walter Xie

                Department of Computer Science
                     University of Auckland
                    walter@cs.auckland.ac.nz

Last updated: 25th August 2015


=====

The toolkit is initially designed to analyse various types of data collected from
New Zealand Genomic Observatory project, but we are aimed to integrate these tools
to create an open-source framework useful and reusable for data analysis and modelling
in bioinformatics and ecology.

The minimum version of Java 1.7 is required. The whole software has the dependencies
on JEBL (http://jebl.sourceforge.net) and BEAST 2 (https://github.com/CompEvol/beast2.git).
The 3rd party libraries are included in the /lib folder.

Core package in this project is to provide a framework for an easy development of other
packages. Its main features are:
* BLAST+ xml output parser (core.blast)
* Ecological modelling concepts, such as OTU, community, diversity, etc. (core.community)
* JEBL Sequence extension, such as translation class (core.sequences)
* Parsers for related UC files [http://www.drive5.com/], such as importer/export,
uc parser, etc. (core.community.io, core.io, core.uc)
* utils for DNA sequences, Amino Acid sequences, trees, etc. (core.util)

Each of other packages represents an independent project, but it may share the code with
others. Core package is always on the top of dependency, it cannot contain any code of
other packages.
The available packages at the current version are:

1) resemble R data frame and read.table
Main package: nzgo.toolkit.r
Owners: Walter Xie

2) useful tools to parse UPARSE pipeline outputs, and create community matrices.
Main package: nzgo.toolkit.uparse, nzgo.toolkit.edna
Owners: Walter Xie

3) useful tools to parse BLAST outputs, and taxonomic identification from NCBI.
Main package: nzgo.core.ncbi
Owners: Walter Xie

4) error correction tool for the next-generation sequencing data.
Main package: nzgo.toolkit.ec
Owners: Thomas Hummel, Alexei Drummond, Walter Xie

5) useful tools for BEAST 2 project.
Main package: nzgo.toolkit.beast
Owners: Walter Xie


Developer guide:
How to share IntelliJ IDEA project http://devnet.jetbrains.com/docs/DOC-1186



