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
 
package org.ejbca.ui.cli.ca;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.util.encoders.Hex;
import org.cesecore.certificates.ca.CAInfo;
import org.ejbca.cvc.CardVerifiableCertificate;
import org.ejbca.ui.cli.CliUsernameException;
import org.ejbca.ui.cli.ErrorAdminCommandException;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.ValidityDate;

/**
 * Renews the CA certificate and optionally regenerates the key-pair. This is the CLI equivalent of pushing 
 * the renewal button in EJBCA Admin Web.
 *
 * @author Markus Kilas
 * @version $Id$
 */
public class CaRenewCACommand extends BaseCaAdminCommand {

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZ");
	private static final String NEWLINE = System.getProperty("line.separator");
	
	public String getMainCommand() { return MAINCOMMAND; }
	public String getSubCommand() { return "renewca"; }
	public String getDescription() { return "Renew CA certificate and optionally regenerate keys"; }

    public void execute(String[] args) throws ErrorAdminCommandException {
        try {
            args = parseUsernameAndPasswordFromArgs(args);
        } catch (CliUsernameException e) {
            return;
        }
        if (args.length < 2 || args.length > 5) {
			printUsage();
        	return;
        }
        try {
        	// Bouncy Castle security provider
        	CryptoProviderTools.installBCProvider();
            
        	// Get the CAs info and id
        	String caname = args[1];
            CAInfo cainfo = ejb.getCaSession().getCAInfo(getAdmin(cliUserName, cliPassword), caname);
            if (cainfo == null) {
            	getLogger().error("Error: CA " + caname + " cannot be found");	
            	return;            	
            }
        	
        	boolean regenerateKeys = false;
        	String authCode = null;
        	boolean prompt = false;
        	Date customNotBefore = null;
        	if (args.length > 2) {
        		if ("TRUE".equalsIgnoreCase(args[2])) {
        			regenerateKeys = true;
        		} else if (!"FALSE".equalsIgnoreCase(args[2])) {
        			getLogger().error("Error: Specify true or false for <regenerate keys>");
        			printUsage();
        			return;
        		}
        		regenerateKeys = Boolean.parseBoolean(args[2]);
        	}
        	if (args.length > 3) {
        		if (!"-prompt".equals(args[3])) {
        			authCode = args[3];
        		} else {
        			prompt = true;
        		}
        	}
        	if (args.length > 4) {
        		customNotBefore = ValidityDate.parseAsIso8601(args[4]);
        		if (customNotBefore == null) {
        			getLogger().error("Error: Could not parse date. Use ISO 8601 format. ");
        			return;
        		}
        	}
            
    		final StringBuilder buff = new StringBuilder();
    		buff.append("Renew CA ");
    		buff.append(caname);
    		buff.append(" ");
    		if (regenerateKeys) {
    			buff.append("with a new key-pair");
    		} else {
    			buff.append("with the current key-pair");
    		}
    		if (customNotBefore != null) {
    			buff.append(" and with custom notBefore date: ");
    			buff.append(format.format(customNotBefore));
    		}
    		getLogger().info(buff.toString());
    		
    		getLogger().info("Current certificate: ");
    		final Object oldCertificate = cainfo.getCertificateChain().iterator().next();
            if (oldCertificate instanceof Certificate) {
            	printCertificate((Certificate) oldCertificate);
            } else {
            	getLogger().error("Error: Certificate not found");
            }
    		
    		if (authCode == null && regenerateKeys) {
	            getLogger().info("Enter authorization code to continue: ");
	            authCode = String.valueOf(System.console().readPassword());
    		} else if (prompt) {
    		getLogger().info("Press ENTER to continue: ");
                    System.console().readPassword();
    		}
            
            ejb.getCAAdminSession().renewCA(getAdmin(cliUserName, cliPassword), cainfo.getCAId(), authCode, regenerateKeys, customNotBefore);
            getLogger().info("New certificate created:");
            cainfo = ejb.getCaSession().getCAInfo(getAdmin(cliUserName, cliPassword), caname);
            if (cainfo == null) {
            	getLogger().error("Error: CA " + caname + " cannot be found");	
            	return;            	
            }
            final Object newCertificate = cainfo.getCertificateChain().iterator().next();
            if (newCertificate instanceof Certificate) {
            	printCertificate((Certificate) newCertificate);
            } else {
            	getLogger().error("Error: Certificate not found");
            }
        } catch (Exception e) {
            throw new ErrorAdminCommandException(e);
        }
    }

