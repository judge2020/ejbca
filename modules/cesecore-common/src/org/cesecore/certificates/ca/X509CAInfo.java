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
package org.cesecore.certificates.ca;

import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cesecore.certificates.ca.catoken.CAToken;
import org.cesecore.certificates.ca.extendedservices.ExtendedCAServiceInfo;
import org.cesecore.certificates.certificateprofile.CertificatePolicy;
import org.cesecore.util.CertTools;
import org.cesecore.util.SimpleTime;
import org.cesecore.util.StringTools;


/**
 * Holds non-sensitive information about a X509CA.
 *
 * @version $Id$
 */
public class X509CAInfo extends CAInfo {

	private static final long serialVersionUID = 2L;
	private List<CertificatePolicy> policies;
	private boolean useauthoritykeyidentifier;
	private boolean authoritykeyidentifiercritical;
	private boolean usecrlnumber;
	private boolean crlnumbercritical;
	private String defaultcrldistpoint;
	private String defaultcrlissuer;
	private String defaultocsplocator;
	private String cadefinedfreshestcrl;
	private String subjectaltname;
	private boolean useUTF8PolicyText;
	private boolean usePrintableStringSubjectDN;
	private boolean useLdapDNOrder;
	private boolean useCrlDistributionPointOnCrl;
	private boolean crlDistributionPointOnCrlCritical;
	private String cmpRaAuthSecret = "";
	private List<String> authorityInformationAccess;
	private List<String> certificateAiaDefaultCaIssuerUri;
	private List<String> nameConstraintsPermitted;
	private List<String> nameConstraintsExcluded;
	private String externalCdp;
	private boolean nameChanged;

    /**
     * This constructor can be used when creating a CA.
     * This constructor uses defaults for the fields that are not specified.
     */
    public X509CAInfo(final String subjectdn, final String name, final int status,
            final int certificateProfileId, final String encodedValidity, int signedby, final Collection<Certificate> certificatechain, final CAToken catoken) {
        this(subjectdn,
             name,
             status, // CA status (CAConstants.CA_ACTIVE, etc.)
             new Date(), // update time
             "", // Subject Alternative name
             certificateProfileId, // CA certificate profile
             encodedValidity, null, // Expiretime
             CAInfo.CATYPE_X509, // CA type (X509/CVC)
             signedby, // Signed by CA
             certificatechain, // Certificate chain
             catoken, // CA Token
             "", // Description
             -1, // Revocation reason
             null, // Revocation date
             null, // PolicyId
             24 * SimpleTime.MILLISECONDS_PER_HOUR, // CRLPeriod
             0 * SimpleTime.MILLISECONDS_PER_HOUR, // CRLIssueInterval
             10 * SimpleTime.MILLISECONDS_PER_HOUR, // CRLOverlapTime
             10 * SimpleTime.MILLISECONDS_PER_HOUR, // DeltaCRLPeriod
             new ArrayList<Integer>(),
             new ArrayList<Integer>(),
             true, // Authority Key Identifier
             false, // Authority Key Identifier Critical
             true, // CRL Number
             false, // CRL Number Critical
             null, // defaultcrldistpoint
             null, // defaultcrlissuer
             null, // defaultocsplocator
             null, // CRL Authority Information Access (AIA) extension
             null, // Certificate AIA default CA issuer URI
             null, null, // Name Constraints (permitted/excluded)
             null, // defaultfreshestcrl
             true, // Finish User
             new ArrayList<ExtendedCAServiceInfo>(), // no extended services
             false, // use default utf8 settings
             new HashMap<ApprovalRequestType, Integer>(), //approvals
             false, // Use UTF8 subject DN by default
             true, // Use LDAP DN order by default
             false, // Use CRL Distribution Point on CRL
             false, // CRL Distribution Point on CRL critical
             true, // Include in HealthCheck
             true, // isDoEnforceUniquePublicKeys
             true, // isDoEnforceUniqueDistinguishedName
             false, // isDoEnforceUniqueSubjectDNSerialnumber
             false, // useCertReqHistory
             true, // useUserStorage
             true, // useCertificateStorage
             null, // cmpRaAuthSecret
             false // keepExpiredCertsOnCRL
        );
    }

