                   NZGOT v0.0.1 2013-2014
            New Zealand Genomic Observatory Toolkit
                             by
                Walter Xie & Alexei J. Drummond

                Department of Computer Science
                     University of Auckland
                    walter@cs.auckland.ac.nz
                    alexei@cs.auckland.ac.nz

Last updated: walter@cs.auckland.ac.nz - 2nd December 2013


=====

The toolkit is initially designed to analyse various types of data collected from
New Zealand Genomic Observatory project, but we are aimed to integrate these tools
to create an open-source framework useful and reusable for data analysis and modelling
in bioinformatics and ecology.

The minimum version of Java 1.7 is required. The whole software has the dependencies
on JEBL (http://jebl.sourceforge.net) and BEAST 2 (http://code.google.com/p/beast2/).
The 3rd party libraries are included in the /lib folder.

Core package in the project is to provide a framework for the development of these
tools. The main features are:
* BLAST+ xml output parser (core.blast)
* Ecological modelling concepts, such as OTU, community, diversity, etc. (core.community)
* JEBL Sequence extension, such as translation class (core.sequences)
* Parsers for related UC files [http://www.drive5.com/], such as importer/export,
uc parser, etc. (core.community.io, core.io, core.uc)
* utils for DNA sequences, Amino Acid sequences, trees, etc. (core.util)

Each tool should contain its main class and build script, and its ownership may be
maintained by different collaborators or research groups.
It contains the following tools at the current version:

1) Community matrix analysis.
Core package: nzgot.cma
Owners: Walter Xie, Alexei Drummond

2) Error correction tool for the next-generation sequencing data.
Core package: nzgot.ec
Owners: Thomas Hummel, Alexei Drummond, Walter Xie







