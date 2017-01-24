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
package org.ejbca.core.ejb.ca.caadmin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBException;
import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.operator.OperatorCreationException;
import org.cesecore.CaTestUtils;
import org.cesecore.authentication.tokens.AuthenticationSubject;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.authorization.control.StandardRules;
import org.cesecore.authorization.rules.AccessRuleData;
import org.cesecore.authorization.rules.AccessRuleState;
import org.cesecore.authorization.user.AccessMatchType;
import org.cesecore.authorization.user.AccessUserAspectData;
import org.cesecore.authorization.user.matchvalues.X500PrincipalAccessMatchValue;
import org.cesecore.certificates.ca.CAConstants;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.ca.InvalidAlgorithmException;
import org.cesecore.certificates.ca.X509CA;
import org.cesecore.certificates.ca.X509CAInfo;
import org.cesecore.certificates.ca.catoken.CAToken;
import org.cesecore.certificates.ca.catoken.CATokenConstants;
import org.cesecore.certificates.certificate.IllegalKeyException;
import org.cesecore.certificates.certificate.InternalCertificateStoreSessionRemote;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.certificateprofile.CertificateProfileExistsException;
import org.cesecore.certificates.certificateprofile.CertificateProfileSessionRemote;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.keys.token.CryptoToken;
import org.cesecore.keys.token.CryptoTokenAuthenticationFailedException;
import org.cesecore.keys.token.CryptoTokenManagementSessionRemote;
import org.cesecore.keys.token.CryptoTokenNameInUseException;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.keys.token.SoftCryptoToken;
import org.cesecore.keys.token.p11.exception.NoSuchSlotException;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.SimpleAuthenticationProviderSessionRemote;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.roles.AdminGroupData;
import org.cesecore.roles.RoleExistsException;
import org.cesecore.roles.RoleNotFoundException;
import org.cesecore.roles.management.RoleManagementSessionRemote;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.EJBTools;
import org.cesecore.util.EjbRemoteHelper;
import org.cesecore.util.StringTools;
import org.ejbca.core.ejb.ca.publisher.PublisherProxySessionRemote;
import org.ejbca.core.model.ca.publisher.LdapPublisher;
import org.ejbca.core.model.ca.publisher.PublisherExistsException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * System tests for {@link CAAdminSession}
 * 
 * @version $Id$
 *
 */
public class CaAdminSessionBeanTest {

    private static final Logger log = Logger.getLogger(CaAdminSessionBeanTest.class);

