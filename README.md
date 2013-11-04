                   NZGOT v0.0.1 2013-2014
            New Zealand Genomic Observatory Toolkit
                             by
                Walter Xie & Alexei J. Drummond

                Department of Computer Science
                     University of Auckland
                    walter@cs.auckland.ac.nz
                    alexei@cs.auckland.ac.nz

Last updated: walter@cs.auckland.ac.nz - 22nd October 2013


=====

The toolkit is initially designed to analyse various types of data collected from
New Zealand Genomic Observatory project, but we are aimed to integrate these tools
to create an open-source framework useful and reusable for data analysis and modelling
in bioinformatics and ecology.

The minimum version of Java 1.7 is required. The whole software has the dependencies
on JEBL (http://jebl.sourceforge.net).

Each tool should have its core package containing the main code, and separated
owners who have its copyright and are responsible to maintain it.
It contains the following tools:

1) Community matrix analysis.
Core package: nzgot.cma
Owners: Walter Xie, Alexei Drummond

2) Error correction tool for the next-generation sequencing data.
Core package: nzgot.ec
Owners: Thomas Hummel, Alexei Drummond, Walter Xie







