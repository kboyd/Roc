# Copyright (c) 2014 Roc Project.  This is free software.  See
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
# help: Display this list of targets.
#
# tests: Compile, run core JUnit tests.
#
# usertests: Compile, run JUnit user scenarios (functionality/acceptance
#   tests).
#
# alltests: Combines all above tests.
#
# javadoc: Generate the Java API documentation for this software.
#
# release-javadoc: Generate the public Java API documentation for this
#   software.
#
# jar: Create a Java archive (package) for this software.
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
#     make build/java/mloss/roc/Curve.class
#
########################################

# List all the phony targets (targets that are really commands, not files)
.PHONY: help tests usertests alltests javadoc release-javadoc jar clean-java clean-javadoc clean allclean listconfig selftest


########################################
# Variables

# Variables for string substitution
emptyString :=
space := $(emptyString) $(emptyString)
indent := $(emptyString)    $(emptyString)
define newline


endef
makefileName := $(firstword $(MAKEFILE_LIST))

# Target Java version
javaVersion := 7
# Java compiler version
javacVersion := $(word 2, $(subst ., , $(word 2, $(shell javac -version 2>&1))))

# Location of the Java runtime JAR for the target Java version.  Needed
# for cross-compiling.
rtJarLocations := /usr/lib/jvm/jre-1.$(javaVersion).0/lib/rt.jar
rtJar := $(wildcard $(rtjar) $(rtJarLocations))

# If the target Java version and the Java compiler version are
# different, check for the runtime JAR for the target version and set
# the bootclasspath option to enable proper cross-compilation.  Set
# empty bootclasspath option when not cross-compiling.
crossCompileOpts :=
ifneq ($(javaVersion),$(javacVersion))
ifndef rtJar
$(error Error: The target Java version is $(javaVersion) but cannot find the Java $(javaVersion) runtime JAR.  (The compiler is version $(javacVersion).)  Add some alternative locations to the makefile, assign variable 'rtjar' on the command line, or set 'javaVersion' to the intended version.$(newline)$(indent)Searched: $(rtJarLocations))
else
rtJar := $(firstword $(rtJar))
# Set the bootclasspath option to the discovered runtime JAR to enable
# proper cross-compilation
crossCompileOpts := -bootclasspath $(rtJar)
endif
endif

# Java compiler options (e.g. -source 7 -target 7)
javacOpts := -source $(javaVersion) -target $(javaVersion) -Xlint

# Project layout
buildBaseDir := build
javaSrcDir := java/src
javaTestDir := java/test
javaDocDir := java/doc
javaBuildDir := $(buildBaseDir)/java
javaPkgDir := mloss/roc

# Locations for the JUnit and Hamcrest JARs required for testing.  Note
# that some versions of JUnit 4 include some of the core Hamcrest
# classes.  The /usr/share/java paths exist on Red Hat and Fedora.
# Presumably they also exist on other distributions.
junitLocations := junit4.jar ~/opt/junit4.jar /usr/share/java/junit4.jar
junitJars := $(wildcard $(junit) $(junitLocations))
hamcrestLocations := hamcrest.jar ~/opt/hamcrest.jar /usr/share/java/hamcrest/core.jar
hamcrestJars := $(wildcard $(hamcrest) $(hamcrestLocations))

# Java class paths (one regular, one for testing).  Use 'strip' to
# remove extra whitespace and avoid empty classpath entries
classpath := $(subst $(space),:,$(strip $(CLASSPATH) $(CURDIR)/$(javaBuildDir)))
testClasspath := $(subst $(space),:,$(strip $(classpath) $(junitJars) $(hamcrestJars)))

# Java sources
javaSrcFiles := $(shell find $(javaSrcDir) -name '*.java' -not -name 'package-info.java' | sort)
javaTestFiles := $(shell find $(javaTestDir) -name '*.java' | sort)
javaDocFiles := $(shell find $(javaSrcDir) -name 'package-info.java' | sort)
javaReleaseSrcFiles := $(subst $(javaSrcDir),$(javaBuildDir),$(javaSrcFiles))

# Java classes
javaSrcClasses := $(subst $(javaSrcDir),$(javaBuildDir),$(javaSrcFiles:.java=.class))
javaTestClasses := $(subst $(javaTestDir),$(javaBuildDir),$(javaTestFiles:.java=.class))
# Limit 'javaTestClasses' to actual unit tests (omit helper classes).
javaUnitTestClasses := $(filter %Test.class,$(javaTestClasses))

# Project version (anything in the README after the identifying phrase
# that consists of digits and periods with digits on the ends)
version := $(shell grep 'Roc is version' README.md | sed -e 's/.*Roc is version \([0-9][0-9.]*[0-9]\).*/\1/')


########################################
# General targets