    private CAAdminSessionRemote caAdminSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CAAdminSessionRemote.class);
    private CaSessionRemote caSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class);
    private CertificateProfileSessionRemote certificateProfileSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CertificateProfileSessionRemote.class);
    private PublisherProxySessionRemote publisherProxySession = EjbRemoteHelper.INSTANCE.getRemoteSession(PublisherProxySessionRemote.class,
            EjbRemoteHelper.MODULE_TEST);
    private RoleManagementSessionRemote roleManagementSession = EjbRemoteHelper.INSTANCE.getRemoteSession(RoleManagementSessionRemote.class);
    private InternalCertificateStoreSessionRemote internalCertStoreSession = EjbRemoteHelper.INSTANCE.getRemoteSession(InternalCertificateStoreSessionRemote.class,
            EjbRemoteHelper.MODULE_TEST);

    private AuthenticationToken alwaysAllowToken = new TestAlwaysAllowLocalAuthenticationToken("CaAdminSessionBeanTest");

    private static final String TEST_BC_CERT_CA = "TestBCProviderCertCA";
    
    
    @BeforeClass
    public static void beforeClass() {
        CryptoProviderTools.installBCProviderIfNotAvailable();
    }

    @Test
    public void testGetAuthorizedPublisherIds() throws CertificateParsingException, CryptoTokenOfflineException, OperatorCreationException,
            IOException, RoleExistsException, AuthorizationDeniedException, PublisherExistsException, CAExistsException, CertificateProfileExistsException, CADoesntExistsException {
        //Create a publisher to be attached to a CA that the admin has access to
        LdapPublisher caPublisher = new LdapPublisher();
        final String caPublisherName = "CA_PUBLISHER";
        int caPublisherId = publisherProxySession.addPublisher(alwaysAllowToken, caPublisherName, caPublisher);
        //Create a publisher to be attached to a CA that the admin doesn't have access to
        LdapPublisher unauthorizedCaPublisher = new LdapPublisher();
        final String unauthorizedCaPublisherName = "UNAUTHORIZED_CA_PUBLISHER";
        int unauthorizedCaPublisherId = publisherProxySession.addPublisher(alwaysAllowToken, unauthorizedCaPublisherName, unauthorizedCaPublisher);
        //Create a publisher to be unattached to any CA or certificate profile
        LdapPublisher unattachedPublisher = new LdapPublisher();
        final String unattachedCaPublisherName = "UNATTACHED_PUBLISHER";
        int unattachedCaPublisherId = publisherProxySession.addPublisher(alwaysAllowToken, unattachedCaPublisherName, unattachedPublisher);
        //Create a publisher to be attached to a certificate profile
        LdapPublisher certificateProfilePublisher = new LdapPublisher();
        final String certificateProfilePublisherName = "CERTIFICATE_PROFILE_PUBLISHER";
        int certificateProfilePublisherId = publisherProxySession.addPublisher(alwaysAllowToken, certificateProfilePublisherName, certificateProfilePublisher);
        UnAuthorizedCustomPublisherMock unAuthorizedCustomPublisher = new UnAuthorizedCustomPublisherMock();
        final String unAuthorizedCustomPublisherName = "UNAUTHORIZED_CUSTOM_PUBLISHER";
        int unAuthorizedCustomPublisherId = publisherProxySession.addPublisher(alwaysAllowToken, unAuthorizedCustomPublisherName, unAuthorizedCustomPublisher);
        AuthorizedCustomPublisherMock authorizedCustomPublisher = new AuthorizedCustomPublisherMock();
        final String authorizedCustomPublisherName = "AUTHORIZED_CUSTOM_PUBLISHER";
        int authorizedCustomPublisherId = publisherProxySession.addPublisher(alwaysAllowToken, authorizedCustomPublisherName, authorizedCustomPublisher);
        
        
        
        //Create a CA that admin has access to. Publishers attached to this CA should be included
        X509CA authorizedCa = CaTestUtils.createTestX509CA("CN=PUB_ID_authorizedCa", null, false);
        authorizedCa.setCRLPublishers(new ArrayList<Integer>(Arrays.asList(caPublisherId)));
        caSession.addCA(alwaysAllowToken, authorizedCa);
        //Create a CA that admin doesn't have access to. Publishers attached to this CA should not be included
        X509CA unauthorizedCa = CaTestUtils.createTestX509CA("CN=PUB_ID_unauthorizedCa", null, false);
        unauthorizedCa.setCRLPublishers(new ArrayList<Integer>(Arrays.asList(unauthorizedCaPublisherId)));
        caSession.addCA(alwaysAllowToken, unauthorizedCa);
        //Create a CA that admin has access to, to be attached to a certificate profile. Publishers attached to that Certificate Profile should be included
        X509CA certProfileCa = CaTestUtils.createTestX509CA("CN=PUB_ID_certprofileCa", null, false);
        caSession.addCA(alwaysAllowToken, certProfileCa);
        
        //Set up a certificate profile
        final String certificateProfileName = "testGetAuthorizedPublisherIds";
        CertificateProfile certificateProfile = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        certificateProfile.setAvailableCAs(Arrays.asList(certProfileCa.getCAId()));
        certificateProfile.setPublisherList(Arrays.asList(certificateProfilePublisherId));
        certificateProfileSession.addCertificateProfile(alwaysAllowToken, certificateProfileName, certificateProfile);
        
        //Set up a role for this test
        final String roleName = "testGetAuthorizedPublisherIds";
        AdminGroupData role = roleManagementSession.create(alwaysAllowToken, roleName);
        List<AccessRuleData> accessRules = new ArrayList<AccessRuleData>();
        //Give our admin access to the authorized CA. 
        accessRules.add(new AccessRuleData(roleName, StandardRules.CAACCESS.resource() +  authorizedCa.getCAId(), AccessRuleState.RULE_ACCEPT, false));
        accessRules.add(new AccessRuleData(roleName, StandardRules.CAACCESS.resource() + certProfileCa.getCAId(), AccessRuleState.RULE_ACCEPT, false));
        try {
            role = roleManagementSession.addAccessRulesToRole(alwaysAllowToken, role, accessRules);
        } catch (RoleNotFoundException e2) {
            // NOPMD: Ignore
        }
        List<AccessUserAspectData> subjects = new ArrayList<AccessUserAspectData>();
        //SimpleAuthenticationProviderSession used below will presume that our ad hoc user issued themselves. 
        subjects.add(new AccessUserAspectData(roleName, ("CN=" + roleName).hashCode(), X500PrincipalAccessMatchValue.WITH_COMMONNAME,
                AccessMatchType.TYPE_EQUALCASE, roleName));
        try {
            role = roleManagementSession.addSubjectsToRole(alwaysAllowToken, role, subjects);
        } catch (RoleNotFoundException e) {
            // NOPMD: Ignore
        }
        
        //Create the authentication token we'll be using for this test.
        Set<Principal> principals = new HashSet<Principal>();
        X500Principal p = new X500Principal("CN=" + roleName);
        AuthenticationSubject subject = new AuthenticationSubject(principals, null);
        principals.add(p);
        final SimpleAuthenticationProviderSessionRemote authenticationProvider = EjbRemoteHelper.INSTANCE.getRemoteSession(
                SimpleAuthenticationProviderSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
        AuthenticationToken authenticationToken = authenticationProvider.authenticate(subject);

        try {
            Set<Integer> publisherIds = caAdminSession.getAuthorizedPublisherIds(authenticationToken);
            assertTrue("Publisher attached to an authorized CA was not in list.", publisherIds.contains(Integer.valueOf(caPublisherId)));
            assertFalse("Publisher attached to an unauthorized CA was in list.", publisherIds.contains(Integer.valueOf(unauthorizedCaPublisherId)));
            assertTrue("Unattached publisher was not in list.", publisherIds.contains(Integer.valueOf(unattachedCaPublisherId)));
            assertTrue("Publisher attached to Certificate Profile was not in list.", publisherIds.contains(Integer.valueOf(certificateProfilePublisherId)));
            assertTrue("Authorized custom publisher was not in list.", publisherIds.contains(Integer.valueOf(authorizedCustomPublisherId)));
            assertFalse("Unauthorized custom publisher was in list.", publisherIds.contains(Integer.valueOf(unAuthorizedCustomPublisherId)));         
        } finally {
            //Remove the test role
            try {
                roleManagementSession.remove(alwaysAllowToken, role);
            } catch (RoleNotFoundException e1) {
                // NOPMD: Ignore
            } catch (AuthorizationDeniedException e1) {
                // NOPMD: Ignore
            }

            //Remove the test CAs
            try {
                CaTestUtils.removeCa(alwaysAllowToken, authorizedCa.getCAInfo());
            } catch (AuthorizationDeniedException e) {
                // NOPMD: Ignore
            }
            try {
                CaTestUtils.removeCa(alwaysAllowToken, unauthorizedCa.getCAInfo());
            } catch (AuthorizationDeniedException e) {
                // NOPMD: Ignore
            }
            try {
                CaTestUtils.removeCa(alwaysAllowToken, certProfileCa.getCAInfo());
            } catch (AuthorizationDeniedException e) {
                // NOPMD: Ignore
            }

            //Remove the certificate profile
            certificateProfileSession.removeCertificateProfile(alwaysAllowToken, certificateProfileName);
            
            //Remove the publishers
            publisherProxySession.removePublisher(alwaysAllowToken, caPublisherName);
            publisherProxySession.removePublisher(alwaysAllowToken, unauthorizedCaPublisherName);
            publisherProxySession.removePublisher(alwaysAllowToken, unattachedCaPublisherName);
            publisherProxySession.removePublisher(alwaysAllowToken, certificateProfilePublisherName);
            publisherProxySession.removePublisher(alwaysAllowToken, unAuthorizedCustomPublisherName);
            publisherProxySession.removePublisher(alwaysAllowToken, authorizedCustomPublisherName);
        }
    }
    
    @Test
    public void testBCProviderCertOverRemoteEJB() throws Exception {
        try {
            final KeyPair keypair = KeyTools.genKeys("brainpoolP224r1", AlgorithmConstants.KEYALGORITHM_ECDSA);
            
            final Collection<Certificate> certs = new ArrayList<Certificate>();
            final Certificate brainpoolCert = CertTools.genSelfCert("CN=" + TEST_BC_CERT_CA, 1, null, keypair.getPrivate(), keypair.getPublic(),
                    AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA, true);
            certs.add(brainpoolCert);
            
            caAdminSession.importCACertificate(alwaysAllowToken, TEST_BC_CERT_CA, EJBTools.wrapCertCollection(certs));
            
            final CAInfo cainfo = caSession.getCAInfo(alwaysAllowToken, TEST_BC_CERT_CA);
            final Certificate returnedCert = cainfo.getCertificateChain().iterator().next();
            assertEquals("Returned cert did not match imported cert", brainpoolCert, returnedCert);
        } finally {
            try {
                final CAInfo cainfo = caSession.getCAInfo(alwaysAllowToken, TEST_BC_CERT_CA);
                caSession.removeCA(alwaysAllowToken, cainfo.getCAId());
            } catch (CADoesntExistsException e) {
                // NOPMD ignore
            }
            internalCertStoreSession.removeCertificatesBySubject("CN=" + TEST_BC_CERT_CA);
        }
    }

    @Test
    public void testInvalidKeySpecs() throws InvalidAlgorithmParameterException, CertificateProfileExistsException, AuthorizationDeniedException, CryptoTokenOfflineException,
    CryptoTokenAuthenticationFailedException, CryptoTokenNameInUseException, NoSuchSlotException, InvalidKeyException, CAExistsException, InvalidAlgorithmException,
    CADoesntExistsException {
        final String TEST_NAME = Thread.currentThread().getStackTrace()[1].getMethodName();
        final CryptoTokenManagementSessionRemote cryptoTokenManagementSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CryptoTokenManagementSessionRemote.class);
        final Integer oldCryptoTokenId = cryptoTokenManagementSession.getIdFromName(TEST_NAME);
        if (oldCryptoTokenId != null) {
            cryptoTokenManagementSession.deleteCryptoToken(alwaysAllowToken, oldCryptoTokenId.intValue());
        }
        final String TOKEN_PIN = "foo1234";
        final Properties cryptoTokenProperties = new Properties();
        cryptoTokenProperties.setProperty(CryptoToken.ALLOW_EXTRACTABLE_PRIVATE_KEY, Boolean.TRUE.toString());
        cryptoTokenProperties.setProperty(CryptoToken.AUTOACTIVATE_PIN_PROPERTY, TOKEN_PIN);
        final String KEY_SPEC_RSA = "1024";
        final String KEY_SPEC_EC = "prime256v1";
        final String KEY_ALIAS_RSA = "signKeyRsa";
        final String KEY_ALIAS_EC = "signKeyEc";
        final Properties caTokenProperties = new Properties();
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING, KEY_ALIAS_RSA);
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CRLSIGN_STRING, KEY_ALIAS_RSA);
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_DEFAULT_STRING, KEY_ALIAS_RSA);
        try {
            final int cryptoTokenId = cryptoTokenManagementSession.createCryptoToken(alwaysAllowToken, TEST_NAME, SoftCryptoToken.class.getName(), cryptoTokenProperties, null, TOKEN_PIN.toCharArray());
            final CAToken caToken = new CAToken(cryptoTokenId, caTokenProperties);
            caToken.setSignatureAlgorithm(AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
            caToken.setEncryptionAlgorithm(AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
            caToken.setKeySequence(CAToken.DEFAULT_KEYSEQUENCE);
            caToken.setKeySequenceFormat(StringTools.KEY_SEQUENCE_FORMAT_NUMERIC);
            cryptoTokenManagementSession.createKeyPair(alwaysAllowToken, cryptoTokenId, KEY_ALIAS_RSA, KEY_SPEC_RSA);
            cryptoTokenManagementSession.createKeyPair(alwaysAllowToken, cryptoTokenId, KEY_ALIAS_EC, KEY_SPEC_EC);
            final CertificateProfile certificateProfile = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ROOTCA);
            final int certificateProfileId = certificateProfileSession.addCertificateProfile(alwaysAllowToken, TEST_NAME, certificateProfile);
            final X509CAInfo x509CaInfo = new X509CAInfo("CN="+TEST_NAME, TEST_NAME, CAConstants.CA_ACTIVE, certificateProfileId, "3650d", CAInfo.SELFSIGNED, null, caToken);
            // Test happy path. RSA 1024 bit key. RSA 1024 allowed by certificate profile.
            testInvalidKeySpecsInternal(true, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_RSA},
                    new String[] {CertificateProfile.ANY_EC_CURVE}, new int[] {1024}, KEY_ALIAS_RSA, AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
            // Test failure. RSA 1024 bit key. 1024 bits not allowed by certificate profile.
            testInvalidKeySpecsInternal(false, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_RSA},
                    new String[] {CertificateProfile.ANY_EC_CURVE}, new int[] {2048}, KEY_ALIAS_RSA, AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
            // Test failure. RSA 1024 bit key. RSA not allowed by certificate profile.
            testInvalidKeySpecsInternal(false, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA},
                    new String[] {CertificateProfile.ANY_EC_CURVE}, new int[] {1024}, KEY_ALIAS_RSA, AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
            // Test failure. EC "prime256v1" 256 bit key. EC not allowed by certificate profile.
            testInvalidKeySpecsInternal(false, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_RSA},
                    new String[] {CertificateProfile.ANY_EC_CURVE}, new int[] {256}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test failure. EC "prime256v1" 256 bit key. 256 bits not allowed by certificate profile.
            testInvalidKeySpecsInternal(false, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA},
                    new String[] {CertificateProfile.ANY_EC_CURVE}, new int[] {512}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test failure. EC "prime256v1" (a.k.a "secp256r1") 256 bit key. "prime256v1" not allowed by certificate profile.
            testInvalidKeySpecsInternal(false, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA},
                    new String[] {"secp256k1"}, new int[] {256}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test happy path. EC "prime256v1" 256 bit key. "prime256v1" allowed by certificate profile.
            testInvalidKeySpecsInternal(true, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA},
                    new String[] {"prime256v1"}, new int[] {512}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test happy path. EC "prime256v1" 256 bit key. "prime256v1" and RSA 1024 allowed by certificate profile.
            testInvalidKeySpecsInternal(true, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA, AlgorithmConstants.KEYALGORITHM_RSA},
                    new String[] {"prime256v1"}, new int[] {1024}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test happy path. RSA 1024 bit key. "prime256v1" and RSA 1024 allowed by certificate profile.
            testInvalidKeySpecsInternal(true, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA, AlgorithmConstants.KEYALGORITHM_RSA},
                    new String[] {"prime256v1"}, new int[] {1024}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
            // Test happy path. EC "prime256v1" (a.k.a "secp256r1") 256 bit key. "prime256v1" alias "secp256r1" allowed by certificate profile.
            testInvalidKeySpecsInternal(true, TEST_NAME, x509CaInfo, new String[] {AlgorithmConstants.KEYALGORITHM_ECDSA},
                    new String[] {"secp256r1"}, new int[] {512}, KEY_ALIAS_EC, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
        } finally {
            try {
                caSession.removeCA(alwaysAllowToken, caSession.getCAInfo(alwaysAllowToken, TEST_NAME).getCAId());
            } catch (CADoesntExistsException e) {
                log.debug(e.getMessage());
            }
            cryptoTokenManagementSession.deleteCryptoToken(alwaysAllowToken, cryptoTokenManagementSession.getIdFromName(TEST_NAME));
            certificateProfileSession.removeCertificateProfile(alwaysAllowToken, TEST_NAME);
        }
    }

    private void testInvalidKeySpecsInternal(final boolean expectNoIllegalKeyException, final String certificateProfileName, final CAInfo caInfo,
            final String[] availableKeyAlgorithms, final String[] availableEcCurves, final int[] availableBitLengths, final String keyAlias,
            final String signatureAlgoritm) throws AuthorizationDeniedException,
            CAExistsException, CryptoTokenOfflineException, InvalidAlgorithmException, CADoesntExistsException {
        final CertificateProfile certificateProfile = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ROOTCA);
        certificateProfile.setAvailableKeyAlgorithms(availableKeyAlgorithms);
        certificateProfile.setAvailableEcCurves(availableEcCurves);
        certificateProfile.setAvailableBitLengths(availableBitLengths);
        certificateProfileSession.changeCertificateProfile(alwaysAllowToken, certificateProfileName, certificateProfile);
        caInfo.getCAToken().setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING, keyAlias);
        caInfo.getCAToken().setProperty(CATokenConstants.CAKEYPURPOSE_CRLSIGN_STRING, keyAlias);
        caInfo.getCAToken().setSignatureAlgorithm(signatureAlgoritm);
        try {
            caAdminSession.createCA(alwaysAllowToken, caInfo);
            caSession.removeCA(alwaysAllowToken, caSession.getCAInfo(alwaysAllowToken, caInfo.getName()).getCAId());
            if (!expectNoIllegalKeyException) {
                fail("Validation should not work with invalid key size and/or algoritmh,");
            }
        } catch (EJBException e) {
            if (e.getCause() instanceof IllegalKeyException) {
                if (expectNoIllegalKeyException) {
                    fail("Key algorithm and spec should have been allowed by certificate profile.");
                }
            }
        }
    }
}