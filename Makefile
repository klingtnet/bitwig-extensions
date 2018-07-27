
.PHONY: clean

SCRIPT_DIR:="$$HOME/Bitwig Studio/Extensions"
JAVA_SOURCES:=$(wilcard src/**/*.java)

all: build

build: test $(JAVA_SOURCES) target/AkaiMPKMiniMK2.bwextension

target/AkaiMPKMiniMK2.bwextension:
	mvn install

test:
	mvn test

debug:
	BITWIG_DEBUG_PORT=5005 bitwig-studio

install: build
	mkdir -p $(SCRIPT_DIR)
	install -Dm644 target/AkaiMPKMiniMK2.bwextension $(SCRIPT_DIR)

clean:
	rm -rf target
