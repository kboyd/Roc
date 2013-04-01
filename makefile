# Copyright (c) 2013 Roc Project.  This is free software.  See
# LICENSE.txt for details.
#
# Builds the website

# This is super quick and dirty for now

releaseDescFiles := $(shell find releases -name 'desc.md' | sort -r)

.PHONY: all

all: index.md releases/index.md releases/0.1.0/roc-0.1.0.jar

# Main page
index.md: index.yaml ../Roc/README.md
	cat $^ > $@

# Releases page
releases/index.md: releases/releases.yaml releases/releases.md $(releaseDescFiles) copyright.md
	cat $^ > $@

releases/0.1.0/roc-0.1.0.jar: ../Roc/build/java/roc-0.1.0.jar
	cp -a $< $@
