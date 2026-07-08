package com.shubham.flashsale.common.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * For learning purposes, this project includes a custom response wrapper. In a production
 * application, I would use Spring's ContentCachingResponseWrapper, which handles additional servlet
 * edge cases.
 */
public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

  private final ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();

  private ServletOutputStream outputStream;

  private PrintWriter writer;

  public CachedBodyHttpServletResponse(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() {

    if (writer != null) {
      throw new IllegalStateException("getWriter() has already been called for this response");
    }
    if (outputStream == null) {
      outputStream = new CachedServletOutputStream(cachedContent);
    }
    return outputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (outputStream != null) {
      throw new IllegalStateException(
          "getOutputStream() has already been called for this response");
    }

    if (writer == null) {
      writer = new PrintWriter(new OutputStreamWriter(cachedContent, resolveCharset()), true);
    }
    return writer;
  }

  public String getCachedBody() throws IOException {
    flushCachedBody();
    return cachedContent.toString(resolveCharset());
  }

  public void copyBodyToResponse() throws IOException {
    flushCachedBody();
    HttpServletResponse originalResponse = (HttpServletResponse) getResponse();

    originalResponse.getOutputStream().write(cachedContent.toByteArray());

    originalResponse.flushBuffer();
  }

  private void flushCachedBody() throws IOException {
    if (writer != null) {
      writer.flush();
    }

    if (outputStream != null) {
      outputStream.flush();
    }
  }

  private Charset resolveCharset() {
    String encoding = getCharacterEncoding();

    if (encoding == null || encoding.isBlank()) {
      return StandardCharsets.UTF_8;
    }

    return Charset.forName(encoding);
  }
}
