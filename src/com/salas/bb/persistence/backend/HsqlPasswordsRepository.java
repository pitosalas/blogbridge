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
// $Id: HsqlPasswordsRepository.java,v 1.5 2006/05/29 12:48:30 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.utils.net.auth.IPasswordsRepository;
import com.salas.bb.utils.i18n.Strings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;

/**
 * Passwords repository based on HSQL database.
 */
class HsqlPasswordsRepository implements IPasswordsRepository
{
    private static final Logger LOG = Logger.getLogger(HsqlPasswordsRepository.class.getName());
    private static final int SECRET_CODE = 0x31;

    private final HsqlPersistenceManager context;

    /**
     * Creates passwords repository.
     *
     * @param aContext context.
     */
    public HsqlPasswordsRepository(HsqlPersistenceManager aContext)
    {
        context = aContext;
    }

    /**
     * Records new password for given context key. If key isn't present,
     * the record will be added, if it is -- updated.
     *
     * @param contextKey     context key.
     * @param authentication authentication info.
     */
    public void record(String contextKey, PasswordAuthentication authentication)
    {
        if (contextKey == null) return;

        // Assuring single-threaded access to the database
        synchronized (context)
        {
            try
            {
                PreparedStatement stmt = context.getPreparedStatement(
                    "DELETE FROM PASSWORDS WHERE CONTEXT = ?");
                try
                {
                    stmt.setString(1, contextKey);
                    stmt.executeUpdate();

                    if (authentication != null)
                    {
                        stmt = context.getPreparedStatement(
                            "INSERT INTO PASSWORDS (CONTEXT, USERNAME, PASSWORD) VALUES (?, ?, ?)");
                        stmt.setString(1, contextKey);
                        stmt.setString(2, authentication.getUserName());
                        stmt.setString(3, new String(recodePassword(authentication.getPassword())));
                        stmt.executeUpdate();
                    }

                    context.commit();
                } finally
                {
                    stmt.close();
                }
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.failed.to.record.password.for.context"),
                    new Object[] { contextKey }), e);
                context.rollback();
            }
        }
    }

    /**
     * Returns password entered by user for given context key.
     *
     * @param contextKey context key.
     *
     * @return authentication info or <code>NULL</code> if there's no match for the context.
     */
    public PasswordAuthentication getAuthInformation(String contextKey)
    {
        PasswordAuthentication auth = null;

        if (contextKey != null)
        {
            // Assuring single-threaded access to the database
            synchronized (context)
            {
                try
                {
                    PreparedStatement stmt = context.getPreparedStatement(
                        "SELECT USERNAME, PASSWORD FROM PASSWORDS WHERE CONTEXT = ?");
                    try
                    {
                        stmt.setString(1, contextKey);

                        String username = null;
                        char[] password = null;

                        ResultSet resultSet = stmt.executeQuery();
                        if (resultSet.next())
                        {
                            username = resultSet.getString("USERNAME");
                            password = recodePassword(resultSet.getString("PASSWORD").toCharArray());
                            auth = new PasswordAuthentication(username, password);
                        }
                    } finally
                    {
                        stmt.close();
                    }
                } catch (Exception e)
                {
                    LOG.log(Level.SEVERE, MessageFormat.format(
                        Strings.error("db.failed.to.load.password.for.context"),
                        new Object[] { contextKey }), e);
                }
            }
        }

        return auth;
    }

    /**
     * Forgets everything about stored passwords.
     */
    public void forgetAll()
    {
        // Assuring single-threaded access to the database
        synchronized (context)
        {
            try
            {
                PreparedStatement stmt = context.getPreparedStatement("DELETE FROM PASSWORDS");
                try
                {
                    stmt.executeUpdate();
                    context.commit();
                } finally
                {
                    stmt.close();
                }
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("db.failed.to.forget.all.passwords"), e);
                context.rollback();
            }
        }
    }

    /**
     * Forgets anything about this key.
     *
     * @param contextKey context key.
     */
    public void forget(String contextKey)
    {
        record(contextKey, null);
    }

    /**
     * Simple encoding/decoding of the password.
     *
     * @param aPassword original password.
     *
     * @return encoded password.
     */
    static char[] recodePassword(char[] aPassword)
    {
        if (aPassword == null) return null;

        char[] chars = new char[aPassword.length];
        for (int i = 0; i < aPassword.length; i++)
        {
            chars[i] = (char)(aPassword[i] ^ SECRET_CODE);
        }

        return chars;
    }
}
