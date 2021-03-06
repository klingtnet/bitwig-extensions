.PHONY: clean

EXTENSION_DIR:="$$HOME/Bitwig Studio/Extensions"
JAVA_SOURCES:=$(shell find -type f -name '*.java')

all: build

build: target/klingt-net-bitwig-extensions.bwextension

target/klingt-net-bitwig-extensions.bwextension: $(JAVA_SOURCES)
	@mvn install

test:
	@mvn test

debug:
	BITWIG_DEBUG_PORT=5005 bitwig-studio

install: target/klingt-net-bitwig-extensions.bwextension
	@mkdir -p $(EXTENSION_DIR)
	install -Dm644 $? $(EXTENSION_DIR)

clean:
	@mvn clean
