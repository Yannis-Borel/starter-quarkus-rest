package org.acme.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test du processeur : on lance le compilateur Java depuis le test
 * ({@code ToolProvider.getSystemJavaCompiler()}) sur un petit fichier source,
 * avec notre processeur activé ({@code -processor}).
 */
@DisplayName("DomainModelProcessor")
class DomainModelProcessorTest {

    @TempDir
    Path dir;

    @Test
    @DisplayName("@DomainModel sur une classe qui n'est pas un record : erreur de compilation")
    void nonRecordIsRejected() throws IOException {
        Path source = dir.resolve("Bad.java");
        Files.writeString(source, """
                package demo;

                @org.acme.catalog.product.domain.DomainModel
                public class Bad {
                }
                """);
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        int result = compile(source, errors);

        assertNotEquals(0, result);
        assertTrue(errors.toString().contains("@DomainModel can only be used on a record"));
    }

    @Test
    @DisplayName("@DomainModel sur un record : compilation réussie et classe Fields générée")
    void recordGeneratesFieldsClass() throws IOException {
        Path source = dir.resolve("Good.java");
        Files.writeString(source, """
                package demo;

                @org.acme.catalog.product.domain.DomainModel
                public record Good(String name) {
                }
                """);

        int result = compile(source, new ByteArrayOutputStream());

        assertEquals(0, result);
        assertTrue(Files.exists(dir.resolve("demo/GoodFields.java")));
    }

    /**
     * Compile {@code source} avec le processeur ; {@code target/classes}
     * contient l'annotation et le processeur déjà compilés.
     */
    private int compile(Path source, ByteArrayOutputStream errors) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, errors,
                "-cp", "target/classes",
                "-processor", DomainModelProcessor.class.getName(),
                "-d", dir.toString(),
                "-s", dir.toString(),
                source.toString());
    }
}
