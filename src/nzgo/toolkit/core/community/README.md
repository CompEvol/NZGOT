

Last updated: walter@cs.auckland.ac.nz - 7th November 2013

Pseudocode of Class relationship in community package:

BioSortedSet is a TreeSet (Java)
OTU is a BioSortedSet
OTUs is a BioSortedSet
Community is a OTUs

OTU has many Read (String) or Sequence (JEBL)
OTU has one Reference
OTU has one AlphaDiversity
OTUs has many OTU








