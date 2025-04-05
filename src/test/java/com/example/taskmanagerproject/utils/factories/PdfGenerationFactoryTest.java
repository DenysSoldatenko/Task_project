package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.TEMPLATE_LOAD_ERROR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfGenerationFactoryTest {

  @Test
  void loadTemplate_shouldReturnHtmlContent_givenValidFile(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("template.html").toFile();
    String expectedHtml = "<html><body><h1>Hello</h1></body></html>";

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(expectedHtml);
    }

    String actual = PdfGenerationFactory.loadTemplate(tempFile.getAbsolutePath());
    assertEquals(expectedHtml, actual);
  }

  @Test
  void loadTemplate_shouldThrowException_givenNonExistentFile() {
    String invalidPath = "non_existent_template.html";
    PdfGenerationException exception = assertThrows(PdfGenerationException.class, () -> PdfGenerationFactory.loadTemplate(invalidPath));

    assertTrue(exception.getMessage().contains(TEMPLATE_LOAD_ERROR + invalidPath));
  }

  @Test
  void generatePdfFromHtml_shouldReturnNonEmptyByteArray_givenValidHtml() {
    String html = "<html><body><h1>PDF Test</h1></body></html>";
    byte[] pdfBytes = PdfGenerationFactory.generatePdfFromHtml(html);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0, "Generated PDF content should not be empty");
  }

  @Test
  void generatePdfFromHtml_shouldHandleMalformedHtmlGracefully() {
    // Jsoup is designed to be highly tolerant of malformed HTML, automatically fixing broken structures.
    // This test ensures that even incomplete HTML input (e.g., missing closing tags) does not cause PDF generation to fail.
    // This reflects a real-world scenario where user-generated HTML might be imperfect.
    String malformedHtml = "<html><body><h1>Unclosed tag";

    byte[] pdfBytes = PdfGenerationFactory.generatePdfFromHtml(malformedHtml);

    assertNotNull(pdfBytes, "PDF generation should succeed with malformed but parsable HTML");
    assertTrue(pdfBytes.length > 0, "Generated PDF should not be empty");
  }

  @Test
  void generatePdfFromHtml_shouldNotThrow_whenRendererHandlesWellFormedHtml() {
    // ITextRenderer is resilient and typically handles basic HTML without throwing exceptions.
    // This test validates that the method operates safely under expected conditions,
    // without mocking internal rendering, which would require architectural changes (e.g., dependency injection).
    // Purpose: Ensure robustness and integration sanity for well-formed inputs.
    assertDoesNotThrow(() -> PdfGenerationFactory.generatePdfFromHtml("<html><body><p>Test</p></body></html>"), "PDF generation should not throw for well-formed HTML input");
  }
}
