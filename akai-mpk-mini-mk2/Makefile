.PHONY: clean

SCRIPT_DIR:="$$HOME/Bitwig Studio/Extensions"
JAVA_SOURCES:=$(shell find -type f -name '*.java')

all: build

build: test target/AkaiMPKMiniMK2.bwextension

target/AkaiMPKMiniMK2.bwextension: $(JAVA_SOURCES)
	mvn install

test:
	mvn test

debug:
	BITWIG_DEBUG_PORT=5005 bitwig-studio

install: target/AkaiMPKMiniMK2.bwextension
	mkdir -p $(SCRIPT_DIR)
	install -Dm644 $< $(SCRIPT_DIR)

clean:
	rm -rf target
