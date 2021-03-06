Roc Project Development and Style Guide
=======================================


Style
-----


### General Style ###

* Lines wrapped at 72 characters (per Python).
* Two spaces between sentences.  (Easier to read in fixed-width text and
  Emacs default.  Also less ambiguous.)
* Two empty lines between sections.
* Abbreviations treated like words with respect to capitalization
  etc. in identifiers, code, file names, etc. (contrary to Java
  style).  For example, "Http", "Xml", "Csv", "Roc", "Pr".
  Abbreviations regular in other text (e.g. documentation).
* Punctuation outside quotation marks to mimic programming style, even
  in prose.
* License notice part of every file.
  > Copyright (c) <year> Roc Project.  This is free software.  See LICENSE.txt for details.
  * Bottom for docs.
  * Top for code.
  * Update year when edit file.
* Spaces not tabs.
* M-x delete-trailing-whitespace before committing.
* Otherwise standard per-language style unless noted.
* Specific to-do items marked with TODO and searched with `grep -R TODO *`.


### Pandoc / Markdown ###

* Pandoc extensions where appropriate (but don't expect GitHub to
  interpret (What to do about that?))
* Emacs [Markdown mode](http://jblevins.org/projects/markdown-mode/).
* ".md" extension per GitHub.  But what if pandoc?
* Newline after heading.  Two after title.
* Two spaces between table columns (if using spaces)
* Do hanging indents, no "lazy" styles
* Indent nested list items to text indent
  * Like so


Development
-----------


### Social and Political Model ###

* Be mindful of reducing barriers to entry (poor documentation,
  difficult setup) and making people feel welcome.  You never know who
  you alienate (users or developers) because they just go away.

**TODO**


### Communication Model ###

* All bugs, issues, enhancements, etc. on Github issue tracker.
* Mailing list.  Do on Github or accomplished via other Github features?
* Tips from Producing Open Source Software:
  * Public discussions
  * Civility, keeping discussions friendly, polite

**TODO**


### Project Layout ###

* Top-level directories organized by language or other high-level needs
  (e.g. tools)
* Organize subdirectories of languages by convention
  * Java has `src` and `test` directories, perhaps `doc`
* High-level structure documented in file CONTENTS.md.  (Maintain it!)
  Only guidelines, intentions, and ideas should exist in this file.  All
  specific information or details go in CONTENTS.md.


### Branching Model ###

* Everything derives from `master`.  It is used for integration and
  release.  It should be as stable as possible.
* Develop on your own branch: <initials>/<topicName>
* Create other branches as needed for bugs, large features, etc.
* Meta can be done directly on `master` or on your own branch depending
  on where it fits best.


References
----------

* [Producing Open Source
  Software](http://producingoss.com/en/index.html) I found this via a
  web search and then realized I had seen it on Jeff Atwood's
  recommended reading list.


Copyright (c) 2014 Roc Project.  This is free software.  See LICENSE.txt
for details.
