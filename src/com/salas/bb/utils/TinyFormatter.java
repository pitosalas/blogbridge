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
// $Id: TinyFormatter.java,v 1.7 2006/01/08 05:04:21 kyank Exp $
//

package com.salas.bb.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Print a brief summary of the LogRecord in a human readable
 * format. The summary will typically be 1 or 2 lines.
 */
public class TinyFormatter extends Formatter
{
    private Date dat = new Date();
    private MessageFormat formatter;
    private Object args[] = new Object[1];
    private static final String FORMAT = "{0,time, hh:mm:ss}";


    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator =
            (String)java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction("line.separator"));

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     *
     * @return a formatted log record.
     */
    public synchronized String format(LogRecord record)
    {
        StringBuffer sb = new StringBuffer();

        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        args[0] = dat;
        StringBuffer text = new StringBuffer();
        if (formatter == null)
        {
            formatter = new MessageFormat(FORMAT);
        }
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(" [");
        sb.append(record.getThreadID()).append(" ").append(Thread.currentThread().getName());
        sb.append("] ");

        // We simply want the Class name, so we strip off the package names

        String loggername = record.getLoggerName();
        int index = loggername.lastIndexOf(".");
        sb.append(loggername.substring(index + 1));
        String message = formatMessage(record);

        sb.append(" ");
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append(lineSeparator);
        if (record.getThrown() != null)
        {
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return sb.toString();
    }
}
