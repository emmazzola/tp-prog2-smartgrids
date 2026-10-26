# SmartGrids – TP Integrador Programación II (UNGS, 2026C2)

Sistema de monitoreo de plantas generadoras (solares, eólicas, hidráulicas y térmicas).

- `src/ar/edu/ungs/prog2/smartgrids/ISmartGrids.java`, `CodigoCliente.java` y `SmartGridsTest.java` son los archivos provistos por la cátedra (no se modifican).
- `SmartGrids` implementa `ISmartGrids`; el resto de las clases modela las plantas, sus registros de monitoreo y sus períodos de inactividad.
- `SmartGridsPropioTest.java` tiene tests propios con casos borde.

## Compilar y ejecutar

Requiere Java 17 o superior y JUnit 4 en `lib/`:

```sh
mkdir -p lib
curl -sSfo lib/junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
curl -sSfo lib/hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar

javac -d bin -cp lib/junit-4.13.2.jar $(find src -name '*.java')
java -cp bin ar.edu.ungs.prog2.smartgrids.CodigoCliente
java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore \
    ar.edu.ungs.prog2.smartgrids.SmartGridsTest ar.edu.ungs.prog2.smartgrids.SmartGridsPropioTest
```

También se puede importar `src/` en Eclipse o IntelliJ agregando JUnit 4 al proyecto.
