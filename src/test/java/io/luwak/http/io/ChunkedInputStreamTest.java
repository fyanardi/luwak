package io.luwak.http.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit test for ChunkedInputStream
 *
 * @author Fredy Yanardi
 *
 */
public class ChunkedInputStreamTest {

    private static final String[] LINES = new String[] {
            "0123456789ABCDEF",
            "0123456789",
            "0123456789ABCDEF" + "0123456789ABCDEF" + "0123456789ABCDEF" + "0123456789ABCDEF"
                    + "0123456789ABCDEF" + "0123456789ABCDEF" + "0123456789ABCDEF"
                    + "0123456789ABCDEF" + "0123456789ABCDEF" + "0123456789ABCDEF"
    };

    private static final byte[] CHUNKED_BYTES = new byte[] {
            0x31, 0x30, 0x0D, 0x0A,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x0D, 0x0A,
            0x61, 0x0D, 0x0A,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x0D, 0x0A,
            0x61, 0x30, 0x0D, 0x0A,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x0D, 0x0A,
            0x30, 0x0D, 0x0A,
            0x0D, 0x0A
    };

    @Test
    public void testRead1() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[8];
        int rlen = in.read(buf);
        assertEquals(8, rlen);
        assertArrayEquals("01234567".getBytes(), buf);
        in.close();
    }

    @Test
    public void testRead2() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[16];
        int rlen = in.read(buf, 0, 8);
        assertEquals(8, rlen);
        assertArrayEquals(new byte[]{ 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, buf);
        in.close();
    }

    @Test
    public void testRead3() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[16];
        int rlen = in.read(buf);
        assertEquals(16, rlen);
        assertArrayEquals(LINES[0].getBytes(), buf);
        in.close();
    }

    /**
     * Testing valid offset > 0
     *
     * @throws IOException
     */
    @Test
    public void testRead4() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[16];
        int rlen = in.read(buf, 8, 8);
        assertEquals(8, rlen);
        assertArrayEquals(new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37 }, buf);
        in.close();
    }

    @Test
    public void testRead5() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[24];
        int rlen = in.read(buf);
        assertEquals(24, rlen);
        assertArrayEquals("0123456789ABCDEF01234567".getBytes(), buf);
        in.close();
    }

    /**
     * Test multiple read across multiple chunks.
     *
     * @throws IOException
     */
    @Test
    public void testRead6() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[24];

        int rlen = in.read(buf, 0, 8);
        assertEquals(8, rlen);

        rlen = in.read(buf, 8, 16);
        assertEquals(16, rlen);

        assertArrayEquals("0123456789ABCDEF01234567".getBytes(), buf);

        in.close();
    }

    /**
     * Test multiple read across multiple chunks, exhausting the underlaying input stream.
     *
     * @throws IOException
     */
    @Test
    public void testRead8() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[186];

        int rlen = in.read(buf, 0, 8);
        assertEquals(8, rlen);

        rlen = in.read(buf, 8, 16);
        assertEquals(16, rlen);

        rlen = in.read(buf, 24, 162);
        assertEquals(162, rlen);

        assertArrayEquals((LINES[0] + LINES[1] + LINES[2]).getBytes(), buf);

        rlen = in.read(new byte[32], 0, 32);
        assertEquals(-1, rlen);

        in.close();
    }

    @Test
    public void testRead9() throws IOException {
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(CHUNKED_BYTES));
        byte[] buf = new byte[186];

        int rlen = in.read(buf, 0, 200);
        assertEquals(186, rlen);

        assertArrayEquals((LINES[0] + LINES[1] + LINES[2]).getBytes(), buf);

        rlen = in.read(new byte[32]);
        assertEquals(-1, rlen);

        in.close();
    }

    /**
     * Test chunk(s) bigger than read buffer size (1024)
     * @throws IOException
     */
    @Test
    public void testRead10() throws IOException {
        // Chunk #1:
        // - Size: 0x40A (1034)
        // - Content: '0' for 1st 1024 bytes, 'A' to 'J' for the next/last 10 bytes
        byte[] BIG_CHUNKED_BYTES = new byte[5 + 1024 + 10 + 2 + 3 + 1 + 3 + 2];
        System.arraycopy("40A\r\n".getBytes(), 0, BIG_CHUNKED_BYTES, 0, 5);
        for (int i = 5; i < 5 + 1024; i++) {
            BIG_CHUNKED_BYTES[i] = 0x30;
        }
        for (int i = 5 + 1024; i < 5 + 1024 + 10; i++) {
            BIG_CHUNKED_BYTES[i] = (byte) (0x41 + i - 5 - 1024);
        }
        System.arraycopy("\r\n".getBytes(), 0, BIG_CHUNKED_BYTES, 5 + 1024 + 10, 2);
        System.arraycopy("0\r\n\r\n".getBytes(), 0, BIG_CHUNKED_BYTES, 5 + 1024 + 10 + 2, 5);

        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(BIG_CHUNKED_BYTES));

        byte[] buf = new byte[1024 + 10];

        int rlen = in.read(buf);
        assertEquals(1024 + 10, rlen);

        byte[] expectedBuffer = new byte[1024 + 10];
        for (int i = 0; i < 1024; i++) {
            expectedBuffer[i] = 0x30;
        }
        for (int i = 1024; i < 1024 + 10; i++) {
            expectedBuffer[i] = (byte) (0x41 + i - 1024);
        }
        assertArrayEquals(expectedBuffer, buf);

        rlen = in.read(new byte[32]);
        assertEquals(-1, rlen);

        in.close();
    }
}
