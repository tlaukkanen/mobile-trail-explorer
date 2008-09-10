/*
 * HttpAbstractUtil.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.substanceofcode.util;

/**
 * HttpAbstractUtil
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public abstract class HttpAbstractUtil {

    protected static String cookie;
    protected static String username;
    protected static String password;
    
    public static String getCookie() {
        return cookie;
    }
    
    public static void setCookie(String value) {
        cookie = value;
    }
    
    public static void setBasicAuthentication(String username, String password) {
        HttpAbstractUtil.username = username;
        HttpAbstractUtil.password = password;
    }
    
    /** Creates a new instance of HttpAbstractUtil */
    public HttpAbstractUtil() {
    }

}
