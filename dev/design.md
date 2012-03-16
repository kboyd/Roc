Design and Planning
===================


High-Level Issues
-----------------

* Project name: Roc
* Code license: BSD 2-clause (FreeBSD)


Features
--------

* library
* CLI
* curve points
* curve area
* convex hull (or not)
* aggregation (vertical averaging)
* one-column (labels sorted according to removed key) or two-column
  input (key-label pairs to be sorted)
* regular PR, minimum PR
* weighted examples (three-column input)
* clipping of curve


Future Features
---------------

* confidence intervals
* CLI calls plotting program
* ranking statistics/comparisons?


Nouns and Verbs
---------------

* curve calculation: data -> curve data
* curve area: curve data -> area under curve
* plotting: curve data -> curve points
* maximum achievable: curve data -> convex hull curve data
* transformation: ROC points <-> PR points
* vertical averaging: curves data -> curve data
* clipping: curve data -> clipped curve data
* ranking
  * data -> ranking -> curve data
  * data -> ranking -> stuff with ranks


Design Questions
----------------

* How accomodate weighted examples?
* How calculate minimum PR score? Treat minimum as separate curve and subtract?


Notes
-----

* Numeric integration shouldn't be necessary.  Only need trapezoids for
  ROC and trapezoid-analogue closed-form solution for PR.


Development Structure
---------------------

* How organize different languages, tests?
* Build system?  (Probably not Ant.)


Tools and Languages
-------------------

* [Pandoc](http://johnmacfarlane.net/pandoc/README.html)
  ([Markdown](http://daringfireball.net/projects/markdown/syntax)) for
  documentation
* Java for "pure", reference implementation
* Vala for library implementation
* Swig for language bindings
* Python for CLI?
* Build system?  Waf?


Competitors
-----------

* MedCalc
* Analyse-it
* Metz group at University of Chicago: ROCKIT (ROCFIT?)
  * derivative JROCFIT


Copyright (c) 2012 Roc Project
This is free software.  See LICENSE.txt for details.
