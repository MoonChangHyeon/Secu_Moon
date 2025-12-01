This sample shows how to specify a JDK version that is not included in the OpenText SAST (Fortify) installation (<sca_install_dir>/Core/bootcp/). See the OpenText Static Application Security Testing User Guide for supported versions.

Use the command-line option "-custom-jdk-dir" to specify a directory that contains a JDK.
The equivalent property name is "com.fortify.sca.CustomJdkDir".

Sample.java uses Switch expressions with pattern matching.

Run OpenText SAST (Fortify) to scan the code:

$ sourceanalyzer -b sample -clean
$ sourceanalyzer -b sample -custom-jdk-dir <path_to_jdk-14> Sample.java
$ sourceanalyzer -b sample -scan -f Sample.fpr

Open the results in Audit Workbench:

$ auditworkbench Sample.fpr

The analysis results should contain vulnerabilities in the following categories:

      Privacy Violation

The analysis results might include other issues depending on the version of the Rulepacks used in the scan.

In this sample, the Privacy Violation vulnerability indicates that sensitive data such as a password is written to the console, which compromises user privacy.