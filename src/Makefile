CLASSDIR = ../class
LIBDIR = ../lib
JC = javac
JCFLAGS = -d $(CLASSDIR) -cp .:$(LIBDIR)/*
EXPORT = export
DOCKER = docker

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java

CLASSES = \
		  Client.java \
		  ClientGUI.java \
		  Messenger.java \

default: docker classes

docker:
	$(DOCKER) run -it -d --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq

classes: $(CLASSES:.java=.class)

clean: docker-clean classes-clean

docker-clean:
	$(DOCKER) stop rabbitmq

classes-clean:
	$(RM) $(CLASSDIR)/*.class
