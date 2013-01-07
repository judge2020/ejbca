/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.cesecore.certificates.ca.catoken;


import java.util.Properties;

import org.cesecore.keys.token.CryptoToken;
import org.cesecore.keys.token.PKCS11CryptoTokenTest;
import org.cesecore.util.CryptoProviderTools;
import org.junit.Test;

/**
 * Tests PKCS11 keystore crypto token. To run this test a slot 1 must exist on the hsm, with a user with user pin "userpin1" that can use the slot.
 * 
 * @version $Id$
 */
public class PKCS11CATokenTest extends CATokenTestBase {

    public static final String tokenpin = "userpin1";

    public PKCS11CATokenTest() {
        CryptoProviderTools.installBCProvider();
    }

    @Test
    public void testCATokenRSA() throws Exception {
    	CryptoToken cryptoToken = createPKCS11Token();
        doCaTokenRSA("1024", cryptoToken, getCaTokenProperties("rsatest" + CAToken.DEFAULT_KEYSEQUENCE));
    }

	@Test
    public void testCATokenECC() throws Exception {
    	CryptoToken cryptoToken = createPKCS11Token();
        doCaTokenECC("secp256r1", cryptoToken, getCaTokenProperties("ecctest" + CAToken.DEFAULT_KEYSEQUENCE));
    }

    @Test
    public void testActivateDeactivate() throws Exception {
    	CryptoToken cryptoToken = createPKCS11Token();
    	doActivateDeactivate("1024", cryptoToken, getCaTokenProperties("rsatest" + CAToken.DEFAULT_KEYSEQUENCE));
    }

	@Test
	public void testSaveAndLoad() throws Exception {
    	CryptoToken cryptoToken = createPKCS11Token();
    	doSaveAndLoad("1024", cryptoToken, getCaTokenProperties("rsatest" + CAToken.DEFAULT_KEYSEQUENCE));
	}

	private CryptoToken createPKCS11Token(/*String keyspec*/) {
		CryptoToken cryptoToken = PKCS11CryptoTokenTest.createPKCS11Token();
    	Properties cryptoTokenProperties = cryptoToken.getProperties();
        // Use autoactivation for easy testing
        cryptoTokenProperties.setProperty(CryptoToken.AUTOACTIVATE_PIN_PROPERTY, tokenpin);
        cryptoToken.setProperties(cryptoTokenProperties);
		return cryptoToken;
	}
	
	private Properties getCaTokenProperties(String signAlias) {
	    Properties caTokenProperties = new Properties();
	    caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING, signAlias); // does not exist and never will, will be moved to new keys
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CRLSIGN_STRING, signAlias); // does not exist and never will, will be moved to new keys
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_DEFAULT_STRING, "encryptionKey"); // does not exist but will soon, after first generate
        return caTokenProperties;
	}

    String getProvider() {
    	return PKCS11CryptoTokenTest.getHSMProvider();
    }
}
