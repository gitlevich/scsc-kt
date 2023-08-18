import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "demo.scsc")
public class SCSCArchTest {

    final ArchRule commandSideEncapsulation = classes()
            .that().resideInAPackage("demo.scsc.commandside..")
            .should().onlyBeAccessed().byAnyPackage("demo.scsc.commandside..", "demo.scsc");

    final ArchRule querySideEncapsulation = classes()
            .that().resideInAPackage("demo.scsc.queryside..")
            .should().onlyBeAccessed().byAnyPackage("demo.scsc.queryside..", "demo.scsc");

    @Test
    public void cqrsTest() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("demo.scsc");
        commandSideEncapsulation.check(importedClasses);
        querySideEncapsulation.check(importedClasses);
    }


}
