package icu.nothingless.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * 响应包装器，用于捕获 Servlet 输出内容
 */
public class MenuResponseWrapper extends HttpServletResponseWrapper {
    
    private ByteArrayOutputStream capture;
    private ServletOutputStream output;
    private PrintWriter writer;

    public MenuResponseWrapper(HttpServletResponse response) {
        super(response);
        capture = new ByteArrayOutputStream(response.getBufferSize());
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() already called");
        }
        if (output == null) {
            output = new ServletOutputStream() {
                @Override
                public boolean isReady() { return true; }
                
                @Override
                public void setWriteListener(WriteListener listener) {}
                
                @Override
                public void write(int b) throws IOException {
                    capture.write(b);
                }
            };
        }
        return output;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (output != null) {
            throw new IllegalStateException("getOutputStream() already called");
        }
        if (writer == null) {
            writer = new PrintWriter(capture);
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        super.flushBuffer();
        if (writer != null) writer.flush();
        if (output != null) output.flush();
    }

    public String getCaptureAsString() throws IOException {
        if (writer != null) {
            writer.flush();
            return capture.toString("UTF-8");
        }
        if (output != null) {
            output.flush();
        }
        return capture.toString("UTF-8");
    }
}