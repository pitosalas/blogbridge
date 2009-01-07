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
// $Id: ThreadTerminatingHandler.java,v 1.3 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * Thread terminating handler terminates the specific thread when the message with level
 * equal or greater than specified comes.
 */
public class ThreadTerminatingHandler extends Handler
{
    private StressRunner    runner;
    private Level           threshold;

    /**
     * Creates handler.
     *
     * @param aRunner       thread to terminate.
     * @param aThreshold    threshold level.
     */
    public ThreadTerminatingHandler(StressRunner aRunner, Level aThreshold)
    {
        runner = aRunner;
        threshold = aThreshold;
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     */
    public void close()
    {
    }

    /**
     * Flush any buffered output.
     */
    public void flush()
    {
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p/>
     * The logging request was made initially to a <tt>Logger</tt> object, which initialized the
     * <tt>LogRecord</tt> and forwarded it here.
     * <p/>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and if necessary.  The
     * formatting should include localization.
     *
     * @param record description of the log event
     */
    public void publish(LogRecord record)
    {
        if (record.getLevel().intValue() >= threshold.intValue())
        {
            if (!runner.isTerminating()) runner.setTerminating(true);
        }
    }
}