# Print documentation
help:
	@sed -n '/^# Documentation/,/^#####/p' $(makefileName) | cut -c 3- | head -n -1

# List variables and values
listconfig:
	@echo Variables:
	@echo version: $(version)
	@echo classpath: $(classpath)
	@echo testClasspath: $(testClasspath)
	@echo junitJars: $(junitJars)
	@echo hamcrestJars: $(hamcrestJars)
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

# Java sources compilation
$(javaBuildDir)/%.class: $(javaBuildDir)/.exists $(javaSrcDir)/%.java
	javac -cp $(classpath) $(crossCompileOpts) -d $(javaBuildDir) $(javacOpts) $(javaSrcDir)/$*.java
# Java tests compilation.  Depends on JUnit and Hamcrest.
$(javaBuildDir)/%.class: $(javaBuildDir)/.junitClassesExist $(javaBuildDir)/.hamcrestClassesExist $(javaTestDir)/%.java
	javac -cp $(testClasspath) $(crossCompileOpts) -d $(javaBuildDir) $(javacOpts) $(javaTestDir)/$*.java

# List Java dependencies here
$(javaBuildDir)/$(javaPkgDir)/Curve.class:
$(javaBuildDir)/$(javaPkgDir)/util/ArrayIterator.class:
$(javaBuildDir)/$(javaPkgDir)/util/Arrays.class:
$(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class: $(javaBuildDir)/$(javaPkgDir)/util/ArrayIterator.class
$(javaBuildDir)/$(javaPkgDir)/CurveTest.class: $(javaBuildDir)/$(javaPkgDir)/Curve.class
$(javaBuildDir)/$(javaPkgDir)/CurveBuilderTest.class: $(javaBuildDir)/$(javaPkgDir)/Curve.class $(javaBuildDir)/$(javaPkgDir)/util/Assert.class $(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class
$(javaBuildDir)/$(javaPkgDir)/CurvePrimitivesBuilderTest.class: $(javaBuildDir)/$(javaPkgDir)/Curve.class $(javaBuildDir)/$(javaPkgDir)/util/Assert.class $(javaBuildDir)/$(javaPkgDir)/util/Arrays.class $(javaBuildDir)/$(javaPkgDir)/util/IterableArray.class
$(javaBuildDir)/$(javaPkgDir)/UserScenarios.class: $(javaBuildDir)/$(javaPkgDir)/Curve.class $(javaBuildDir)/$(javaPkgDir)/CurveTest.class
$(javaBuildDir)/$(javaPkgDir)/util/Assert.class:

# Test dependencies

# Check for JUnit 4 JARs (>= 4.6) and the required JUnit classes
junitClasses := org.junit.Assert org.junit.Test
junitClassesFiles := $(addsuffix .class,$(subst .,/,$(junitClasses)))
$(javaBuildDir)/.junitClassesExist: $(javaBuildDir)/.exists
	@[[ -n "$(junitJars)" ]] && true || { echo -e "make: *** Error: Cannot find the JUnit 4 JAR.  Assign variable 'junit' on the command line or add some alternative locations to the makefile.\n$(indent)Searched locations: $(junitLocations)"; exit 1; }
	@junitVersion=( $$(java -cp $(junitJars) junit.runner.Version) ); [[ $${junitVersion%*.*} -ge 4 && $${junitVersion#*.*} -ge 6 ]] && true || { echo -e "make: *** Error: The JUnit version is too old.  Expected >= 4.6 but found $$junitVersion.\n$(indent)Searched JARs: $(junitJars)"; exit 1; }
	@{ for jar in $(junitJars); do jar tf $$jar; done; } | sort | uniq > $(javaBuildDir)/.junitJarsContents
	@[[ "$$(grep -c $(foreach pattern,$(junitClassesFiles),-e $(pattern)) $(javaBuildDir)/.junitJarsContents)" -eq "$(words $(junitClassesFiles))" ]] && touch $@ || { echo -e "make: *** Error: The JUnit 4 JAR(s) do not contain the required classes.\n$(indent)Searched JARs: $(junitJars)"; exit 1; }

# Check for Hamcrest JARs and the required Hamcrest classes (which may
# be contained in the JUnit JARs in some versions)
hamcrestClasses := org.hamcrest.CoreMatchers org.hamcrest.Matcher
hamcrestClassesFiles := $(addsuffix .class,$(subst .,/,$(hamcrestClasses)))
$(javaBuildDir)/.hamcrestClassesExist: $(javaBuildDir)/.exists
	@[[ -n "$(wildcard $(hamcrestJars) $(junitJars))" ]] && true || { echo -e "make: *** Error: Cannot find any Hamcrest JARs.  Assign variable 'hamcrest' on the command line or add some alternative locations to the makefile.\n$(indent)Searched locations: $(hamcrestLocations) $(junitLocations)"; exit 1; }
	@{ for jar in $(hamcrestJars) $(junitJars); do jar tf $$jar; done; } | sort | uniq > $(javaBuildDir)/.hamcrestJarsContents
	@[[ "$$(grep -c $(foreach pattern,$(hamcrestClassesFiles),-e $(pattern)) $(javaBuildDir)/.hamcrestJarsContents)" -eq "$(words $(hamcrestClassesFiles))" ]] && touch $@ || { echo -e "make: *** Error: The Hamcrest JAR(s) do not contain the required classes.\n$(indent)Searched JARs: $(junitJars) $(hamcrestJars)"; exit 1; }

#####
# JUnit

# Run unit tests
tests: $(javaUnitTestClasses)
	java -cp $(testClasspath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaBuildDir)/,,$(javaUnitTestClasses:.class=)))

# Run acceptance tests
usertests: $(javaBuildDir)/$(javaPkgDir)/UserScenarios.class
	java -cp $(testClasspath) org.junit.runner.JUnitCore mloss.roc.UserScenarios

# Run all tests
alltests: $(javaUnitTestClasses) $(javaBuildDir)/$(javaPkgDir)/UserScenarios.class
	java -cp $(testClasspath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaBuildDir)/,,$(^:.class=)))

#####
# Javadoc for internal reading

# Java documentation for the full API (including private) but not the tests
javadoc: $(javaDocDir)/index.html

$(javaSrcDir)/doc-files/LICENSE.txt: LICENSE.txt
	mkdir -p $(javaSrcDir)/doc-files
	cp $< $@

# Javadoc level private
$(javaDocDir)/index.html: $(javaSrcDir)/overview.html $(javaSrcDir)/javadocOptions.txt $(javaSrcFiles) $(javaDocFiles) $(javaSrcDir)/doc-files/LICENSE.txt $(javaSrcDir)/overview-summary.html.patch
	javadoc -d $(javaDocDir) -sourcepath $(javaSrcDir) -private @$(javaSrcDir)/javadocOptions.txt -overview $< $(javaSrcFiles)
# Work around javadoc bug where content is put in div with footer class (but only if present)
	grep -q 'class="footer".*name="overview_description"' $(javaDocDir)/overview-summary.html && patch --forward --input $(javaSrcDir)/overview-summary.html.patch $(javaDocDir)/overview-summary.html || true

#####
# Javadoc for release.  Same as 'javadoc' above but level public.

release-javadoc: $(javaBuildDir)/doc/index.html

$(javaBuildDir)/doc/index.html: $(javaSrcDir)/overview.html $(javaSrcDir)/javadocOptions.txt $(javaSrcFiles) $(javaDocFiles) $(javaSrcDir)/doc-files/LICENSE.txt $(javaSrcDir)/overview-summary.html.patch
	javadoc -d $(javaBuildDir)/doc -sourcepath $(javaSrcDir) @$(javaSrcDir)/javadocOptions.txt -overview $< $(javaSrcFiles)
# Work around javadoc bug where content is put in div with footer class
# (but only do if bug present)
	grep -q 'class="footer".*name="overview_description"' $(javaBuildDir)/doc/overview-summary.html && patch --forward --input $(javaSrcDir)/overview-summary.html.patch $(javaBuildDir)/doc/overview-summary.html || true

#####
# JAR package of library source, bytecode, and docs; a distribution for use, not for development

# Redirect to a JAR for the current version
jar: $(javaBuildDir)/roc-$(version).jar

# Copy files from src to build for packaging
$(javaBuildDir)/%.java: $(javaSrcDir)/%.java
	mkdir -p $(@D) # Make sure the destination directory exists
	cp $< $@

# Build a JAR for the current version
$(javaBuildDir)/roc-$(version).jar: README.md LICENSE.txt $(javaReleaseSrcFiles) $(javaSrcClasses) release-javadoc
	jar cf $@ README.md LICENSE.txt -C $(javaBuildDir) mloss -C $(javaBuildDir) doc


########################################
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


########################################
# Test the makefile to make sure each target runs (not necessarily
# correctly)

selfTestCommands := $(shell grep '^.PHONY:' $(makefileName) | sed -e 's/.PHONY://' -e 's/selftest//')
selftest:
	@for command in $(javaSrcClasses) $(javaTestClasses) $(selfTestCommands); do \
	    echo -n "Testing 'make $$command' ... "; \
	    output=$$( make allclean 2>&1 && make $$command 2>&1 ); \
	    if [[ $$? -ne 0 ]]; then \
	        echo -e "\n----------\n$$output\n----------\nFAIL: $$command" ; exit 1; \
	    else \
	        echo OK; \
	    fi \
	done