    /**
     * Constructor that should be used when creating CA and retrieving CA info.
     * Please use the shorter form if you do not need to set all of the values.
     * @param subjectdn the Subject DN of the CA as found in the certificate
     * @param name the name of the CA shown in EJBCA, can be changed by the user
     * @param status the operational status of the CA, one of the constants in {@link #CAConstants}
     * @param updateTime the last time this CA was updated, normally the current date and time
     * @param subjectaltname the Subject Alternative Name (SAN) of the CA, as found in the certificate
     * @param certificateprofileid the ID of the certificate profile for this CA
     * @param encodedValidity the validity of this CA as a human-readable string, e.g. 25y
     * @param expiretime the date when this CA expires
     * @param catype the type of CA, in this case CAInfo.CATYPE_X509
     * @param signedby the id of the CA which signed this CA
     * @param certificatechain the certificate chain containing the CA certificate of this CA
     * @param catoken the CA token for this CA, containing e.g. a reference to the crypto token
     * @param description a text describing this CA
     * @param revocationReason the reason why this CA was revoked, or -1 if not revoked
     * @param revocationDate the date of revocation, or null if not revoked
     * @param policies a policy OID
     * @param crlperiod the CRL validity period in ms
     * @param crlIssueInterval how often in ms the CRLs should be distributed, e.g. 3600000 will generate a new CRL every hour
     * @param crlOverlapTime the validity overlap in ms for a subsequent CRL, e.g. 5000 will generate a CRL 5m before the previous CRL expires
     * @param deltacrlperiod how often Delta CRLs should be distributed
     * @param crlpublishers a collection of publisher IDs for this CA
     * @param keyValidators a collection of key validator IDs for this CA
     * @param useauthoritykeyidentifier
     * @param authoritykeyidentifiercritical
     * @param usecrlnumber
     * @param crlnumbercritical
     * @param defaultcrldistpoint the URI of the default CRL distribution point
     * @param defaultcrlissuer
     * @param defaultocspservicelocator
     * @param authorityInformationAccess
     * @param certificateAiaDefaultCaIssuerUri
     * @param nameConstraintsPermitted a list of name constraints which should be permitted
     * @param nameConstraintsExcluded a list of name constraints which should be excluded
     * @param cadefinedfreshestcrl
     * @param finishuser
     * @param extendedcaserviceinfos
     * @param useUTF8PolicyText
     * @param approvals a map of approval profiles which should be used for different operations
     * @param usePrintableStringSubjectDN
     * @param useLdapDnOrder
     * @param useCrlDistributionPointOnCrl
     * @param crlDistributionPointOnCrlCritical
     * @param includeInHealthCheck enable healthcheck for this CA
     * @param _doEnforceUniquePublicKeys
     * @param _doEnforceUniqueDistinguishedName
     * @param _doEnforceUniqueSubjectDNSerialnumber
     * @param _useCertReqHistory
     * @param _useUserStorage
     * @param _useCertificateStorage
     * @param _cmpRaAuthSecret
     * @param keepExpiredCertsOnCRL
     */
    public X509CAInfo(final String subjectdn, final String name, final int status, final Date updateTime, final String subjectaltname,
            final int certificateprofileid, final String encodedValidity, final Date expiretime, final int catype, final int signedby,
            final Collection<Certificate> certificatechain, final CAToken catoken,
    		final String description, final int revocationReason, final Date revocationDate, final List<CertificatePolicy> policies,
    		final long crlperiod, final long crlIssueInterval, final long crlOverlapTime, final long deltacrlperiod,
    		final Collection<Integer> crlpublishers, final Collection<Integer> keyValidators, final boolean useauthoritykeyidentifier, final boolean authoritykeyidentifiercritical,
    		final boolean usecrlnumber, final boolean crlnumbercritical, final String defaultcrldistpoint, final String defaultcrlissuer,
    		final String defaultocspservicelocator,
    		final List<String> authorityInformationAccess,
    		final List<String> certificateAiaDefaultCaIssuerUri,
    		final List<String> nameConstraintsPermitted, final List<String> nameConstraintsExcluded, final String cadefinedfreshestcrl,
    		final boolean finishuser, final Collection<ExtendedCAServiceInfo> extendedcaserviceinfos,
    		final boolean useUTF8PolicyText, final Map<ApprovalRequestType, Integer> approvals, final boolean usePrintableStringSubjectDN,
    		final boolean useLdapDnOrder, final boolean useCrlDistributionPointOnCrl, final boolean crlDistributionPointOnCrlCritical, final boolean includeInHealthCheck,
    		final boolean _doEnforceUniquePublicKeys, final boolean _doEnforceUniqueDistinguishedName, final boolean _doEnforceUniqueSubjectDNSerialnumber,
    		final boolean _useCertReqHistory, final boolean _useUserStorage, final boolean _useCertificateStorage, final String _cmpRaAuthSecret, final boolean keepExpiredCertsOnCRL) {
        this.subjectdn = CertTools.stringToBCDNString(StringTools.strip(subjectdn));
        this.caid = CertTools.stringToBCDNString(this.subjectdn).hashCode();
        this.name = name;
        this.status = status;
        this.updatetime = updateTime;
        this.encodedValidity = encodedValidity;
        this.expiretime = expiretime;
        this.catype = catype;
        this.signedby = signedby;
        // Due to a bug in Glassfish v1 (fixed in v2), we used to have to make sure all certificates in this
        // Array were of SUNs own provider, using CertTools.SYSTEM_SECURITY_PROVIDER.
        // As of EJBCA 3.9.3 we decided that we don't have to support Glassfish v1 anymore.
		try {
			if (certificatechain != null) {
		        X509Certificate[] certs = certificatechain.toArray(new X509Certificate[certificatechain.size()]);
                List<Certificate> list = CertTools.getCertCollectionFromArray(certs, null);
		        setCertificateChain(list);
			} else {
			    setCertificateChain(null);
			}
		} catch (CertificateException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		}
        this.catoken = catoken;
        this.description = description;
        setRevocationReason(revocationReason);
        this.revocationDate = revocationDate;
        this.policies = policies;
        this.crlperiod = crlperiod;
        this.crlIssueInterval = crlIssueInterval;
        this.crlOverlapTime = crlOverlapTime;
        this.deltacrlperiod = deltacrlperiod;
        this.crlpublishers = crlpublishers;
        this.validators = keyValidators;
        this.useauthoritykeyidentifier = useauthoritykeyidentifier;
        this.authoritykeyidentifiercritical = authoritykeyidentifiercritical;
        this.usecrlnumber = usecrlnumber;
        this.crlnumbercritical = crlnumbercritical;
        this.defaultcrldistpoint = defaultcrldistpoint;
        this.defaultcrlissuer = defaultcrlissuer;
        this.defaultocsplocator = defaultocspservicelocator;
        this.cadefinedfreshestcrl = cadefinedfreshestcrl;
        this.finishuser = finishuser;
        this.subjectaltname = subjectaltname;
        this.certificateprofileid = certificateprofileid;
        this.extendedcaserviceinfos = extendedcaserviceinfos;
        this.useUTF8PolicyText = useUTF8PolicyText;
        setApprovals(approvals);
        this.usePrintableStringSubjectDN = usePrintableStringSubjectDN;
        this.useLdapDNOrder = useLdapDnOrder;
        this.useCrlDistributionPointOnCrl = useCrlDistributionPointOnCrl;
        this.crlDistributionPointOnCrlCritical = crlDistributionPointOnCrlCritical;
        this.includeInHealthCheck = includeInHealthCheck;
        this.doEnforceUniquePublicKeys = _doEnforceUniquePublicKeys;
        this.doEnforceUniqueDistinguishedName = _doEnforceUniqueDistinguishedName;
        this.doEnforceUniqueSubjectDNSerialnumber = _doEnforceUniqueSubjectDNSerialnumber;
        this.useCertReqHistory = _useCertReqHistory;
        this.useUserStorage = _useUserStorage;
        this.useCertificateStorage = _useCertificateStorage;
        setCmpRaAuthSecret(_cmpRaAuthSecret);
        this.keepExpiredCertsOnCRL = keepExpiredCertsOnCRL;
        this.authorityInformationAccess = authorityInformationAccess;
        this.certificateAiaDefaultCaIssuerUri = certificateAiaDefaultCaIssuerUri;
        this.nameConstraintsPermitted = nameConstraintsPermitted;
        this.nameConstraintsExcluded = nameConstraintsExcluded;
    }

