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
package org.ejbca.core.model.util;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;


/**
 * Classes used by TestAlgorithmTools.
 * 
 * @version $Id$
 */
class AlgorithmToolsHelper {
	
	static class MockPublicKey implements PublicKey {
		@Override public String getAlgorithm() { return null; }
		@Override public byte[] getEncoded() { return null; }
		@Override public String getFormat() { return null; }		
	}
	
	static class MockNotSupportedPublicKey extends MockPublicKey {}
	
	static class MockRSAPublicKey extends MockPublicKey implements RSAPublicKey {
		@Override public BigInteger getPublicExponent() { return null; }
		@Override public BigInteger getModulus() { return null; }
	}
	
	static class MockDSAPublicKey extends MockPublicKey implements DSAPublicKey {
		@Override public BigInteger getY() { return null; }
		@Override public DSAParams getParams() { return null; }
	}
	
	static class MockECDSAPublicKey extends MockPublicKey implements ECPublicKey {
		@Override public ECPoint getW() { return null; }
		@Override public ECParameterSpec getParams() { return null; }
	}
}
