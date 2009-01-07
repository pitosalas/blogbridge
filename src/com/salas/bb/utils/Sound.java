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
// $Id: Sound.java,v 1.7 2006/05/29 12:50:07 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;

/**
 * Collection of helpfull sound-related utilities.
 */
public final class Sound
{
    private static final Logger LOG = Logger.getLogger(Sound.class.getName());

    /**
     * Hidden utility class constructor.
     */
    private Sound()
    {
    }

    /**
     * Plays sound who's path is specified in resources by the <code>resourceId</code> key.
     *
     * @param resourceId    the key.
     */
    public static void play(String resourceId)
    {
        if (resourceId == null) return;

        String path = ResourceUtils.getString(resourceId);
        if (path == null)
        {
            LOG.warning(MessageFormat.format(Strings.error("resource.not.found"),
                new Object[] { resourceId }));
            return;
        }

        InputStream inputStream = ResourceUtils.getInputStream(path);
        try
        {
            play(inputStream);
        } catch (Exception e)
        {
            LOG.log(Level.WARNING, MessageFormat.format(
                Strings.error("failed.to.play.sound"),
                new Object[] { resourceId }), e);
        }
    }

    /**
     * Plays audio stream.
     *
     * @param stream   stream.
     *
     * @throws UnsupportedAudioFileException    if format is unsupported.
     * @throws IOException                      if I/O error happens.
     */
    public static void play(final InputStream stream)
        throws UnsupportedAudioFileException, IOException
    {
        if (stream == null) return;

        play(AudioSystem.getAudioInputStream(stream));
    }

    /**
     * Plays audio stream.
     *
     * @param audioStream   stream.
     *
     * @throws IOException if I/O error happens.
     */
    public static void play(AudioInputStream audioStream)
        throws IOException
    {
        if (audioStream == null) return;

        try
        {
            Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
            clip.open(audioStream);
            clip.start();
        } catch (LineUnavailableException e)
        {
            // It's not a problem if user has no audio device
        }
    }
}