    /** Constructor that should be used when updating CA data. */
    public X509CAInfo(final int caid, final String encodedValidity, final CAToken catoken, final String description, final long crlperiod,
            final long crlIssueInterval, final long crlOverlapTime, final long deltacrlperiod, final Collection<Integer> crlpublishers,
            final Collection<Integer> keyValidators, final boolean useauthoritykeyidentifier, final boolean authoritykeyidentifiercritical,
            final boolean usecrlnumber, final boolean crlnumbercritical, final String defaultcrldistpoint, final String defaultcrlissuer,
            final String defaultocspservicelocator, final List<String> crlAuthorityInformationAccess,
            final List<String> certificateAiaDefaultCaIssuerUri, final List<String> nameConstraintsPermitted,
            final List<String> nameConstraintsExcluded, final String cadefinedfreshestcrl, final boolean finishuser,
            final Collection<ExtendedCAServiceInfo> extendedcaserviceinfos, final boolean useUTF8PolicyText,
            final Map<ApprovalRequestType, Integer> approvals, final boolean usePrintableStringSubjectDN, final boolean useLdapDnOrder,
            final boolean useCrlDistributionPointOnCrl, final boolean crlDistributionPointOnCrlCritical, final boolean includeInHealthCheck,
            final boolean _doEnforceUniquePublicKeys, final boolean _doEnforceUniqueDistinguishedName,
            final boolean _doEnforceUniqueSubjectDNSerialnumber, final boolean _useCertReqHistory, final boolean _useUserStorage,
            final boolean _useCertificateStorage, final String _cmpRaAuthSecret, final boolean keepExpiredCertsOnCRL) {
        this.caid = caid;
        this.encodedValidity = encodedValidity;
        this.catoken = catoken;
        this.description = description;
        this.crlperiod = crlperiod;
        this.crlIssueInterval = crlIssueInterval;
        this.crlOverlapTime = crlOverlapTime;
        this.deltacrlperiod = deltacrlperiod;
        this.crlpublishers = crlpublishers;
        this.validators = keyValidators;
        this.useauthoritykeyidentifier = useauthoritykeyidentifier;
        this.authoritykeyidentifiercritical = authoritykeyidentifiercritical;
        this.usecrlnumber = usecrlnumber;
        this.crlnumbercritical = crlnumbercritical;
        this.defaultcrldistpoint = defaultcrldistpoint;
        this.defaultcrlissuer = defaultcrlissuer;
        this.defaultocsplocator = defaultocspservicelocator;
        this.cadefinedfreshestcrl = cadefinedfreshestcrl;
        this.finishuser = finishuser;
        this.extendedcaserviceinfos = extendedcaserviceinfos;
        this.useUTF8PolicyText = useUTF8PolicyText;
        setApprovals(approvals);
        this.usePrintableStringSubjectDN = usePrintableStringSubjectDN;
        this.useLdapDNOrder = useLdapDnOrder;
        this.useCrlDistributionPointOnCrl = useCrlDistributionPointOnCrl;
        this.crlDistributionPointOnCrlCritical = crlDistributionPointOnCrlCritical;
        this.includeInHealthCheck = includeInHealthCheck;
        this.doEnforceUniquePublicKeys = _doEnforceUniquePublicKeys;
        this.doEnforceUniqueDistinguishedName = _doEnforceUniqueDistinguishedName;
        this.doEnforceUniqueSubjectDNSerialnumber = _doEnforceUniqueSubjectDNSerialnumber;
        this.useCertReqHistory = _useCertReqHistory;
        this.useUserStorage = _useUserStorage;
        this.useCertificateStorage = _useCertificateStorage;
        setCmpRaAuthSecret(_cmpRaAuthSecret);
        this.keepExpiredCertsOnCRL = keepExpiredCertsOnCRL;
        this.authorityInformationAccess = crlAuthorityInformationAccess;
        this.certificateAiaDefaultCaIssuerUri = certificateAiaDefaultCaIssuerUri;
        this.nameConstraintsPermitted = nameConstraintsPermitted;
        this.nameConstraintsExcluded = nameConstraintsExcluded;
    }

