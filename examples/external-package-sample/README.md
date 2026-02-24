# External Package Sample (Custom EDAF Plugin)

This sample shows how to implement and run a custom `ProblemPlugin` from an external Maven project.

## 1) Build EDAF artifacts locally

From repository root:

```bash
cd /Users/karloknezevic/Desktop/EDAF
mvn -q -pl edaf-cli,edaf-web -am package -DskipTests
```

## 2) Build external plugin jar

```bash
cd /Users/karloknezevic/Desktop/EDAF/examples/external-package-sample
mvn -q package
```

## 3) Run EDAF with external plugin on classpath

Use `-cp` (not `-jar`) so ServiceLoader can discover plugin classes from both jars:

```bash
cd /Users/karloknezevic/Desktop/EDAF
java -cp "examples/external-package-sample/target/edaf-external-package-sample-1.0.0-SNAPSHOT.jar:edaf-cli/target/edaf-cli.jar" \
  com.knezevic.edaf.v3.cli.EdafCli run -c examples/external-package-sample/sample-leading-ones.yml --verbosity normal
```

## 4) Monitor run in web UI

```bash
cd /Users/karloknezevic/Desktop/EDAF
EDAF_DB_URL="jdbc:sqlite:$(pwd)/examples/external-package-sample/external-edaf.db" \
  java -jar edaf-web/target/edaf-web-3.0.0.jar
```

Open [http://localhost:7070](http://localhost:7070) and inspect run `external-leading-ones-demo`.
