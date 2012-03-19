All About Roc
=============


Description
-----------

Roc is software for generating and working with ROC ([Receiver Operating
Characteristic](http://en.wikipedia.org/wiki/Receiver_operating_characteristic))
and PR
([Precision-Recall](http://en.wikipedia.org/wiki/Precision_and_recall))
curves.  These curves are typically used to evaluate classification
approaches in areas such as Machine Learning and Epidemiology.
Scientists and researchers are the target audience of this software.

The goal of this project is to provide software for evaluation curves
that correctly implements the traditional and recent approaches in
languages suited to each investigator's environment.

Roc, the name of the software, is pronounced "rock" like its namesake,
roc, an [enormous, legendary bird of
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
features.  Features are "not applicable", "planned", "implemented",
"tested", or "stable".

<table>
  <thead>
    <tr><th>Feature Description</th><th>Library Status</th><th>CLI Status</th></tr>
  </thead>
  <tbody>
    <tr><td>Calculate curve points</td><td>P</td><td>P</td></tr>
    <tr><td>Calculate curve area</td><td>P</td><td>P</td></tr>
    <tr><td>Maximum achievable area (convex hull)</td><td>P</td><td>P</td></tr>
    <tr><td>Curve aggregation (vertical averaging)</td><td>P</td><td>P</td></tr>
    <tr><td>Curve clipping</td><td>P</td><td>P</td></tr>
    <tr><td>Minimum-aware PR</td><td>P</td><td>P</td></tr>
    <tr><td>Label column input</td><td>P</td><td>P</td></tr>
    <tr><td>Predicted-actual pairs columns input</td><td>P</td><td>P</td></tr>
    <tr><td>Weighted examples</td><td>P</td><td>P</td></tr>
    <tr><td>Confidence intervals</td><td>P</td><td>P</td></tr>
    <tr><td>Plotting</td><td>NA</td><td>P</td></tr>
    <tr><td>Ranking statistics</td><td>P</td><td>P</td></tr>
  </tbody>
</table>


Requirements
------------

* Java 5 if using the Java library
* Python 2.5 if using the command line interface


Contact
-------

* [Kendrick Boyd](http://github.com/kboyd)
* [Aubrey Barnard](http://github.com/afbarnard)


Copyright (c) 2012 Roc Project
This is free software.  See LICENSE.txt for details.
