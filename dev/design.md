Design and Planning
===================


High-Level Issues
-----------------

* Project name: Roc
* Code license: BSD 2-clause (FreeBSD)


Future Features?
----------------

(Check the README for the current status of implemented features.)

* measures
  * accuracy? (at a particular threshold)
  * Kendall's Tau
  * Kruskal-Wallis?
  * F1?
* one-column (labels sorted according to removed key) or two-column
  input (key-label pairs to be sorted)
  * input via a positive list and a negative list (or files)
* regular PR, minimum PR
* utilities
  * read labels (and weights) from file
  * write points to file
  * sort actual labels by predicted labels
  * input/output conversions (e.g. int[] -> double[])
  * zip/unzip conversions (transpose data)
* one-line analysis, i.e. one function call to read input and write
  output points and areas
* ties in ranking
* aggregation (vertical averaging)
* weighted examples (three-column input)
* clipping of curve
* confidence intervals
* multiple classes
* CLI calls plotting program
* Variants of area under ROC curve (http://www.springerlink.com/content/u5h27552t1642g55/abstract/)
  * scored ROC
  * softROC
  * probROC


Nouns and Verbs
---------------

* curve calculation: data -> curve data
* curve area: curve data -> area under curve
* plotting: curve data -> curve points
* maximum achievable: curve data -> convex hull curve data
* transformation: ROC points <-> PR points
* vertical averaging: curves data -> curve data
* clipping: curve points -> clipped curve points
* ranking
  * data -> ranking -> curve data
  * data -> ranking -> stuff with ranks


Formalized Nouns and Verbs
--------------------------

Nouns (Objects?)
* curve data - information for creating a curve, list of confusion
  matrices - might want a different name for this curve may be
  misleading
* area under curve - number (Auc?)
* curve points - list of points (x,y) defining a curve
* data - list of weight,label

Verbs (Methods)
* make: data -> curve data
* make: curve data -> curve data (ie for maximum achievable using
  convex hull)
* make: curve points -> curve data (for transformation, needs extra
  info like pos and neg counts)
* calculate: curve data -> area under curve
* plot: curve data -> curve points
* average: list of curve points -> curve points? or list of curve data -> curve data?
* clip: curve points -> curve points


Design Questions
----------------

* How accomodate weighted examples?
* How calculate minimum PR score? Treat minimum as separate curve and subtract?


Notes
-----

* Numeric integration shouldn't be necessary.  Only need trapezoids for
  ROC and trapezoid-analogue closed-form solution for PR.


Tools and Languages
-------------------

* [Pandoc](http://johnmacfarlane.net/pandoc/README.html)
  ([Markdown](http://daringfireball.net/projects/markdown/syntax)) for
  documentation
* Java for "pure", reference implementation
* Python?
* Julia?
* Build system?  Scons? Waf?


Competitors
-----------

* MedCalc
* Analyse-it
* Metz group at University of Chicago: ROCKIT (ROCFIT?)
  * derivative JROCFIT
* [StAR](http://protein.bio.puc.cl/cardex/servers/roc/roc_analysis.php)


Copyright (c) 2014 Roc Project.  This is free software.  See LICENSE.txt
for details.
