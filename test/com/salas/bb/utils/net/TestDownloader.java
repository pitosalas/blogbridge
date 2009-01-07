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
// $Id: TestDownloader.java,v 1.4 2006/10/16 10:12:40 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.TUtils;
import com.salas.bb.utils.FileUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This suite contains tests for <code>Downloader</code> unit.
 */
public class TestDownloader extends TestCase
{
    private File        tempDir;
    private Downloader  downloader;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        tempDir = TUtils.resetDir("../TestDownloader.tmp");
        downloader = new Downloader(null);
    }

    protected void tearDown()
        throws Exception
    {
        FileUtils.rmdir(tempDir);

        super.tearDown();
    }

    /**
     * Tests downloading of missing file.
     */
    public void testDownloadMissing()
        throws MalformedURLException, InterruptedException
    {
        URL missingLocation = new URL("file:///missing");
        try
        {
            downloader.download(missingLocation, tempDir);
            fail("File is missing.");
        } catch (FileNotFoundException e)
        {
            // Expected
        } catch (IOException e)
        {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }

    /**
     * Tests downloading existing file.
     */
    public void testDownloadExisting()
        throws IOException, InterruptedException
    {
        String name = TestDownloader.class.getName().replace('.', '/') + ".class";
        URL existingLocation = TestDownloader.class.getClassLoader().getResource(name);
        assertNotNull("Resource cannot be found.", existingLocation);

        File downloadedFile = downloader.download(existingLocation, tempDir);
        assertNotNull(downloadedFile);
    }

    /**
     * Tests getting the name of file.
     */
    public void testGetFilename()
        throws MalformedURLException
    {
        URL somefile = new URL("file://somedir/somefile.zip");
        URL noname = new URL("file://somedir/");

        assertEquals("somefile.zip", Downloader.getFilename(somefile));
        assertEquals(null, Downloader.getFilename(noname));
    }
}
