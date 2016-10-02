All: compile server

server:
	java  -classpath class/ jvn.JvnCoordImpl

client:
	java -Djava.security.policy=security.policy -classpath class/ irc.Irc

compile:
	javac -d class/ -classpath class/ -sourcepath . jvn/*.java
	javac -d class/ -classpath class/ -sourcepath . irc/*.java
	
clean:
	rm -rf *.class
	


	
