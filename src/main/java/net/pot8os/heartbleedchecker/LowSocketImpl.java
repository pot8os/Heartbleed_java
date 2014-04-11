/*
 * The MIT License
 *
 * Copyright 2014 pot8os.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.pot8os.heartbleedchecker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 *
 * @author pot8os
 */
public class LowSocketImpl {

    private final Socket socket = new Socket();
    private final OutputStream outStream;
    private final InputStream inStream;

    public LowSocketImpl(String host, int port) throws IOException {
        final int timeout = 5 * 1000;
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        this.outStream = socket.getOutputStream();
        this.inStream = socket.getInputStream();
    }

    public LowSocketImpl write(byte[] value) throws IOException {
        outStream.write(value);
        return this;
    }

    public void flush() throws IOException {
        outStream.flush();
    }

    public byte[] getResponse() throws InterruptedException, ExecutionException {
        final FutureTask<byte[]> task = new FutureTask<byte[]>(new Callable<byte[]>() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            @Override
            public byte[] call() {
                byte[] buffer = new byte[2048];
                try {
                    while (true) {
                        int length = inStream.read(buffer);
                        if (length < 0) {
                            break;
                        }
                        baos.write(buffer, 0, length);
                    }
                } catch (Exception e) {
                }
                return baos.toByteArray();
            }
        });
        new Thread(task).start();
        return task.get();
    }
    
    public void close() throws IOException{
        socket.close();
    }
}
