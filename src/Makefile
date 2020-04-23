BINDIR = ../bin
CLASSDIR = ../class
LIBDIR = ../lib
MFDIR = ../manifest
JC = javac
JAR = jar
JCFLAGS = -d $(CLASSDIR) -cp .:$(LIBDIR)/*
JARFLAGS = cmf
EXPORT = export
DOCKER = docker

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java

CLASSES = \
		  Main.java \

default: docker classes bin

docker:
	$(info Starting RabbitMQ Docker image. The server should be usable in few moments.)
	$(DOCKER) run -it -d --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq

classes: $(CLASSES:.java=.class)

bin:
	$(JAR) $(JARFLAGS) $(MFDIR)/MANIFEST.MF $(BINDIR)/Main.jar -C $(CLASSDIR)/ .

clean: docker-clean classes-clean bin-clean

docker-clean:
	$(DOCKER) stop rabbitmq

classes-clean:
	$(RM) $(CLASSDIR)/*.class

bin-clean:
	$(RM) $(BINDIR)/*.jar
