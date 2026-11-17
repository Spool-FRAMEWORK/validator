package software.spool.validator.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.spool.validator.api.Validator;
import software.spool.validator.engine.ValidatorRegistry;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpoolValidatorProcessorTest {

    private static final String NESTED_VALIDATOR_SOURCE = """
            package example;

            import software.spool.validator.api.Validate;
            import software.spool.validator.api.Validator;

            public class Outer {
                @Validate(value = String.class, sourceId = "nested-src")
                public static class Inner implements Validator<String> {
                    @Override
                    public boolean validate(String value) {
                        return true;
                    }
                }
            }
            """;

    @TempDir
    Path workDirectory;

    @Test
    void process_nestedValidator_registersItWithItsBinaryName() throws IOException {
        Path classes = compileWithProcessor(NESTED_VALIDATOR_SOURCE);

        assertThat(Files.readAllLines(classes.resolve("META-INF/services/" + Validator.class.getName())))
                .containsExactly("example.Outer$Inner");
    }

    @Test
    void process_nestedValidator_letsTheDefaultRegistryLoadIt() throws IOException {
        Path classes = compileWithProcessor(NESTED_VALIDATOR_SOURCE);

        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader compiled = new URLClassLoader(new URL[]{classes.toUri().toURL()}, getClass().getClassLoader())) {
            Thread.currentThread().setContextClassLoader(compiled);

            ValidatorRegistry registry = new ValidatorRegistry();

            assertThat(registry.hasValidatorsFor("nested-src", String.class)).isTrue();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private Path compileWithProcessor(String source) throws IOException {
        Path sourceFile = workDirectory.resolve("src/example/Outer.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, source);
        Path classes = Files.createDirectories(workDirectory.resolve("classes"));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(sourceFile);
            List<String> options = List.of(
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", classes.toString());
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, units);
            task.setProcessors(List.of(new SpoolValidatorProcessor()));
            assertThat(task.call()).as("compilation of the nested validator").isTrue();
        }
        return classes;
    }
}
