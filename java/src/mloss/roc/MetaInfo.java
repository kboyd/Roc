/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;

/**
 * Version number and other meta-information accessible programmatically
 * and to outside tools.  See {@link http://semver.org/}.
 */
public class MetaInfo {
    // If you update the version, also update it in the README.
    // (Everything except the README can automatically get the version
    // number from here but the README is not dynamically generated.)
    public static final int VERSION_NUMBER_MAJOR = 0;
    public static final int VERSION_NUMBER_MINOR = 1;
    public static final int VERSION_NUMBER_PATCH = 0;
    // The version string could be generated from the above numbers but
    // having it be literal makes it accessible to external tools via
    // grep or similar.  (See the makefile for an example.)
    public static final String VERSION_STRING = "0.1.0";

    public static final String NAME = "Roc";
    public static final String PROJECT_NAME = NAME + " Project";
    public static final String TAGLINE = "Everything ROC and PR curves";
    public static final int COPYRIGHT_YEAR = 2014;
    public static final String COPYRIGHT_NOTICE =
        "Copyright (c) " + COPYRIGHT_YEAR + " " + PROJECT_NAME;
    public static final String LICENSE_NAME = "BSD 2-clause (FreeBSD)";
    public static final String URL = "https://github.com/kboyd/Roc";
}