  public List<CertificatePolicy> getPolicies() {
	  return this.policies;
  }
  public void setPolicies(final List<CertificatePolicy> policies) {
      this.policies = policies;
  }
  public boolean getUseCRLNumber(){ return usecrlnumber;}
  public void setUseCRLNumber(boolean usecrlnumber){ this.usecrlnumber=usecrlnumber;}

  public boolean getCRLNumberCritical(){ return crlnumbercritical;}
  public void setCRLNumberCritical(boolean crlnumbercritical){ this.crlnumbercritical=crlnumbercritical;}

  public boolean getUseAuthorityKeyIdentifier(){ return useauthoritykeyidentifier;}
  public void setUseAuthorityKeyIdentifier(boolean useauthoritykeyidentifier) {this.useauthoritykeyidentifier=useauthoritykeyidentifier;}

  public boolean getAuthorityKeyIdentifierCritical(){ return authoritykeyidentifiercritical;}
  public void setAuthorityKeyIdentifierCritical(boolean authoritykeyidentifiercritical)
                {this.authoritykeyidentifiercritical=authoritykeyidentifiercritical;}

  public String getDefaultCRLDistPoint(){ return defaultcrldistpoint; }

  public void setDefaultCRLDistPoint(String defaultCRLDistPoint) {
      this.defaultcrldistpoint = defaultCRLDistPoint;
  }

