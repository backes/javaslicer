**_JavaSlicer_** is an open-source dynamic slicing tool developed by Clemens
Hammacher at Saarland University.
It computes dynamic backward slices of Java programs by attaching to them
as a [java agent](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html).

Installation
============

JavaSlicer requires the following software on your machine:
* JDK 1.6 or 1.7

  JDK 1.8 introduces new language features which JavaSlicer is unable to handle
  (pull requests are welcome!)

        > javac -version
        javac 1.6.0_20

* [Apache Maven](http://maven.apache.org/)

        > mvn --version
        Apache Maven 2.2.0 (r788681; 2009-06-26 15:04:01+0200)
        Java version: 1.6.0_20

To compile all JavaSlicer components, do simply run the assemble script:

    > cd javaslicer
    > ./assemble.sh

This should create an assembly directory and copy runnable jars into it:

    > ls -lh assembly
    slicer.jar     traceReader.jar   tracer.jar     visualize.jar

If you plan to modify the JavaSlicer sources, you can use Maven to create the
corresponding eclipse projects:

    mvn eclipse:eclipse
After that, just import the created projects.

If you installed a Maven plugin in eclipse (e.g.
[m2e](http://eclipse.org/m2e/)), you can just import the Maven projects without
calling "mvn eclipse:eclipse" on the command line.


Limitations
===========

Currently JavaSlicer still has some limitations, which may or may not be lifted in the future. These include:

* Tracing of native methods.

 This is a drawback of using a java agent and will most probably never get fixed. Instrumenting native methods loaded from shared libraries is far more complex that instrumenting java bytecode.
 This drawback also affects many method in the Standard Library, like for example System.arraycopy and large parts of the java.util.concurrent.atomic package.

* Tracing of some Standard Library classes.

 Unfortunately, some of the integral classes of Java have to be excluded from instrumentation, hence they are not traced. This includes the classes `java.lang.String`, `java.lang.System`, `java.lang.Object` and others. The consequence is that the dependencies through method calls in these classes can not be reconstructed, leading to incomplete slices. It is theoretically possible to circumvent most of the problems arising when instrumenting these classes, so this may get fixes in the future.

* Tracing of multithreaded applications.

 At the moment, each thread is traced separately, so data dependencies between different threads can not be reconstructed. This also means that the finalize method will most probably not be contained in the trace, since (at least in some JVMs) the finalizer is executed in its own thread.


Usage
=====

This section shortly describes how to use the command-line tools of JavaSlicer.
It assumes that you have the assembled jar files as created by Maven.
If you plan to integrate JavaSlicer into your own tool or to develop another
tool on top of JavaSlicer, I recommend looking at the Java API instead of
using the existing command line tools. You should start learning the API from
the existing main-methods, mainly in the TraceResult and Slicer classes.

This description assumes that you have the following jar files:

    > ls -lh assembly
    slicer.jar     traceReader.jar   tracer.jar     visualize.jar

Use tracer.jar (containing the java agent) to create the trace of a java program run.
In order to have all debug information (like variable names and line numbers) available,
you should have compiled your program with the "-g" flag.
JavaSlicer has been developed and tested on bytecode compiled by the Sun Java Compiler in
version 1.6 (any OpenJDK compiler should be compatible). Other compilers may generate
unsupported bytecode sequences (especially for creating new objects).

You can get a list of all options for the java agent:

    > java -javaagent:assembly/tracer.jar=help

Create a trace file by just attaching the java agent (tracer.jar) to the java process.
It does not matter whether you execute a runnable jar, like this:

    > java -javaagent:assembly/tracer.jar=tracefile:test.trace -jar evaluation/dacapo-2006-10-MR2.jar -s small pmd
or any other class files from your classpath:

    > java -javaagent:... -cp bin/classes:resources my.package.ClassName
You can also trace JUnit tests (cmp. http://junit.sourceforge.net/doc/faq/faq.htm#running_4):

    > java -javaagent:... org.junit.runner.JUnitCore <test class name>


Then you can just view the complete trace, if you want so (be warned that the output can be huge!):

    > java -jar assembly/traceReader.jar test.trace

For output options of the traceReader, please see the provided help:

    > java -jar assembly/traceReader.jar -h

Or you can run the dynamic slicer on it, and you should give the JVM some more memory:

    > java -Xmx2g -jar assembly/slicer.jar -p test.trace java.util.HashMap.clear:614

The slicer gives you a summary of the options available:

    > java -jar assembly/slicer.jar

Defining The Slicing Criterion
------------------------------

It is crucial for the usave of JavaSlicer to understand how to specify the slicing criterion.
There are *three options*:
Either you slice for the execution of specific instructions (i.e. you start with control dependencies):
> java.util.HashMap.clear:614

The slice then contains all instructions on the specified line plus (transitively) those instructions which lead to the execution of any of the selected instructions.

The other option is to slice for specific data:
> java.util.HashMap.clear:614:{tab,modCount}

Then the slice contains all instructions which influenced the values of the
local variables "tab" and "modCount" in the method java.util.HashMap.clear in
line 614.
Note that the local variables don't have to occur in this line. You can even
leave out the line number, then you will slice for the last value of the variables
in that method.

In this form, the instructions on the specified line are *only* added to the slice if their produced value is used by any other instruction on that line.

The third option is to slice for the execution and all data used in a specific line.
A slicing criterion of this form looks like this:
> java.util.HashMap.clear:614:*

This slice will be the transitive closure over all control and data dependences starting from
the specified instruction(s).

You can specify several slicing criteria for one slicing run by specifying them all together,
separated by a comma:
> java.util.HashMap.clear:614:*,java.util.HashMap.put:373:{hash}


There are still problems with tracing for parameters. If you trace for a "local variable" which is a
parameter that has not been written in the method, then the slice may be empty.
This will potentially be fixed some day.

Note that in many cases, you have to quote the slicing criterion such that the shell does not try to
expand it (not only "*", but especially "{a,b,c}").

If you wish to, you can even visualize the slice using the JUNG library, but this is not very useful in most cases:

    > java -Xmx2g -jar assembly/visualize.jar -p test.trace java.util.HashMap.clear:614

License
=======

JavaSlicer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JavaSlicer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

A copy of the GNU General Public License is distributed along with
JavaSlicer. You can also see http://www.gnu.org/licenses/.

