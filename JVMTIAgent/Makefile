UNAME := $(shell uname)
CCFLAGS = -o slicer.o -I${JAVA_HOME}/include -c -fPIC -fpermissive
ifeq ($(UNAME), Linux)
	CCFLAGS += -I${JAVA_HOME}/include/linux 
	LINKFLAGS = -z defs -static-libgcc -shared -o libslicer.so -lc 
endif
ifeq ($(UNAME), Darwin)
	CCFLAGS += -I${JAVA_HOME}/include/darwin
	LINKFLAGS += -dynamiclib -o libslicer.dylib
endif

tracker.dylib:
	gcc ${CCFLAGS} slicer.cpp
	g++ ${LINKFLAGS} slicer.o
clean:
	rm -rf slicer.o libslicer.dylib libslicer.so
