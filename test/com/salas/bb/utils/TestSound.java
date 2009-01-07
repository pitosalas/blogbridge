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
// $Id: TestSound.java,v 1.3 2006/11/13 12:57:13 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.uif.util.ResourceUtils;
import junit.framework.TestCase;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @see Sound
 */
public class TestSound extends TestCase
{
    private static final String TEST_SOUND_RESOURCE_ID          = "sound.no.unread";
    private static final String TEST_INVALID_SOUND_RESOURCE_ID  = "splash.image";
    private static final String TEST_SOUND_RESOURCE_PATH        = "resources/sounds/click.wav";

    protected void setUp()
        throws Exception
    {
        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
    }

    /**
     * Tests playing audio streams.
     *
     * @throws Exception in case of error.
     */
    public void testPlayAudioStream() throws Exception
    {
        Sound.play((AudioInputStream)null);
        Sound.play(getTestSoundAS());
    }

    /**
     * Tests playing input streams.
     *
     * @throws Exception in case of error.
     */
    public void testPlayInputStream() throws Exception
    {
        Sound.play((InputStream)null);
        Sound.play(getTestSoundIS());

        try
        {
            Sound.play(getTestInvalidStream());
            fail("Stream is invalid -- should fail.");
        } catch (UnsupportedAudioFileException e)
        {
            // Expected
        } catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Tests playing audio from resources.
     */
    public void testPlay()
    {
        Logger logger = Logger.getLogger(Sound.class.getName());
        Level oldLevel = logger.getLevel();

        logger.setLevel(Level.OFF);

        try
        {
            Sound.play((String)null);
            Sound.play(TEST_SOUND_RESOURCE_ID);
            Sound.play("Abracadabra");
            Sound.play(TEST_INVALID_SOUND_RESOURCE_ID);
        } finally
        {
            logger.setLevel(oldLevel);
        }
    }

    // Returns non-audio stream.
    private static InputStream getTestInvalidStream()
    {
        ClassLoader classLoader = TestSound.class.getClassLoader();
        String name = TestSound.class.getName().replace('.', '/') + ".class";
        InputStream inputStream = classLoader.getResourceAsStream(name);

        assertNotNull(inputStream);

        return inputStream;
    }

    // Returns test sound.
    private AudioInputStream getTestSoundAS()
    {
        AudioInputStream audioInputStream = null;
        try
        {
            InputStream inputStream = getTestSoundIS();
            audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        } catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }

        assertNotNull(audioInputStream);

        return audioInputStream;
    }

    // Returns test audio stream.
    private static InputStream getTestSoundIS()
    {
        InputStream inputStream = ResourceUtils.getInputStream(TEST_SOUND_RESOURCE_PATH);

        assertNotNull(inputStream);

        return inputStream;
    }
}
