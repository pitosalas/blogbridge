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
// $Id: IRetriesPolicy.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

import java.io.IOException;

/**
 * Retries policy is used to determine if and when the connection/reading attempt should
 * be performed again after some failure. The policies may tune themselves depending on
 * the parameters they accept and choose one or the other strategy.
 */
public interface IRetriesPolicy
{
    /**
     * Returns period to wait before retrying connecting and reading.
     *
     * @param failure   description of a failure.
     *
     * @return time in a future to retry or -1 if the another try should not be taken.
     */
    long getTimeBeforeRetry(Failure failure);

    /**
     * Description of failure.
     */
    public static class Failure
    {
        private long        attemptStartTime;       // Time when the attempt has started
        private long        attemptFailureTime;     // Time when the attempt has failed
        private int         attemptNumber;          // Sequence number of the attempt (0 - first)

        private boolean     connectionEstablished;  // TRUE if connection has been established

        private IOException cause;                  // Original cause of failure

        private long        bytesRead;              // Number of bytes read already

        /**
         * Creates failure description object.
         *
         * @param aAttemptNumber            attempt sequence number (0 - first, 1 - second ...).
         * @param aAttemptStartTime         time of attempt start.
         * @param aAttemptFailureTime       time when failure happened.
         * @param aConnectionEstablished    TRUE if connection has been established before failure.
         * @param aBytesRead                bytes read before the failure.
         * @param aCause
         */
        public Failure(int aAttemptNumber, long aAttemptStartTime, long aAttemptFailureTime,
            boolean aConnectionEstablished, long aBytesRead, IOException aCause)
        {
            attemptNumber = aAttemptNumber;
            attemptStartTime = aAttemptStartTime;
            attemptFailureTime = aAttemptFailureTime;
            connectionEstablished = aConnectionEstablished;
            bytesRead = aBytesRead;
            cause = aCause;
        }

        /**
         * Returns time of attempt failure.
         *
         * @return time stamp.
         */
        public long getAttemptFailureTime()
        {
            return attemptFailureTime;
        }

        /**
         * Returns attempt sequence number.
         *
         * @return attempt number.
         */
        public int getAttemptNumber()
        {
            return attemptNumber;
        }

        /**
         * Returns time of attempt start.
         *
         * @return time stamp.
         */
        public long getAttemptStartTime()
        {
            return attemptStartTime;
        }

        /**
         * Returns TRUE if connection was established.
         *
         * @return TRUE if connection was established.
         */
        public boolean isConnectionEstablished()
        {
            return connectionEstablished;
        }

        /**
         * Returns number of bytes read so far.
         *
         * @return bytes.
         */
        public long getBytesRead()
        {
            return bytesRead;
        }

        /**
         * Returns original cause of failure.
         *
         * @return failue cause.
         */
        public IOException getCause()
        {
            return cause;
        }
    }
}