    private void printUsage() {
    	getLogger().info(new StringBuilder()
    		.append("Description: ").append(getDescription()).append(NEWLINE)
    		.append("Usage: ").append(getCommand()).append(" <CA name> [<regenerate keys>] [<authorization code> | -prompt] [<custom notBefore>]").append(NEWLINE)
    		.append("Example: ").append(getCommand()).append(" ExampleCA1 false -prompt \"2010-09:08 07:06:05+02:00\"").append(NEWLINE)
    		.toString());
    }

    private void printCertificate(final Certificate certificate) {
    	if (certificate instanceof X509Certificate) {
        	final X509Certificate x509 = (X509Certificate) certificate;
        	getLogger().info(new StringBuilder()
        		.append("  Serial number:  ").append(x509.getSerialNumber().toString(16)).append(NEWLINE)
        		.append("  Issuer DN:      ").append(x509.getIssuerX500Principal().getName()).append(NEWLINE)
        		.append("  Subject DN:     ").append(x509.getSubjectX500Principal().getName()).append(NEWLINE)
        		.append("  Not Before:     ").append(format.format(x509.getNotBefore())).append(NEWLINE)
        		.append("  Not After:      ").append(format.format(x509.getNotAfter())).append(NEWLINE)
        		.append("  Subject key id: ").append(computeSubjectKeyIdentifier(x509)).append(NEWLINE)
        		.toString());
        } else if (certificate instanceof CardVerifiableCertificate) {
        	final CardVerifiableCertificate cvc = (CardVerifiableCertificate) certificate;
        	try {
	        	getLogger().info(new StringBuilder()
	        		.append("  ").append(cvc.getCVCertificate().getCertificateBody().getHolderReference().getAsText(false)).append(NEWLINE)
	        		.append("  ").append(cvc.getCVCertificate().getCertificateBody().getAuthorityReference().getAsText(false)).append(NEWLINE)
	        		.append("  Not Before:      ").append(format.format(cvc.getCVCertificate().getCertificateBody().getValidFrom())).append(NEWLINE)
	        		.append("  Not After:       ").append(format.format(cvc.getCVCertificate().getCertificateBody().getValidTo())).append(NEWLINE)
	        		.append("  Public key hash: ").append(computePublicKeyHash(cvc.getPublicKey())).append(NEWLINE)
	        		.toString());
        	} catch (NoSuchFieldException ex) {
        		getLogger().error("Error: Could not read field in CV Certificate: " + ex.getMessage());
    		}
        } else {
        	getLogger().info(new StringBuilder()
	    		.append("  Unknown certificate type:").append(NEWLINE)
	    		.append(certificate.toString())
	    		.toString());
        }
    }
    
    private static String computeSubjectKeyIdentifier(final X509Certificate certificate) {
		try {
			SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
			        new ByteArrayInputStream(certificate.getPublicKey().getEncoded())).readObject());
			SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
	    	return new String(Hex.encode(ski.getKeyIdentifier()));	
		} catch (IOException e) {
			return "n/a";
		}
    }
    
    private static String computePublicKeyHash(final PublicKey publicKey) {
    	final Digest digest = new SHA1Digest();
    	final byte[] hash = new byte[digest.getDigestSize()];
    	final byte[] data = publicKey.getEncoded();
    	digest.update(data, 0, data.length);
    	digest.doFinal(hash, 0);
    	return new String(Hex.encode(hash));
    }
}
