# Copyright (c) 2013 Roc Project.  This is free software.  See
# LICENSE.txt for details.
#
# Builds all the aspects of Roc until a better build situation comes
# along.


########################################
# Documentation
# =============
#
# Useful Targets and their Descriptions
# -------------------------------------
#
# javadoc: Generate the Java API documentation for this software.
#
# tests: Compile, run core JUnit tests.
#
# usertests: Compile, run JUnit user scenarios (functionality/acceptance
#   tests).
#
# alltests: Combines all above tests.
#
# clean-java: Removes the Java build directory containing all compiled
#   Java classes.
#
# clean-javadoc: Remove generated Java documentation.
#
# clean: Same as clean-java for now.
#
# allclean: Combines all above clean targets as well as removing editor
#   backups (*~) and the base build directory.
#
# listconfig: Lists all internal variables and their values.
#
# Individual Java files can be compiled by making *.class files, e.g.
#     make build/java/mloss/roc/CurveData.class
#
########################################


########################################
# Variables

# JUnit 4 JAR location.  Allow local files to override system ones.
junitJar := $(wildcard $(junit) junit4.jar ~/opt/junit4.jar /usr/share/java/junit4.jar)
ifndef junitJar
$(error Cannot find the JUnit 4 JAR.  Add some alternative locations to the makefile or assign variable 'junit' on the command line)
else
junitJar := $(firstword $(junitJar))
endif

# Project layout
buildBaseDir := build
javaSrcDir := java/src
javaTestDir := java/test
javaDocDir := java/doc
javaBuildDir := $(buildBaseDir)/java
javaPkgDir := mloss/roc

# Java class path
classpath := $(CLASSPATH):$(junitJar):$(CURDIR)/$(javaBuildDir)

# Java sources
javaSrcFiles := $(shell find $(javaSrcDir) -name '*.java' -not -name 'package-info.java' | sort)
javaTestFiles := $(shell find $(javaTestDir) -name '*.java' | sort)
javaDocFiles := $(shell find $(javaSrcDir) -name 'package-info.java' | sort)

# Java classes
javaSrcClasses := $(subst $(javaSrcDir),$(javaBuildDir),$(javaSrcFiles:.java=.class))
javaTestClasses := $(subst $(javaTestDir),$(javaBuildDir),$(javaTestFiles:.java=.class))
javaUnitTestClasses := $(filter %Test.class,$(javaTestClasses))

# List all the phony targets (targets that are really commands, not files)
.PHONY: listconfig tests usertests alltests javadoc clean clean-java clean-javadoc allclean


########################################
# Targets


# Make-related, meta

# Variables for string substitution
emptyString :=
space := $(emptyString) $(emptyString)
indent := $(emptyString)    $(emptyString)

# List variables and values
listconfig:
	@echo Variables:
	@echo junitJar: $(junitJar)
	@echo classpath: $(classpath)
	@echo javaSrcFiles:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaSrcFiles))"
	@echo javaSrcClasses:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaSrcClasses))"
	@echo javaTestFiles:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaTestFiles))"
	@echo javaTestClasses:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaTestClasses))"
	@echo javaUnitTestClasses:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaUnitTestClasses))"


# General targets

# Build directory target.  Use a hidden file to test against because
# directory timestamps are frequently updated and therefore directories
# are not appropriate prerequisites for other targets.
$(buildBaseDir)/.exists:
	mkdir -p $(@D)
	@touch $@
$(javaBuildDir)/.exists:
	mkdir -p $(@D)
	@touch $@


########################################
# Java

# General Java compilation
$(javaBuildDir)/%.class: $(javaBuildDir)/.exists $(javaSrcDir)/%.java
	cd $(javaSrcDir) && javac -cp $(classpath) -d $(CURDIR)/$(javaBuildDir) -source 5 -Xlint $*.java
$(javaBuildDir)/%.class: $(javaBuildDir)/.exists $(javaTestDir)/%.java
	cd $(javaTestDir) && javac -cp $(classpath) -d $(CURDIR)/$(javaBuildDir) -source 5 -Xlint $*.java

# List Java dependencies here
$(javaBuildDir)/$(javaPkgDir)/CurveData.class:
$(javaBuildDir)/$(javaPkgDir)/util/ArrayIterator.class:
$(javaBuildDir)/$(javaPkgDir)/util/Arrays.class:
$(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class:
$(javaBuildDir)/$(javaPkgDir)/CurveDataTest.class: $(javaBuildDir)/$(javaPkgDir)/CurveData.class
$(javaBuildDir)/$(javaPkgDir)/CurveDataBuilderTest.class: $(javaBuildDir)/$(javaPkgDir)/CurveData.class $(javaBuildDir)/$(javaPkgDir)/util/Assert.class $(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class
$(javaBuildDir)/$(javaPkgDir)/CurveDataPrimitivesBuilderTest.class: $(javaBuildDir)/$(javaPkgDir)/CurveData.class $(javaBuildDir)/$(javaPkgDir)/util/Assert.class $(javaBuildDir)/$(javaPkgDir)/util/Arrays.class $(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class
$(javaBuildDir)/$(javaPkgDir)/UserScenarios.class: $(javaBuildDir)/$(javaPkgDir)/CurveData.class
$(javaBuildDir)/$(javaPkgDir)/util/Assert.class:

# Java documentation for just the API, not the tests
javadoc: $(javaDocDir)/index.html

$(javaSrcDir)/doc-files/LICENSE.txt: LICENSE.txt
	mkdir -p $(javaSrcDir)/doc-files
	cp $< $@

# Javadoc level private
$(javaDocDir)/index.html: $(javaSrcDir)/overview.html $(javaSrcDir)/javadocOptions.txt $(javaSrcFiles) $(javaDocFiles) $(javaSrcDir)/doc-files/LICENSE.txt $(javaSrcDir)/overview-summary.html.patch
	javadoc -d $(javaDocDir) -sourcepath $(javaSrcDir) -private @$(javaSrcDir)/javadocOptions.txt -overview $< $(javaSrcFiles)
# Work around javadoc bug where content is put in div with footer class
	-patch --forward --input $(javaSrcDir)/overview-summary.html.patch $(javaDocDir)/overview-summary.html


# TODO javadoc for packaging


# JUnit

# Run unit tests
tests: $(javaUnitTestClasses)
	java -cp $(classpath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaBuildDir)/,,$(javaUnitTestClasses:.class=)))

# Run acceptance tests
usertests: $(javaBuildDir)/$(javaPkgDir)/UserScenarios.class
	java -cp $(classpath) org.junit.runner.JUnitCore mloss.roc.UserScenarios

# Run all tests
alltests: $(javaUnitTestClasses) $(javaBuildDir)/$(javaPkgDir)/UserScenarios.class
	java -cp $(classpath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaBuildDir)/,,$(^:.class=)))


# Packages



# Cleanup

# Remove all derived files
clean: clean-java

clean-java:
	@rm -Rf $(javaBuildDir)

clean-javadoc:
	@rm -Rf $(javaDocDir)

# Named allclean to distinguish from clean* when typing
allclean: clean-java clean-javadoc
	@find -name '*~' -delete
	@rm -Rf $(buildBaseDir)
