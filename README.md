Parallel_LDA_for_Lucene
=======================

A conceptual and compact procedure to extract topics from Lucene documents in a time efficient manner.

The algorithm used to perform a multi-threaded version of the LDA is taken from Aapo Kyrölä's project Report: Parallel LDA, Truth or Dare?

Depending on the number of the available worker threads, the terms in the Lucene Index are partitioned in such a way that each worker thread operates on a distict set of words exclusive to that thread for all documents

The main class :ParallelLDASimulator


