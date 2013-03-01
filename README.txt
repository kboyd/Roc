All About Roc
=============


Description
-----------

Roc is software for generating and working with ROC ([Receiver Operating
Characteristic](http://en.wikipedia.org/wiki/Receiver_operating_characteristic))
and PR
([Precision-Recall](http://en.wikipedia.org/wiki/Precision_and_recall))
curves.  These curves are typically used to evaluate classification
approaches in areas such as Machine Learning, Statistics, Medicine, and
Epidemiology.  Students, scientists, and researchers are the target
audience of this software.

The goal of this project is to provide software for evaluating ROC and
PR curves that correctly implements the traditional and recent
approaches using languages allowing for flexibility in each
investigator's environment.

Roc, the name of the software, is pronounced "rock" like its namesake,
roc, an enormous, legendary [bird of
prey](http://en.wikipedia.org/wiki/Roc_(mythology\)).


License
-------

Roc is free, open source software.  It is released under the BSD
2-Clause License (also known as the FreeBSD License).  See the file
LICENSE.txt for details.


Features and Project Maturity
-----------------------------

This software is in the early design and development stages.  It is
planned to be released as a library and as a command-line interface
(CLI) front-end for the library.  The table below contains a summary of
features.

Features are NP="not planned", P="planned", I="implemented", T="tested",
S="stable".  Languages are J="Java", P2="Python 2.x".

Feature Description           Library Status  CLI Status (P2)
-------------------           --------------  ---------------
ROC curves
. Points                      J:T  P2:P       P
. Area                        J:T  P2:P       P
. Maximum area (convex hull)  J:T  P2:P       P
. Aggregation (averaging)     J:P  P2:P       P
. Confidence bounds           J:P  P2:P       P
. Clipping                    J:P  P2:P       P
PR curves
. Points                      J:P  P2:P       P
. Area                        J:P  P2:P       P
. Maximum area (convex hull)  J:P  P2:P       P
. Aggregation (averaging)     J:P  P2:P       P
. Confidence bounds           J:P  P2:P       P
. Clipping                    J:P  P2:P       P
. Minimum awareness           J:P  P2:P       P
Plotting                      J:NP P2:P       P
Inputs
. Ranking                     J:T  P2:P       P
. Predicteds, actuals         J:T  P2:P       P
. Predicted-actual pairs      J:P  P2:P       P
. Example weights             J:P  P2:P       P
Convenience
. File I/O                    J:P  P2:P       P
Ranking Statistics
. Mann-Whitney-U              J:T  P2:P       P


Requirements
------------

* Java 5 (or later) if using the Java library
* Python 2.5 (or later, but not 3.x) if using the command line interface
  or Python library


Java Library and JAR
--------------------

The Java library provides an API for working with ROC and PR curves in
your Java programs.  It is distributed as a Java archive (JAR)
containing source code, bytecode, and documentation.  The JAR can be
obtained at TODO.  To include the library in your Java project, just
place the JAR in a convenient location and include it in your classpath.
You can browse the documentation by extracting it from the JAR or by
viewing the [latest version on
GitHub](http://kboyd.github.com/Roc/javadoc/).


Contact
-------

* [Kendrick Boyd](http://github.com/kboyd)
* [Aubrey Barnard](http://github.com/afbarnard)


Copyright (c) 2013 Roc Project.  This is free software.  See LICENSE.txt
for details.
