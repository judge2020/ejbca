/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.util.dn;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.ejbca.util.CertTools;


/**
 * Tests the StringTools class .
 *
 * @version $Id: TestDnComponents.java,v 1.2 2006-12-04 12:09:10 anatom Exp $
 */
public class TestDnComponents extends TestCase {
    private static Logger log = Logger.getLogger(TestDnComponents.class);

    /**
     * Creates a new TestStringTools object.
     *
     * @param name name
     */
    public TestDnComponents(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        log.debug(">setUp()");
        log.debug("<setUp()");
    }

    protected void tearDown() throws Exception {
        log.debug(">tearDown()");
        log.debug("<tearDown()");
    }

    /**
     * tests stipping whitespace
     *
     * @throws Exception error
     */
    public void test01CheckObjects() throws Exception {
        String[] s = DnComponents.getDnObjects();
        assertEquals(21, s.length);
        assertEquals("unstructuredaddress",s[0]);
        assertEquals("unstructuredname",s[1]);
        assertEquals("dn",s[5]);
        assertEquals("uid",s[6]);
        assertEquals("cn",s[7]);
        assertEquals("t",s[14]);
        assertEquals("c",s[20]);

        String[] s1 = DnComponents.getDnObjectsReverse();
        assertEquals(21, s1.length);
        assertEquals("unstructuredaddress",s1[20]);
        assertEquals("unstructuredname",s1[19]);
        assertEquals("uid",s1[14]);
        assertEquals("cn",s1[13]);
        assertEquals("t",s1[6]);
        assertEquals("c",s1[0]);

        String[] s2 = DnComponents.getDnObjects();
        assertEquals(21, s2.length);
        assertEquals("unstructuredaddress",s2[0]);
        assertEquals("unstructuredname",s2[1]);
        assertEquals("uid",s2[6]);
        assertEquals("cn",s2[7]);
        assertEquals("t",s2[14]);
        assertEquals("c",s2[20]);

    }
    public void test02() {
        String dn = CertTools.stringToBCDNString("uri=fff,CN=oid,C=se");
        System.out.println(dn);
    }

}
