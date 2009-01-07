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
// $Id: TestHsqlPasswordsRepository.java,v 1.2 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.utils.net.auth.IPasswordsRepository;

import java.net.PasswordAuthentication;
import java.util.Arrays;

/**
 * This suite contains tests for <code>HsqlPasswordsRepository</code> unit.
 */
public class TestHsqlPasswordsRepository extends AbstractHsqlPersistenceTestCase
{
    private static final String SAMPLE_CONTEXT_A = "a";
    private static final String SAMPLE_CONTEXT_B = "b";
    private static final String SAMPLE_USERNAME = "cba";
    private static final char[] SAMPLE_PASSWORD = "abc".toCharArray();
    private static final PasswordAuthentication SAMPLE_AUTH_INFO =
        new PasswordAuthentication(SAMPLE_USERNAME, SAMPLE_PASSWORD);

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * Simple password encoding/decoding verification.
     */
    public void testEncodePassword()
    {
        assertNull(HsqlPasswordsRepository.recodePassword(null));
        assertEquals(0, HsqlPasswordsRepository.recodePassword("".toCharArray()).length);
        assertTrue(Arrays.equals("abc".toCharArray(), HsqlPasswordsRepository.recodePassword(
            HsqlPasswordsRepository.recodePassword("abc".toCharArray()))));
    }

    /**
     * Tests password recording.
     */
    public void testRecord()
    {
        IPasswordsRepository repository = initModernDatabase();
        PasswordAuthentication auth;

        auth = repository.getAuthInformation(SAMPLE_CONTEXT_A);
        assertNull("Database has no record about this context.", auth);

        repository.record(SAMPLE_CONTEXT_A, SAMPLE_AUTH_INFO);
        auth = repository.getAuthInformation(SAMPLE_CONTEXT_A);
        assertTrue("Database has record about this context.",
            Arrays.equals(SAMPLE_PASSWORD, auth.getPassword()));
        assertEquals("Database has record about this context.",
            SAMPLE_USERNAME, auth.getUserName());
    }

    /**
     * Tests forgetting the password from given context.
     */
    public void testForget()
    {
        IPasswordsRepository repository = initModernDatabase();

        repository.record(SAMPLE_CONTEXT_A, SAMPLE_AUTH_INFO);
        repository.record(SAMPLE_CONTEXT_B, SAMPLE_AUTH_INFO);
        repository.forget(SAMPLE_CONTEXT_A);

        PasswordAuthentication auth = repository.getAuthInformation(SAMPLE_CONTEXT_A);
        assertNull("Database should forget about the context.", auth);

        auth = repository.getAuthInformation(SAMPLE_CONTEXT_B);
        assertTrue("Database has record about this context.",
            Arrays.equals(SAMPLE_PASSWORD, auth.getPassword()));
        assertEquals("Database has record about this context.",
            SAMPLE_USERNAME, auth.getUserName());
    }

    /**
     * Tests complete forgeting of all passwords.
     */
    public void testForgetAll()
    {
        IPasswordsRepository repository = initModernDatabase();

        repository.record(SAMPLE_CONTEXT_A, SAMPLE_AUTH_INFO);
        repository.record(SAMPLE_CONTEXT_B, SAMPLE_AUTH_INFO);
        repository.forgetAll();

        PasswordAuthentication auth = repository.getAuthInformation(SAMPLE_CONTEXT_A);
        assertNull("Database should forget the context password.", auth);
        auth = repository.getAuthInformation(SAMPLE_CONTEXT_B);
        assertNull("Database should forget the context password.", auth);
    }

    private IPasswordsRepository initModernDatabase()
    {
        // Init most modern database
        initManager("/resources");
        return new HsqlPasswordsRepository(pm);
    }
}
