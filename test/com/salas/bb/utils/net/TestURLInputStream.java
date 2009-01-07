// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software Foundation;
// either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program;
// if not, write to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA
//
// Contact: R. Pito Salas
// mailto:pitosalas@users.sourceforge.net
// More information: about BlogBridge
// http://www.blogbridge.com
// http://sourceforge.net/projects/blogbridge
//
// $Id: TestURLInputStream.java,v 1.9 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.net;

import junit.framework.TestCase;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see URLInputStream
 */
public class TestURLInputStream extends TestCase
{
    // Number of seconds to last each test.
    private static final int BANDWIDTH_TEST_SEC = 3;

    // Maximum error in seconds for the bandwidth limitation tests.
    private static final double BANDWIDTH_TEST_ERROR = 1;

    private int testResourceSize;

    public TestURLInputStream()
        throws IOException
    {
        URL url = getTestURL();
        testResourceSize = url.openConnection().getContentLength();
    }

    /**
     * Tests basic creation.
     */
    public void testCreation()
        throws IOException
    {
        URLInputStream uis = null;
        try
        {
            uis = new URLInputStream(null);
            fail("NULL-URL's aren't allowed.");
        } catch (Exception e)
        {
            // Expected
        } finally
        {
            if (uis != null) uis.close();
        }

        uis = new URLInputStream(new URL("file:/test123"));
        try
        {
            uis.available();
            fail("File should not be found.");
        } catch (IOException e)
        {
            // Expected - file not found
        } finally
        {
            uis.close();
        }

        try
        {
            uis = new URLInputStream(getTestURL());
            assertTrue(uis.available() > 0);
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        } finally
        {
            uis.close();
        }
    }

    /**
     * Checks unlimited bandwidth reading.
     */
    public void testBandwidthUnlimited()
        throws IOException
    {
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);
        int read = 0;
        long startTime = System.currentTimeMillis();
        while (uis.read() != -1) read++;
        long finishTime = System.currentTimeMillis();

        assertEquals(testResourceSize, read);
        assertTrue((finishTime - startTime) < 1000);

