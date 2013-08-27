#coding:utf-8
#Copyright (C) dirlt

D=veritas-deploy
JAR=veritas-1.0-SNAPSHOT-jar-with-dependencies.jar

dist:clean
	mvn compile package
	mkdir -p $D
	cp target/$(JAR) run-veritas $D
	cd $D && ln -s $(JAR) veritas.jar
	tar czvf $D.tar.gz $D

clean:
	rm -rf $D $D.tar.gz
	mvn clean
