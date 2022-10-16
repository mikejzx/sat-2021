# Simple Makefile to build and run the project in *nix environments

# Name of the project
PROJECT = SAT

# Utilities (we explicitly compile for Java 7)
JAVAC = javac
JAVA = java
JAR = jar

all: dirs build

# Creates required directories
dirs: ./bin

./bin:
	mkdir bin

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
