All About Roc
=============


Description
-----------

Roc is software for generating and working with ROC ([Receiver Operating
Characteristic](http://en.wikipedia.org/wiki/Receiver_operating_characteristic))
and PR
([Precision-Recall](http://en.wikipedia.org/wiki/Precision_and_recall))
curves.  These curves are typically used to evaluate classification
approaches in areas like Machine Learning and Epidemiology.

Roc, the name of the software, is pronounced "rock" like its namesake,
roc, an [enormous, legendary bird of
prey](http://en.wikipedia.org/wiki/Roc_(mythology)).

This software is aimed at scientists and researchers.

One of the goals of this project is to provide software for evaluation
curves that correctly implements the traditional and recent approaches
in languages suited to each investigator's environment.


License
-------

Roc is released under the BSD 2-Clause License (also known as the
FreeBSD License).  See the file LICENSE.txt for details.


Features and Project Maturity
-----------------------------

The software is released as a library and as a command-line interface
(CLI) front-end for the library.  Features are "not applicable",
"planned", "implemented", "tested", or "stable".

----------------------------------------
Feature Description      Library  CLI
                         Status   Status
-----------------------  -------  ------
Calculate curve points   P        P

Calculate curve area     P        P

Maximum achievable area  P        P
(convex hull)

Curve aggregation        P        P
(vertical averaging)

Curve clipping           P        P

Minimum-aware PR         P        P

Label column input       P        P

Predicted-actual pairs   P        P
columns input

Weighted examples        P        P

Confidence intervals     P        P

Plotting                 NA       P

Ranking statistics       P        P

----------------------------------------
