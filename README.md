RecommendSys framework for eachcloud.

1.cd to core
mvn -DskipTests install

2.mvn install:install-file -Dfile=sqljdbc_4.0/enu/sqljdbc4.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=3.0 -Dpackaging=jar


3.cd to examples
mvn compile
