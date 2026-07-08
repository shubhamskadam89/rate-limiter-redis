package com.shubham.flashsale.common.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CachedServletOutputStream extends ServletOutputStream {

  private final ByteArrayOutputStream byteArrayOutputStream;

  public CachedServletOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
    this.byteArrayOutputStream = byteArrayOutputStream;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener listener) {
    // synchronous implementation
  }

  @Override
  public void write(int b) throws IOException {
    byteArrayOutputStream.write(b);
  }
}
