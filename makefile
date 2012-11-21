# Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
#
# Builds all the aspects of Roc until a better build situation comes
# along.


########################################
# Variables

# JUnit 4 JAR location
junitJar := $(wildcard $(junit) /usr/share/java/junit4.jar ~/opt/junit4.jar junit4.jar)
ifndef junitJar
$(error Cannot find the JUnit 4 JAR.  Add some alternative locations to the makefile or assign variable 'junit' on the command line)
else
junitJar := $(firstword $(junitJar))
endif

# Project layout
javaSrcDir := src/java
javaTestDir := test/java
javaPkgDir := mloss/roc

# Java class path
classpath := $(CLASSPATH):$(junitJar):$(CURDIR)/$(javaSrcDir):$(CURDIR)/$(javaTestDir)


# List all the phony targets (targets that are really commands, not files)
.PHONY: listconfig tests usertests alltests clean allclean


########################################
# Targets


# Make-related, meta

# List variables and values
listconfig:
	@echo Variables:
	@echo junitJar: $(junitJar)
	@echo classpath: $(classpath)


# Java

# General Java compilation
$(javaSrcDir)/%.class: $(javaSrcDir)/%.java
	cd $(javaSrcDir) && javac -cp $(classpath) -source 5 -Xlint $*.java
$(javaTestDir)/%.class: $(javaTestDir)/%.java
	cd $(javaTestDir) && javac -cp $(classpath) -source 5 -Xlint $*.java

# List Java dependencies here
$(javaTestDir)/$(javaPkgDir)/CurveDataTest.class: $(javaSrcDir)/$(javaPkgDir)/CurveData.class
$(javaTestDir)/$(javaPkgDir)/CurveDataBuilderTest.class: $(javaSrcDir)/$(javaPkgDir)/CurveData.class
$(javaTestDir)/$(javaPkgDir)/CurveDataPrimitivesBuilderTest.class: $(javaSrcDir)/$(javaPkgDir)/CurveData.class
$(javaTestDir)/$(javaPkgDir)/UserScenarios.class: $(javaSrcDir)/$(javaPkgDir)/CurveData.class


# JUnit

# Run unit tests
tests: $(javaTestDir)/$(javaPkgDir)/CurveDataTest.class $(javaTestDir)/$(javaPkgDir)/CurveDataBuilderTest.class $(javaTestDir)/$(javaPkgDir)/CurveDataPrimitivesBuilderTest.class
	@cd $(javaTestDir) && java -cp $(classpath) org.junit.runner.JUnitCore mloss.roc.CurveDataTest mloss.roc.CurveDataBuilderTest mloss.roc.CurveDataPrimitivesBuilderTest

# Run acceptance tests
usertests: $(javaTestDir)/$(javaPkgDir)/UserScenarios.class
	@cd $(javaTestDir) && java -cp $(classpath) org.junit.runner.JUnitCore mloss.roc.UserScenarios

# Run all tests
alltests: $(javaTestDir)/$(javaPkgDir)/CurveDataTest.class $(javaTestDir)/$(javaPkgDir)/CurveDataBuilderTest.class $(javaTestDir)/$(javaPkgDir)/CurveDataPrimitivesBuilderTest.class $(javaTestDir)/$(javaPkgDir)/UserScenarios.class
	@cd $(javaTestDir) && java -cp $(classpath) org.junit.runner.JUnitCore mloss.roc.CurveDataTest mloss.roc.CurveDataBuilderTest mloss.roc.CurveDataPrimitivesBuilderTest mloss.roc.UserScenarios


# Packages



# Cleanup

# Remove all derived files
clean:
	@find -name '*.class' -delete

allclean: clean
	@find -name '*~' -delete