  public String getDefaultCRLIssuer(){ return defaultcrlissuer; }
  public void setDefaultCRLIssuer(String defaultcrlissuer) {
      this.defaultcrlissuer = defaultcrlissuer;
  }

  public String getDefaultOCSPServiceLocator(){ return defaultocsplocator; }
  public void setDefaultOCSPServiceLocator(String defaultocsplocator) {
      this.defaultocsplocator = defaultocsplocator;
  }

  public String getCADefinedFreshestCRL(){ return this.cadefinedfreshestcrl; }

  public void setCADefinedFreshestCRL(String cADefinedFreshestCRL) {
      this.cadefinedfreshestcrl = cADefinedFreshestCRL;
  }

  public String getSubjectAltName(){ return subjectaltname; }
  public void setSubjectAltName(final String subjectaltname) {
      this.subjectaltname = subjectaltname;
  }
  public boolean getUseUTF8PolicyText() { return useUTF8PolicyText; }
  public void setUseUTF8PolicyText(final boolean useUTF8PolicyText) {
      this.useUTF8PolicyText = useUTF8PolicyText;
  }

  public boolean getUsePrintableStringSubjectDN() { return usePrintableStringSubjectDN; }
  public void setUsePrintableStringSubjectDN(final boolean usePrintableStringSubjectDN) {
      this.usePrintableStringSubjectDN = usePrintableStringSubjectDN;
  }

  public boolean getUseLdapDnOrder() { return useLdapDNOrder; }
  public void setUseLdapDnOrder(final boolean useLdapDNOrder) {
      this.useLdapDNOrder = useLdapDNOrder;
  }

  public boolean getUseCrlDistributionPointOnCrl() {
      return this.useCrlDistributionPointOnCrl;
  }

  public void setUseCrlDistributionPointOnCrl(boolean useCrlDistributionPointOnCrl) {
      this.useCrlDistributionPointOnCrl = useCrlDistributionPointOnCrl;
  }

  public boolean getCrlDistributionPointOnCrlCritical() {
      return this.crlDistributionPointOnCrlCritical;
  }
  public void setCrlDistributionPointOnCrlCritical(boolean crlDistributionPointOnCrlCritical) {
      this.crlDistributionPointOnCrlCritical = crlDistributionPointOnCrlCritical;
  }

  public String getCmpRaAuthSecret() { return cmpRaAuthSecret; }
  public void setCmpRaAuthSecret(String cmpRaAuthSecret) { this.cmpRaAuthSecret = cmpRaAuthSecret == null ? "" : cmpRaAuthSecret; }

    public List<String> getAuthorityInformationAccess() {
        return authorityInformationAccess;
    }

    public void setAuthorityInformationAccess(List<String> list) {
        this.authorityInformationAccess = list;
    }

    /** @return the certificateAiaDefaultCaIssuerUri */
    public List<String> getCertificateAiaDefaultCaIssuerUri() {
        return certificateAiaDefaultCaIssuerUri;
    }

    /** @param list the certificateAiaDefaultCaIssuerUri to set */
    public void setCertificateAiaDefaultCaIssuerUri(List<String> list) {
        this.certificateAiaDefaultCaIssuerUri = list;
    }

    /** @return a list of encoded names of the permitted names in issued certificates */
    public List<String> getNameConstraintsPermitted() {
        return nameConstraintsPermitted;
    }

    public void setNameConstraintsPermitted(List<String> encodedNames) {
        nameConstraintsPermitted = encodedNames;
    }

    /** @return a list of encoded names of the forbidden names in issued certificates */
    public List<String> getNameConstraintsExcluded() {
        return nameConstraintsExcluded;
    }

    public void setNameConstraintsExcluded(List<String> encodedNames) {
        nameConstraintsExcluded = encodedNames;
    }

    /** @return what should be a String formatted URL pointing to an external CA's CDP. */
    public String getExternalCdp() {
        return externalCdp;
    }

    /** Set what should be a String formatted URL pointing to an external CA's CDP. */
    public void setExternalCdp(final String externalCdp) {
        this.externalCdp = externalCdp;
    }

    /** @return true if CA has undergone through name change at some renewal process, otherwise false. */
    public boolean getNameChanged() {
        return nameChanged;
    }

    /** NameChanged attribute should only be set when X509CA is retrieved from DB */
    void setNameChanged(final boolean value){
        nameChanged = value;
    }
}
