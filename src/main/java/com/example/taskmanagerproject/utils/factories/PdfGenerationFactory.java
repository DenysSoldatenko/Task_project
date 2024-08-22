package com.example.taskmanagerproject.utils.factories;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.xml;
import static org.jsoup.nodes.Entities.EscapeMode.xhtml;

import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Factory class for generating PDFs from HTML templates.
 * This utility class provides methods for loading HTML templates from files
 * and generating PDF documents from the HTML content.
 */
@Slf4j
@UtilityClass
public class PdfGenerationFactory {

  /**
   * Loads an HTML template from the specified file path.
   *
   * @param filePath the path to the HTML template file
   * @return the HTML content as a string
   * @throws PdfGenerationException if the file can't be loaded or read
   */
  public static String loadTemplate(String filePath) {
    log.info("Loading HTML template from file: {}", filePath);
    try {
      return readString(new File(filePath).toPath(), UTF_8);
    } catch (IOException e) {
      log.error("Error loading HTML template from file: {}", filePath, e);
      throw new PdfGenerationException("Failed to load HTML template from file: " + filePath);
    }
  }

  /**
   * Generates a PDF document from the provided HTML content.
   *
   * @param htmlContent the HTML content to be converted into a PDF
   * @return the generated PDF as a byte array
   * @throws PdfGenerationException if the PDF generation fails
   */
  public static byte[] generatePdfFromHtml(String htmlContent) {
    log.info("Generating PDF from HTML...");
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Document document = Jsoup.parse(htmlContent);
      document.outputSettings().syntax(xml);
      document.outputSettings().escapeMode(xhtml);

      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(document.html());
      renderer.layout();
      renderer.createPDF(outputStream);

      log.info("PDF generation completed successfully.");
      return outputStream.toByteArray();
    } catch (IOException | DocumentException e) {
      log.error("Error occurred while generating PDF", e);
      throw new PdfGenerationException("Failed to generate PDF.");
    }
  }
}
