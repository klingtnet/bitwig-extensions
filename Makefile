.PHONY: clean

EXTENSION_DIR:="$$HOME/Bitwig Studio/Extensions"
JAVA_SOURCES:=$(shell find -type f -name '*.java')

all: build

build: akai-midimix/target/AKAIMidimix.bwextension akai-mpk-mini-mk2/target/AkaiMPKMiniMK2.bwextension edirol-pcr/target/EdirolPCR.bwextension

akai-midimix/target/AKAIMidimix.bwextension: $(JAVA_SOURCES)
akai-mpk-mini-mk2/target/AkaiMPKMiniMK2.bwextension: $(JAVA_SOURCES)
edirol-pcr/target/EdirolPCR.bwextension: $(JAVA_SOURCES)
	@mvn install

test:
	@mvn test

debug:
	BITWIG_DEBUG_PORT=5005 bitwig-studio

install: akai-midimix/target/AKAIMidimix.bwextension akai-mpk-mini-mk2/target/AkaiMPKMiniMK2.bwextension edirol-pcr/target/EdirolPCR.bwextension
	@mkdir -p $(EXTENSION_DIR)
	install -Dm644 $? $(EXTENSION_DIR)

clean:
	@mvn clean