        uis.close();
    }

    /**
     * Checks limited bandwidth reading (byte-per read).
     */
    public void testBandwidthLimitedSingleStep()
        throws IOException
    {
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);
        uis.setBandwidth(testResourceSize / BANDWIDTH_TEST_SEC);

        int read = 0;
        long startTime = System.currentTimeMillis();
        while (uis.read() != -1) read++;
        long finishTime = System.currentTimeMillis();
        final long readTime = (finishTime - startTime);

        assertEquals(testResourceSize, read);
        assertTrue(Long.toString(readTime), readTime < (BANDWIDTH_TEST_SEC + BANDWIDTH_TEST_ERROR) * 1000);
        assertTrue(Long.toString(readTime), readTime > (BANDWIDTH_TEST_SEC - BANDWIDTH_TEST_ERROR) * 1000);

        uis.close();
    }

    /**
     * Checks limited bandwidth reading in blocks to buffer w/o offset and length.
     */
    public void testBandwidthLimitedBlockA()
        throws IOException
    {
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);
        uis.setBandwidth(testResourceSize / BANDWIDTH_TEST_SEC);

        int read = 0;
        byte[] buf = new byte[1000];
        long rd;
        long startTime = System.currentTimeMillis();
        while ((rd = uis.read(buf)) != -1) read += rd;
        long finishTime = System.currentTimeMillis();
        final long readTime = (finishTime - startTime);

        assertEquals(testResourceSize, read);
        assertTrue(Long.toString(readTime), readTime < (BANDWIDTH_TEST_SEC + BANDWIDTH_TEST_ERROR) * 1000);
        assertTrue(Long.toString(readTime), readTime > (BANDWIDTH_TEST_SEC - BANDWIDTH_TEST_ERROR) * 1000);

        uis.close();
    }

    /**
     * Checks limited bandwidth reading in blocks to buffer w/ offset and length specified.
     */
    public void testBandwidthLimitedBlockB()
        throws IOException
    {
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);
        uis.setBandwidth(testResourceSize / BANDWIDTH_TEST_SEC);

        int read = 0;
        byte[] buf = new byte[1000];
        long rd;
        long startTime = System.currentTimeMillis();
        while ((rd = uis.read(buf, 0, 10)) != -1) read += rd;
        long finishTime = System.currentTimeMillis();
        final long readTime = (finishTime - startTime);

        assertEquals(testResourceSize, read);
        assertTrue(Long.toString(readTime), readTime < (BANDWIDTH_TEST_SEC + BANDWIDTH_TEST_ERROR) * 1000);
        assertTrue(Long.toString(readTime), readTime > (BANDWIDTH_TEST_SEC - BANDWIDTH_TEST_ERROR) * 1000);

        uis.close();
    }

    /**
     * Tests how the pauses in reading are taken.
     */
    public void testBandwidthThreshold()
        throws IOException
    {
        long delay = 2;
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);
        uis.setBandwidth(testResourceSize / BANDWIDTH_TEST_SEC);

        long read = 0;
        long rd;
        byte[] buf = new byte[1000];

        long startTime = System.currentTimeMillis();
        uis.read();
        read++;
        try
        {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e)
        {
        }

        while ((rd = uis.read(buf)) != -1) read += rd;
        long finishTime = System.currentTimeMillis();
        final long readTime = (finishTime - startTime);

        assertEquals(testResourceSize, read);
        assertTrue(Long.toString(readTime), readTime < (BANDWIDTH_TEST_SEC + BANDWIDTH_TEST_ERROR + delay) * 1000);
        assertTrue(Long.toString(readTime), readTime > (BANDWIDTH_TEST_SEC - BANDWIDTH_TEST_ERROR + delay) * 1000);

        uis.close();
    }

    /**
     * Tests the changing of bandwidth on-the-fly.
     */
    public void testRealtimeBandwidthChanging()
        throws IOException
    {
        URL url = getTestURL();

        URLInputStream uis = new URLInputStream(url);

        int read = 0;
        long startTime = System.currentTimeMillis();

        // Read one byte with unlimited bandwidth usage
        assertTrue(uis.read() != -1);
        read++;

        // Change bandwidth limit
        uis.setBandwidth(testResourceSize / BANDWIDTH_TEST_SEC);

        // Read all the rest and measure the total time
        while (uis.read() != -1) read++;
        long finishTime = System.currentTimeMillis();
        final long readTime = (finishTime - startTime);

        assertEquals(testResourceSize, read);
        assertTrue(Long.toString(readTime), readTime < (BANDWIDTH_TEST_SEC + BANDWIDTH_TEST_ERROR) * 1000);
        assertTrue(Long.toString(readTime), readTime > (BANDWIDTH_TEST_SEC - BANDWIDTH_TEST_ERROR) * 1000);

        uis.close();
    }

    /**
     * Tests how the skipping works.
     */
    public void testSkipping()
        throws IOException
    {
        URL url = getTestURL();
        URLInputStream uis = new URLInputStream(url);
        int blockSize = testResourceSize / 3;
        int[] blocks = new int[] { blockSize, blockSize, blockSize, testResourceSize - blockSize * 3 };

        int read = 0;
        byte[] bl = new byte[blockSize];
        for (int i = 0; i < blocks.length; i++)
        {
            int block = blocks[i];
            while (block > 0)
            {
                if (i % 2 == 0)
                {
                    block -= uis.read(bl, 0, block);
                } else
                {
                    block -= (int)uis.skip(block);
                }
            }
            read += blocks[i];
        }

        assertEquals(testResourceSize, read);

        uis.close();
    }

    /**
     * Tests how the stream is automatically closing on completion.
     */
    public void testAutoClosing()
        throws IOException
    {
        URL url = getTestURL();
        URLInputStream uis = new URLInputStream(url);

        int size = testResourceSize;
        while (size > 0)
        {
            size -= uis.skip(size);
        }

        assertEquals(-1, uis.read());
        assertEquals(-1, uis.read());

        assertEquals(-1, uis.read(new byte[10]));
        assertEquals(-1, uis.read(new byte[10]));

        assertEquals(-1, uis.read(new byte[10], 0, 1));
        assertEquals(-1, uis.read(new byte[10], 0, 1));

        // Skipping skips even when stream has ended
        assertEquals(1, uis.skip(1));

        uis.close();
    }

    /**
     * @see URLInputStream#handleFailure
     */
    public void testHandleFailure() throws IOException
    {
        IRetriesPolicy.Failure failure;

        URLInputStream uis = new URLInputStream(getTestURL());
        uis.setRetriesPolicy(new DirectRetriesPolicy(1, 0));

        failure = new IRetriesPolicy.Failure(0, 0, 0, false, 0, new IOException("Failed"));
        uis.handleFailure(failure);

        failure = new IRetriesPolicy.Failure(1, 0, 0, false, 0, new IOException("Failed"));
        try
        {
            uis.handleFailure(failure);
            fail("Handling should fail. Attempts ran out.");
        } catch (IOException e)
        {
            // Expected
            assertEquals("Failed", e.getMessage());
        }

        uis.close();
    }

    /**
     * @see URLInputStream#connectionAttempt
     */
    public void testConnectionAttempt() throws IOException
    {
        IRetriesPolicy.Failure failure;
        DirectRetriesPolicy retriesPolicy = new DirectRetriesPolicy(1, 0);
        URL testURL = getTestURL();

        URLInputStream uis = new URLInputStream(testURL)
        {
            // Always fails.
            protected InputStream makeConnection(long read)
                throws IOException
            {
                throw new IOException("Failed");
            }
        };
        uis.setRetriesPolicy(retriesPolicy);

        long start = System.currentTimeMillis();
        failure = uis.connectionAttempt(2);
        long finish = System.currentTimeMillis();

        assertEquals(2, failure.getAttemptNumber());
        assertTrue(failure.getAttemptStartTime() >= start && failure.getAttemptStartTime() <= finish);
        assertTrue(failure.getAttemptFailureTime() >= start && failure.getAttemptFailureTime() <= finish);
        assertEquals(0, failure.getBytesRead());
        assertEquals("Failed", failure.getCause().getMessage());

        uis.close();

        BandwidthInputStream stream;

        // Default bandwidth and success
        uis = new URLInputStream(testURL);
        uis.setRetriesPolicy(retriesPolicy);
        failure = uis.connectionAttempt(0);
        stream = uis.getStream();
        assertNull(failure);
        assertNotNull(stream);
        assertEquals(0, stream.getBandwidth());

        uis.close();

        // Custom bandwidth and success
        uis = new URLInputStream(testURL);
        uis.setRetriesPolicy(retriesPolicy);
        uis.setBandwidth(1000);
        failure = uis.connectionAttempt(0);
        stream = uis.getStream();
        assertNull(failure);
        assertNotNull(stream);
        assertEquals(uis.getBandwidth(), stream.getBandwidth());

        uis.close();
    }

    /**
     * @see URLInputStream#connect
     */
    public void testConnectToClosedStream() throws IOException
    {
        URLInputStream uis = new URLInputStream(getTestURL());
        uis.close();
        try
        {
            uis.read();
            fail("IOException should be thrown. The stream is already closed.");
        } catch (IOException e)
        {
            // Expected
        }
    }

    /**
     * @see URLInputStream#connect
     */
    public void testRepeatableConnection() throws IOException
    {
        // Passes first connection and then fails all the way.
        URLInputStream uis = new URLInputStream(getTestURL())
        {
            private int cnt = 0;

            protected InputStream makeConnection(long read)
                throws IOException
            {
                if (cnt++ > 0) throw new IOException("Failed");
                return super.makeConnection(read);
            }
        };

        // Connects and read byte
        assertTrue(uis.read() != -1);

        // If this one will require another connection we will fail.
        assertTrue(uis.read() != -1);

        uis.close();
    }

    /**
     * @see URLInputStream#connect
     */
    public void testSecondAttemptConnection() throws IOException
    {
        // Fails first attempt and then passes all the way.
        URLInputStream uis = new URLInputStream(getTestURL())
        {
            private int cnt = 0;

            protected InputStream makeConnection(long read)
                throws IOException
            {
                if (cnt++ < 1) throw new IOException("Failed");
                return super.makeConnection(read);
            }
        };

        // Connects and read byte
        assertTrue(uis.read() != -1);

        uis.close();
    }

    /**
     * Checks the resuming during reading byte.
     */
    public void testResumeReadingByte()
        throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        // Fails to read tenth byte once
        URLInputStream uis = new URLInputStream(getTestURL())
        {
            private boolean failed = false;

            protected InputStream makeConnection(long read)
                throws IOException
            {
                InputStream in = super.makeConnection(read);

                if (!failed)
                {
                    in = new SingleFailInputStream(in, 10);
                    failed = true;
                }

                return in;
            }
        };
        uis.setRetriesPolicy(new DirectRetriesPolicy(1, 0));

        int size = 0;
        while (uis.read() != -1) size++;

        assertEquals(testResourceSize, size);

        uis.close();
    }

    /**
     * Checks the resuming during reading block.
     */
    public void testResumeReadingBlock()
        throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        // Fails to read tenth byte once
        URLInputStream uis = new URLInputStream(getTestURL())
        {
            private boolean failed = false;

            protected InputStream makeConnection(long read)
                throws IOException
            {
                InputStream in = super.makeConnection(read);

                if (!failed)
                {
                    in = new SingleFailInputStream(in, 10);
                    failed = true;
                }

                return in;
            }
        };
        uis.setRetriesPolicy(new DirectRetriesPolicy(1, 0));

        int size = 0;
        byte[] buf = new byte[100];
        int read;
        while ((read = uis.read(buf)) != -1) size += read;

        assertEquals(testResourceSize, size);

        uis.close();
    }

    /**
     * Checks the resuming during skipping.
     */
    public void testResumeSkipping()
        throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        // Fails to read tenth byte once
        URLInputStream uis = new URLInputStream(getTestURL())
        {
            private boolean failed = false;

            protected InputStream makeConnection(long read)
                throws IOException
            {
                InputStream in = super.makeConnection(read);

                if (!failed)
                {
                    in = new SingleFailInputStream(in, 10);
                    failed = true;
                }

                return in;
            }
        };
        uis.setRetriesPolicy(new DirectRetriesPolicy(1, 0));

        int size = testResourceSize;
        while (size > 0) size -= uis.skip(size);

        uis.close();
    }

    /**
     * Tests reporting the progress when reading streams in byte-size steps.
     */
    public void testProgressReadingBytes() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL());
        uis.addListener(listener);

        while (uis.read() != -1);

        assertTrue(listener.connecting);
        assertTrue(listener.connected);
        assertTrue(listener.finished);
        assertTrue(listener.size > 0);
        assertEquals(listener.size, listener.read);

        uis.close();
    }

    /**
     * Tests reporting the progress when reading streams in block steps.
     */
    public void testProgressReadingBlocks() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL());
        uis.addListener(listener);

        byte[] buf = new byte[100];
        while (uis.read(buf) != -1);

        assertTrue(listener.connecting);
        assertTrue(listener.connected);
        assertTrue(listener.finished);
        assertTrue(listener.size > 0);
        assertEquals(listener.size, listener.read);

        uis.close();
    }

    /**
     * Tests reporting the progress when skipping streams.
     */
    public void testProgressSkipping() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL());
        uis.addListener(listener);

        int size = testResourceSize;
        while (size > 0) size -= uis.skip(size);

        assertTrue(listener.connecting);
        assertTrue(listener.connected);
        assertTrue(listener.finished);
        assertTrue(listener.size > 0);
        assertEquals(listener.size, listener.read);

        listener.finished = false;
        uis.skip(1);

        // Finished shouldn't be called twice
        assertFalse(listener.finished);

        assertEquals(listener.size, listener.read);

        uis.close();
    }

    /**
     * Tests reading of bytes in stream of undetermined length and how the finish is
     * reported.
     */
    public void testReportingFinishReadingBytesOfEndless() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL())
        {
            protected int resolveContentLength(URLConnection aCon)
            {
                // Always tell "undetermined".
                return -1;
            }
        };
        uis.addListener(listener);

        while (uis.read() != -1);

        assertTrue(listener.finished);

        uis.close();
    }

    /**
     * Tests reading of blocks in stream of undetermined length and how the finish is
     * reported.
     */
    public void testReportingFinishReadingBlocksOfEndless() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL())
        {
            protected int resolveContentLength(URLConnection aCon)
            {
                // Always tell "undetermined".
                return -1;
            }
        };
        uis.addListener(listener);

        byte[] buf = new byte[100];
        while (uis.read(buf) != -1);

        assertTrue(listener.finished);

        uis.close();
    }

    /**
     * Tests how the UIS reports errored state during connection
     * (for example, when file is missing).
     */
    public void testFailureReportingDuringConnection() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(new URL("file:///missing.file"));
        uis.addListener(listener);

        try
        {
            uis.read();
            fail("File is missing -- exception should be thrown.");
        } catch (IOException e)
        {
            // Expected
        }

        assertTrue(listener.connecting);
        assertFalse(listener.connected);
        assertFalse(listener.finished);
        assertTrue(listener.errored);

        uis.close();
    }

    /**
     * Tests how the UIS reports errored state during reading.
     */
    public void testFailureReportingDuringReading() throws IOException
    {
        Logger logger = Logger.getLogger(URLInputStream.class.getName());
        logger.setLevel(Level.OFF);

        RecordingStreamListener listener = new RecordingStreamListener();

        URLInputStream uis = new URLInputStream(getTestURL());
        uis.addListener(listener);

        assertTrue(uis.read() != -1);

        // Here we are cheating! Instead of honest simulation of several errors we simply
        // close the stream and try to access it again which produces immediate error.
        uis.close();

        try
        {
            uis.read();
            fail("The stream is closed -- exception should be thorwn.");
        } catch (IOException e)
        {
            // Expected
        }

        assertTrue(listener.connecting);
        assertTrue(listener.connected);
        assertEquals(testResourceSize, listener.size);
        assertEquals(1, listener.read);
        assertTrue(listener.finished);
        assertTrue(listener.errored);
    }

    /**
     * Tests closing of stream even before connecting.
     */
    public void testClosingBeforeOpening() throws IOException
    {
        URLInputStream uis = new URLInputStream(getTestURL());
        uis.close();

        try
        {
            uis.read();
            fail("Connection is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }
    }

    /**
     * Tests normal closing sequence.
     */
    public void testClosing() throws IOException
    {
        URLInputStream uis = new URLInputStream(getTestURL());

        assertTrue(uis.read() != -1);

        uis.close();

        try
        {
            uis.read();
            fail("Stream is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }

        try
        {
            uis.read(new byte[0]);
            fail("Stream is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }

        try
        {
            uis.read(new byte[1], 0, 1);
            fail("Stream is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }

        try
        {
            uis.available();
            fail("Stream is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }

        try
        {
            uis.skip(1);
            fail("Stream is already closed. Cannot read.");
        } catch (IOException e)
        {
            // Expected
        }
    }

    /**
     * Tests permanent redirection notifications.
     */
    public void testRedirection()
        throws IOException
    {
        URL url = new URL("http://www.blogbridge.com/weblog/index.xml");

        PermRedirListener l = new PermRedirListener();
        URLInputStream uis = new URLInputStream(url);
        uis.setRedirectionListener(l);
        
        assertTrue(uis.read() != -1);
        assertTrue("Redirection didn't happen.", l.redirected);

        uis.close();
    }

    // Returns test URL.
    private URL getTestURL()
    {
        final String name = TestURLInputStream.class.getName().replaceAll("\\.", "/") + ".class";
        final URL resource = TestURLInputStream.class.getClassLoader().getResource(name);

        assertNotNull(resource);

        return resource;
    }

    /**
     * Simply records the fact of notification.
     */
    private static class PermRedirListener implements IPermanentRedirectionListener
    {
        private boolean redirected;

        public void redirectedTo(URL newLocation)
        {
            redirected = true;
        }
    }
    /**
     * Fails to read the specified byte in the stream once
     */
    private static class SingleFailInputStream extends FilterInputStream
    {
        private int failByte;
        private int read;

        public SingleFailInputStream(InputStream in, int aFailByte)
        {
            super(in);

            failByte = aFailByte;
            read = 0;
        }

        public int read()
            throws IOException
        {
            if (read++ == failByte)
            {
                throw new IOException("Connection terminated.");
            }

            return super.read();
        }

        public int read(byte b[], int off, int len)
            throws IOException
        {
            if (read + len > failByte)
            {
                throw new IOException("Connection terminated.");
            }

            int rd = super.read(b, off, len);
            if (rd != -1) read += rd;
            return rd;
        }

        public long skip(long n)
            throws IOException
        {
            if (read + n > failByte)
            {
                throw new IOException("Connection terminated.");
            }

            long sk = super.skip(n);
            read += sk;
            return sk;
        }
    }

    /**
     * Listener, recording whole reading process.
     */
    private static class RecordingStreamListener implements IStreamProgressListener
    {
        private boolean connecting = false;
        private boolean connected = false;
        private long size = 0;
        private long read = 0;
        private boolean finished = false;
        private boolean errored = false;

        public void connecting(URLInputStream source)
        {
            assertFalse(connecting);
            assertFalse(connected);
            assertEquals(0, size);
            assertEquals(0, read);
            assertFalse(finished);

            connecting = true;

            // Throwing exception to test how we are actually protected against that.
            throw new RuntimeException("Test");
        }

        public void connected(URLInputStream source, long length)
        {
            assertTrue(connecting);
            assertFalse(connected);
            assertEquals(0, size);
            assertEquals(0, read);
            assertFalse(finished);

            connected = true;
            size = length;

            // Throwing exception to test how we are actually protected against that.
            throw new RuntimeException("Test");
        }

        public void read(URLInputStream source, long bytes)
        {
            assertTrue(connecting);
            assertTrue(connected);
            assertFalse(finished);

            read += bytes;

            if (size != -1) assertTrue(read <= size);

            // Throwing exception to test how we are actually protected against that.
            throw new RuntimeException("Test");
        }

        public void finished(URLInputStream source)
        {
            assertTrue(connecting);
            assertTrue(connected);
            assertFalse(finished);
            finished = true;

            // Throwing exception to test how we are actually protected against that.
            throw new RuntimeException("Test");
        }

        public void errored(URLInputStream source, IOException ex)
        {
            assertTrue(connecting);
            errored = true;

            // Throwing exception to test how we are actually protected against that.
            throw new RuntimeException("Test");
        }
    }
}
