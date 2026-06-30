package com.shubham.flashsale.common.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * For learning purposes, this project includes a custom response wrapper.
 * In a production application, I would use Spring's ContentCachingResponseWrapper,
 * which handles additional servlet edge cases.
 */

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream cachedContent
             = new ByteArrayOutputStream();

    private ServletOutputStream outputStream;

    private PrintWriter writer;

    public CachedBodyHttpServletResponse(HttpServletResponse response, ByteArrayOutputStream cachedContent) {
        super(response);

    }


    @Override
    public ServletOutputStream getOutputStream(){

        if(writer !=null){
            throw new IllegalStateException(
                    "Writer already obtained."
            );
        }
        if(outputStream ==null){
            outputStream = new CachedServletOutputStream(cachedContent);

        }
        return outputStream;
    }


    @Override
    public PrintWriter getWriter() throws IOException{
        if(cachedContent != null){
            throw new IllegalStateException(
                    "OutputStream already obtained."
            );
        }

        if(writer ==null){
            writer = new PrintWriter(
                    new OutputStreamWriter(cachedContent,getCharacterEncoding())
            ,true
            );
        }
        return writer;

    }

    public String getCachedBody() throws IOException {

        if (writer != null) {
            writer.flush();
        }

        if (outputStream != null) {
            outputStream.flush();
        }

        return cachedContent.toString(getCharacterEncoding());
    }

    public void copyBodyToResponse() throws IOException {

        HttpServletResponse response =
                (HttpServletResponse) getResponse();

        response.getOutputStream().write(cachedContent.toByteArray());

        response.flushBuffer();
    }
}
