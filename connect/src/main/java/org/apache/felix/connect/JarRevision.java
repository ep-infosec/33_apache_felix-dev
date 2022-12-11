/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.connect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class JarRevision implements Revision
{
    private final long m_lastModified;
    private final JarFile m_jar;
    private final URL m_url;
    private final String m_urlString;
    private final String m_prefix;

    public JarRevision(JarFile jar, URL url, String prefix, long lastModified)
    {
        m_jar = jar;
        m_url = url;
        m_urlString = m_url.toExternalForm();
        m_prefix = prefix;
        if (lastModified > 0)
        {
            m_lastModified = lastModified;
        }
        else
        {
            m_lastModified = System.currentTimeMillis();
        }
    }

    @Override
    public long getLastModified()
    {
        return m_lastModified;
    }

    public Enumeration<String> getEntries()
    {
        return new EntriesEnumeration(m_jar.entries(), m_prefix);
    }

    @Override
    public URL getEntry(final String entryName)
    {
        try
        {
            if ("/".equals(entryName) || "".equals(entryName) || " ".equals(entryName))
            {
                return new URL("jar:" + m_urlString + "!/" + ((m_prefix == null) ? "" : m_prefix));
            }
            if (entryName != null)
            {
                final String target = ((entryName.startsWith("/")) ? entryName.substring(1) : entryName);
                final JarEntry entry = m_jar.getJarEntry(((m_prefix == null) ? "" : m_prefix) + target);
                if (entry != null)
                {
                    final String newUrl;
                    if ( m_urlString.startsWith("jar:") ) {
                        newUrl =  m_urlString + "!/" + ((m_prefix == null) ? "" : m_prefix) + target;
                    } else {
                        newUrl = "jar:" + m_urlString + "!/" + ((m_prefix == null) ? "" : m_prefix) + target;
                    }

                    URL result = new URL(null, newUrl, new URLStreamHandler()
                    {
                        protected URLConnection openConnection(final URL u) throws IOException
                        {
                            return new java.net.JarURLConnection(new URL(u.toExternalForm()))
                            {
                                public JarFile getJarFile()
                                {
                                    return m_jar;
                                }

                                public void connect() throws IOException
                                {
                                }

                                @Override
                                public String getEntryName()
                                {
                                    String path = u.getPath();
                                    return path.startsWith("/") ? path.substring(1) : path;
                                }

                                @Override
                                public JarEntry getJarEntry() throws IOException
                                {
                                    return getJarFile().getJarEntry(getEntryName());
                                }

                                public InputStream getInputStream() throws IOException
                                {
                                    JarEntry entry = getJarEntry();
                                    if (entry == null) {
                                        throw new FileNotFoundException(u.toExternalForm());
                                    }
                                    return getJarFile().getInputStream(entry);
                                }

                                @Override
                                public long getContentLengthLong()
                                {
                                    try
                                    {
                                        return getJarEntry().getSize();
                                    }
                                    catch (IOException e)
                                    {
                                        return -1;
                                    }
                                }
                            };
                        }

                        @Override
                        protected void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref)
                        {
                            super.setURL(u, protocol, path.contains("!/") ? path.substring(0, path.lastIndexOf("!/") + 1) : host, port, authority, userInfo, path.contains("!/") ? path.substring(path.lastIndexOf("!/") + 1) : path, query, ref);
                        }

                        @Override
                        protected String toExternalForm(URL u)
                        {
                            return u.getHost() != null && u.getHost().contains("!") ? "jar:" + u.getHost() + u.getPath() : super.toExternalForm(u);
                        }
                    });
                    return result;
                }
                else
                {
                    if (entryName.endsWith("/"))
                    {
                        return new URL("jar:" + m_urlString + "!/" + ((m_prefix == null) ? "" : m_prefix) + target);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;

    }

}
