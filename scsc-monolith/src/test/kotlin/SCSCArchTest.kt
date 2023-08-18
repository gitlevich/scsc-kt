import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Test

@AnalyzeClasses(packages = ["demo.scsc"])
class SCSCArchTest {
    private val commandSideEncapsulation: ArchRule = ArchRuleDefinition.classes()
        .that().resideInAPackage("demo.scsc.commandside..")
        .should().onlyBeAccessed().byAnyPackage("demo.scsc.commandside..", "demo.scsc")
    private val querySideEncapsulation: ArchRule = ArchRuleDefinition.classes()
        .that().resideInAPackage("demo.scsc.queryside..")
        .should().onlyBeAccessed().byAnyPackage("demo.scsc.queryside..", "demo.scsc")

    @Test
    fun cqrsTest() {
        val importedClasses = ClassFileImporter().importPackages("demo.scsc")
        commandSideEncapsulation.check(importedClasses)
        querySideEncapsulation.check(importedClasses)
    }
}
