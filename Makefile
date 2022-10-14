# Simple Makefile to build and run the project in *nix environments

# Name of the project
PROJECT = SAT

# Utilities (we explicitly compile for Java 7)
JAVAC = javac -source 1.7 -target 1.7 -bootclasspath /usr/lib/jvm/java-7-openjdk/jre/lib/rt.jar -extdirs "" #-Xlint:deprecation -Xlint:unchecked
JAVA = /usr/lib/jvm/java-7-openjdk/jre/bin/java
JAR = jar

all: run

# Run the program
run: build
	@cd bin && $(JAVA) $(PROJECT)

# Compile to JAR archive
jar: build
	@cd bin && $(JAR) -cvfm ../$(PROJECT).jar ../src/manifest.mf *

# Build the program
build:
	$(JAVAC) -sourcepath src -d bin -cp bin src/*.java

# Cleanup binaries
clean:
	rm -f bin/*.class

.PHONY: all build clean jar
