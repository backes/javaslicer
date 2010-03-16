This is some way to start with the slicer:

Go to the javaslicer directory and run the assemble.sh script
> cd javaslicer
> ./assemble.sh

This should create an assembly directory and copy runnable jars into it:
> ls -lh assembly
slicer.jar     traceReader.jar   tracer.jar     visualize.jar

Use tracer.jar (containing a java agent) to create traces of a java program run.
You can get a list of all options for the java agent:
> java -javaagent:assembly/tracer.jar=help

Create a trace file:
> java -javaagent:assembly/tracer.jar=tracefile:test.trace -jar evaluation/dacapo-2006-10-MR2.jar -s small pmd

Then you can just view the complete trace, if you want so:
> java -jar assembly/traceReader.jar test.trace

Or you can run the dynamic slicer on it, and you should give the JVM some more memory:
> java -Xmx2g -jar assembly/slicer.jar -p test.trace java.util.HashMap.clear:614

The slicer give you a summary of the options available:
> java -jar assembly/slicer.jar

The crucial part here is to define the slicing criterion. There are two options:
Either you slice for the execution of specific instructions (i.e. you start with control dependencies):
    java.util.HashMap.clear:614
The slice should contain all instructions which lead to the execution of this specific line.

The other option is to slice for specific data:
    java.util.HashMap.clear:614:{tab,modCount}
Then the slice contains all instructions which influenced the values of the
local variables "tab" and "modCount" in the method java.util.HashMap.clear in
line 614.
Note that the local variables don't have to occure in this line. You can even
leave out the line number, but if you specify it, then the slicer slices for
the values that the variables have in this line.
If you want to slice for all local variables, then specify "*", i.e.
    java.util.HashMap.clear:614:*

Note that in many cases, you have to quote the slicing criterion such that the shell does not try to expand it.

If you wish to, you can even visualize the slice using the JUNG library, but this is not very useful in most cases:
> java -Xmx2g -jar assembly/visualize.jar -p test.trace java.util.HashMap.clear:614

