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
// $Id: TestBandwidthInputStream.java,v 1.4 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.net;

import junit.framework.TestCase;

import java.awt.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * @see BandwidthInputStream
 */
public class TestBandwidthInputStream extends TestCase
{
    /**
     * Tests limit of the bandwidth from top.
     */
    public void testOverlimit() throws IOException
    {
        BandwidthInputStream bis = new BandwidthInputStream(null);
        bis.setBandwidth((long)Integer.MAX_VALUE * 1000);
        assertEquals(Integer.MAX_VALUE, bis.getBytesAllowed());
    }

    /**
     * Tests reading in steps.
     */
    public void testBandwidthStep() throws Exception
    {
        long speed;
        speed = measureSpeed(0);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeed(30000);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);
    }

    /**
     * Tests reading in blocks.
     */
    public void testBandwidthBlock() throws Exception
    {
        long speed;

        speed = measureSpeed(0, 100);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeed(0, 1000);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeed(0, 100000);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeed(30000, 100);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);

        speed = measureSpeed(30000, 1000);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);

        speed = measureSpeed(30000, 100000);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);
    }

    /**
     * Tests skipping.
     */
    public void testBandwidthSkip() throws Exception
    {
        long speed;

        speed = measureSpeedSkip(0, 1);
        assertTrue(Long.toString(speed), speed > 30000);

        speed = measureSpeedSkip(0, 1000);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeedSkip(0, 100000);
        assertTrue(Long.toString(speed), speed > 10000);

        speed = measureSpeedSkip(30000, 100);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);

        speed = measureSpeedSkip(30000, 1000);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);

        speed = measureSpeedSkip(30000, 100000);
        assertTrue(Long.toString(speed), speed < 31000 && speed > 29000);
    }

    /**
     * Test the returning of availability.
     */
    public void testAvailable() throws Exception
    {
        final String name = Component.class.getName().replaceAll("\\.", "/") + ".class";
        InputStream in = ClassLoader.getSystemResourceAsStream(name);
        BandwidthInputStream bis = new BandwidthInputStream(in);

        int streamAvail = in.available();
        int bandAvail = bis.available();
        assertEquals(streamAvail, bandAvail);

        // set the bandwidth to some limit
        bis.setBandwidth(20);
        assertEquals(1, bis.available());

        bis.read();
        assertEquals(0, bis.available());

        bis.close();
    }

    // Measure single-step speed.
    private long measureSpeed(long bandwidth)
        throws IOException
    {
        return measureSpeed(bandwidth, -1);
    }

    // Measure block-reading speed.
    private long measureSpeed(long bandwidth, int bufsize)
        throws IOException
    {
        final String name = Component.class.getName().replaceAll("\\.", "/") + ".class";
        InputStream in = ClassLoader.getSystemResourceAsStream(name);
        BandwidthInputStream bis = new BandwidthInputStream(in);
        bis.setBandwidth(bandwidth);

        int size = 0;
        boolean block = false;
        byte[] buf = new byte[0];
        if (bufsize != -1)
        {
            buf = new byte[bufsize];
            block = true;
        }
        boolean finished = false;

        long start = System.currentTimeMillis();
        while (!finished)
        {
            int read;
            if (block)
            {
                read = bis.read(buf);
            } else
            {
                read = bis.read();
                if (read != -1) read = 1;
            }
            if (read == -1)
            {
                finished = true;
            } else
            {
                size += read;
            }
        }
        long time = (System.currentTimeMillis() - start);
        long speed = time == 0 ? Long.MAX_VALUE : (long)size * 1000l / time;

        bis.close();

        return speed;
    }

    // Measure skipping speed.
    private long measureSpeedSkip(long bandwidth, int bytes)
        throws IOException
    {
        final String name = Component.class.getName().replaceAll("\\.", "/") + ".class";
        InputStream in = ClassLoader.getSystemResourceAsStream(name);
        BandwidthInputStream bis = new BandwidthInputStream(in);
        bis.setBandwidth(bandwidth);

        int size = 0;
        boolean finished = false;

        long start = System.currentTimeMillis();
        while (!finished)
        {
            final long skipped = bis.skip(bytes);
            if (skipped > 0) size += skipped;
            int b = bis.read();
            if (b != -1) size++; else finished = true;
        }
        long time = (System.currentTimeMillis() - start);
        long speed = time == 0 ? Long.MAX_VALUE : (long)size * 1000l / time;

        bis.close();

        return speed;
    }
}
