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
// $Id: ReportingLogHandler.java,v 1.8 2007/04/10 11:23:34 spyromus Exp $
//

package com.salas.bb.utils;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.salas.bb.service.ServerService;
import com.salas.bb.utils.concurrency.ExecutorFactory;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Log handler which is sending messages with levels equal or higher than defined to
 * the service.
 */
public class ReportingLogHandler extends Handler
{
    private static final Executor EXECUTOR =
        ExecutorFactory.createPooledExecutor("Reporting Log Handler", 1, 2000);

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
        if (isLoggable(record))
        {
            PublishLogRecord task = new PublishLogRecord(record, Thread.currentThread().getName());

            try
            {
                EXECUTOR.execute(task);
            } catch (InterruptedException e)
            {
                task.run();
            }
        }
    }

    /**
     * Returns TRUE if message is loggable: level is allowed and the cause
     * isn't SecurityException.
     *
     * @param record record.
     *
     * @return TTUR if loggable.
     */
    public boolean isLoggable(LogRecord record)
    {
        boolean loggable = super.isLoggable(record);
        Throwable cause = record == null ? null : record.getThrown();

        return loggable && (cause == null || (!(cause instanceof SecurityException) &&
            isAllowedMessage(cause.getMessage())));
    }

    /**
     * Returns <code>TRUE</code> if the message is not one of these we wish to block.
     *
     * @param msg   msg.
     *
     * @return <code>TRUE</code> if the message is not one of these we wish to block. 
     */
    private boolean isAllowedMessage(String msg)
    {
        return msg != null &&
            msg.indexOf("timed out") == -1 &&
            msg.indexOf("No route to host") == -1 &&
            msg.indexOf("Network is unreachable") == -1;
    }

    /**
     * Runnable task of publishing log record to remote service.
     */
    private static class PublishLogRecord implements Runnable
    {
        private final LogRecord record;
        private final String    threadName;

        /**
         * Creates task.
         *
         * @param aRecord       record to publish.
         * @param aThreadName   name of thread the record has come from.
         */
        public PublishLogRecord(LogRecord aRecord, String aThreadName)
        {
            record = aRecord;
            threadName = aThreadName;
        }

        /**
         * Publishing processing.
         */
        public void run()
        {
            Throwable exc = record.getThrown();

            StringBuffer sb = new StringBuffer();

            sb.append(" [");
            sb.append(threadName);
            sb.append("] ");

            // We simply want the Class name, so we strip off the package names

            String loggername = record.getLoggerName();
            sb.append(loggername.substring(loggername.lastIndexOf(".") + 1));
            sb.append(" ");
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");

            String message = record.getMessage();
            String description = null;

            if (message != null)
            {
                int crIndex = message.indexOf('\n');
                if (crIndex > -1)
                {
                    description = message.substring(crIndex);
                    message = message.substring(0, crIndex);
                }
            }
            sb.append(message);

            ServerService.reportClientError(sb.toString(), description, exc);
        }
    }
}
