

mvn install:install-file \
	-DgroupId=it.seralf.ir \
	-DartifactId=simples \
	-Dversion=0.0.1-SNAPSHOT \
	-Dpackaging=jar \
	-Dfile=target/simples-0.0.1-SNAPSHOT.jar \
	-DpomFile=pom.xml



mvn install:install-file -DgroupId=it.seralf.ir -DartifactId=simples -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=./target/simples-0.0.1-SNAPSHOT.jar