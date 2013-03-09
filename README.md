RecommendSys framework for eachcloud.

1.cd to core
mvn -DskipTests install

2.mvn install:install-file -Dfile=sqljdbc_4.0/enu/sqljdbc4.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0 -Dpackaging=jar

<dependency>
  <groupId>com.microsoft.sqlserver</groupId>
  <artifactId>sqljdbc4</artifactId>
  <version>4.0</version>
</dependency>


3.cd to examples
mvn compile

4.then
mvn -q exec:java -Dexec.mainClass="org.apache.mahout.cf.taste.vjianke.IntrestBasedRecommendEntryPoint"

git rm -r --cached .
git add .
git commit -m "Start using .gitignore"

mvn  exec:java -Dexec.mainClass="org.apache.mahout.cf.taste.vjianke.engine.TikaIndexer"

mvn  exec:java -Dexec.mainClass="org.apache.mahout.cf.taste.vjianke.engine.TermVectorBasedSimilarity"
