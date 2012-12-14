# Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
#
# Builds all the aspects of Roc until a better build situation comes
# along.


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
javaBuildDir := $(buildBaseDir)/java
javaPkgDir := mloss/roc

# Java class path
classpath := $(CLASSPATH):$(junitJar):$(CURDIR)/$(javaBuildDir)

# Java sources
javaSrcFiles := $(shell find $(javaSrcDir) -name '*.java')
javaTestFiles := $(shell find $(javaTestDir) -name '*.java')

# Java classes
javaSrcClasses := $(subst $(javaSrcDir),$(javaBuildDir),$(javaSrcFiles:.java=.class))
javaTestClasses := $(subst $(javaTestDir),$(javaBuildDir),$(javaTestFiles:.java=.class))
javaUnitTestClasses := $(filter %Test.class,$(javaTestClasses))

# List all the phony targets (targets that are really commands, not files)
.PHONY: listconfig tests usertests alltests clean javaclean allclean


########################################
# Targets


# Make-related, meta

# List variables and values
listconfig:
	@echo Variables:
	@echo junitJar: $(junitJar)
	@echo classpath: $(classpath)
	@echo javaSrcClasses: $(javaSrcClasses)
	@echo javaTestClasses: $(javaTestClasses)
	@echo javaUnitTestClasses: $(javaUnitTestClasses)


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
clean: javaclean

javaclean:
	@rm -Rf $(javaBuildDir)

allclean: javaclean
	@find -name '*~' -delete
	@rm -Rf $(buildBaseDir)
