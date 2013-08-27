#coding:utf-8
#Copyright (C) dirlt

D=fast-hbase-rest-deploy
JAR=FastHBaseRest-1.0-SNAPSHOT-jar-with-dependencies.jar

dist:clean
	mvn compile package
	mkdir -p $D
	cp target/$(JAR) run-fast-hbase-rest $D
	cd $D && ln -s $(JAR) FastHBaseRest.jar
	tar czvf $D.tar.gz $D

proto:
	protoc --python_out=./ src/main/proto/message.proto

clean:
	rm -rf $D $D.tar.gz
	mvn clean && rm -rf src/main/proto/*.pyc
