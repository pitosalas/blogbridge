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
// $Id: Package.java,v 1.3 2007/03/23 07:49:16 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import java.util.ArrayList;

/**
 * The package containing one or more plug-ins.
 */
public class Package extends ArrayList<IPlugin>
{
    private final String fileName;

    private final String name;
    private final String description;
    private final String version;
    private final String author;
    private final String email;

    /**
     * Creates an empty package.
     *
     * @param fileName      name of the package file.
     * @param name          name of the package.
     * @param description   description of the package.
     * @param version       version of the package.
     * @param author        author of the package.
     * @param email         author e-mail.
     */
    public Package(String fileName, String name, String description,
                   String version, String author, String email)
    {
        this.fileName = fileName;
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
        this.email = email;
    }

    /**
     * Returns the file name.
     *
     * @return file name.
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Returns the name of the package.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the description of the package.
     *
     * @return description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the version.
     *
     * @return version.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the author.
     *
     * @return author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Returns the author e-mail address.
     *
     * @return address.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Compares the package to another.
     *
     * @param o other object.
     *
     * @return <code>TRUE</code> if equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package)o;

        if (!fileName.equals(aPackage.fileName)) return false;
        if (!name.equals(aPackage.name)) return false;
        if (version != null ? !version.equals(aPackage.version) : aPackage.version != null) return false;

        return true;
    }

    /**
     * Returns the hash code of this package.
     *
     * @return code.
     */
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + fileName.hashCode();
        return result;
    }

    /**
     * Initializes the package and all plug-ins.
     */
    public void initialize()
    {
        for (IPlugin plugin : this) plugin.initialize();
    }
}
