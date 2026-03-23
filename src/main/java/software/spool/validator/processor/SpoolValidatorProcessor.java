package software.spool.validator.processor;

import software.spool.validator.api.Validator;
import software.spool.validator.api.Validate;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("software.spool.validator.api.SpoolValidator")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SpoolValidatorProcessor extends AbstractProcessor {

    private static final String SERVICE_FILE =
        "META-INF/services/" + Validator.class.getName();

    private final Set<String> discovered = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Validate.class)) {
            discovered.add(((TypeElement) element).getQualifiedName().toString());
        }

        if (roundEnv.processingOver() && !discovered.isEmpty()) {
            writeServiceFile();
        }
        return true;
    }

    private void writeServiceFile() {
        try {
            FileObject file = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_FILE);
            try (Writer writer = file.openWriter()) {
                for (String className : discovered) {
                    writer.write(className);
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to write service file: " + e.getMessage()
            );
        }
    }
}
