Flowgrams are different from DNA sequence as flowgrams are the reads from 454 and/or Ion Torrent sequencer (by Pyrosequencing). Flowgram of length m can be denoted by sequence of flows F= (F1, F2, F3……. Fm) where each flow F can be represented by pair (b, f) of nucleotide and its associated fractional intensity. Interestingly, while denoting flowgram (by 454 sequencing) we follow typical order of T, A, C, G.

Earlier work on aligning flowgrams to DNA sequences used base calling. Although there are methods to improve base calling but they is always a potential that there can be loss of information from base calling. For example (A, 3.7) can be AAA or AAAA (result by rounding).

The new algorithm proposed by M.Martin and Sven Rahmann in their paper uses dynamic programming to align a flowgram directly to DNA sequence. So there is no need to convert sequence to flowgram or vice-versa. 

Link:
GCB’13 Paper-“Aligning Flowgrams to DNA Sequences” by Marcel Martin and Sven Rahmann.
http://drops.dagstuhl.de/opus/volltexte/2013/4237/pdf/p125-martin.pdf

