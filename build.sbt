name := "Projektverwaltung"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  javaJpa,
  "mysql" % "mysql-connector-java" % "5.1.29",
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final"
)



play.Project.playJavaSettings
