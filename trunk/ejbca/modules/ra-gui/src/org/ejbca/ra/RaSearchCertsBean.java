/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ra;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.certificate.CertificateConstants;
import org.cesecore.certificates.certificate.CertificateDataWrapper;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.util.CertTools;
import org.cesecore.util.ValidityDate;
import org.ejbca.core.model.era.RaCertificateSearchRequest;
import org.ejbca.core.model.era.RaCertificateSearchResponse;
import org.ejbca.core.model.era.RaMasterApiProxyBeanLocal;

/**
 * Backing bean for Search Certificates page. 
 * 
 * @version $Id$
 */
@ManagedBean
@ViewScoped
public class RaSearchCertsBean implements Serializable {

    public class RaSearchCertificate {
        private final String fingerprint;
        private final String username;
        private final String serialnumber;
        private final String serialnumberRaw;
        private final String subjectDn;
        private final String subjectAn;
        private final String eepName;
        private final String cpName;
        private final String caName;
        private final String created;
        private final String expires;
        private final int status;
        private final int revocationReason;
        private String updated;
        public RaSearchCertificate(final CertificateDataWrapper cdw) {
            this.fingerprint = cdw.getCertificateData().getFingerprint();
            this.serialnumber = CertTools.getSerialNumberAsString(cdw.getCertificate());
            this.serialnumberRaw = cdw.getCertificateData().getSerialNumber();
            this.username = cdw.getCertificateData().getUsername();
            this.subjectDn = cdw.getCertificateData().getSubjectDN();
            this.subjectAn = CertTools.getSubjectAlternativeName(cdw.getCertificate());
            this.cpName = String.valueOf(cpIdToNameMap.get(cdw.getCertificateData().getCertificateProfileId()));
            this.eepName = String.valueOf(eepIdToNameMap.get(cdw.getCertificateData().getEndEntityProfileIdOrZero()));
            this.caName = String.valueOf(caSubjectToNameMap.get(cdw.getCertificateData().getIssuerDN()));
            this.created = ValidityDate.formatAsISO8601ServerTZ(CertTools.getNotBefore(cdw.getCertificate()).getTime(), TimeZone.getDefault());
            this.expires = ValidityDate.formatAsISO8601ServerTZ(cdw.getCertificateData().getExpireDate(), TimeZone.getDefault());
            this.status = cdw.getCertificateData().getStatus();
            this.revocationReason = cdw.getCertificateData().getRevocationReason();
            if (status==CertificateConstants.CERT_ARCHIVED || status==CertificateConstants.CERT_REVOKED) {
                this.updated = ValidityDate.formatAsISO8601ServerTZ(cdw.getCertificateData().getRevocationDate(), TimeZone.getDefault());
            } else {
                this.updated = ValidityDate.formatAsISO8601ServerTZ(cdw.getCertificateData().getUpdateTime(), TimeZone.getDefault());
            }
        }
        public String getFingerprint() { return fingerprint; }
        public String getSerialnumber() { return serialnumber; }
        public String getSerialnumberRaw() { return serialnumberRaw; }
        public String getUsername() { return username; }
        public String getSubjectDn() { return subjectDn; }
        public String getSubjectAn() { return subjectAn; }
        public String getCaName() { return caName; }
        public String getCpName() {
            return cpName;
        }
        public String getEepName() {
            return eepName;
        }
        public String getCreated() { return created; }
        public String getExpires() { return expires; }
        public boolean isActive() { return status==CertificateConstants.CERT_ACTIVE || status==CertificateConstants.CERT_NOTIFIEDABOUTEXPIRATION; }
        public String getStatus() {
            switch (status) {
            case CertificateConstants.CERT_ACTIVE:
            case CertificateConstants.CERT_NOTIFIEDABOUTEXPIRATION:
                return raLocaleBean.getMessage("search_certs_page_status_active");
            case CertificateConstants.CERT_ARCHIVED:
            case CertificateConstants.CERT_REVOKED:
                return raLocaleBean.getMessage("search_certs_page_status_revoked_"+revocationReason);
            }
            return raLocaleBean.getMessage("search_certs_page_status_other");
        }
        public String getUpdated() { return updated; }
    }
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RaSearchCertsBean.class);

    @EJB
    private RaMasterApiProxyBeanLocal raMasterApiProxyBean;

    @ManagedProperty(value="#{raAuthenticationBean}")
    private RaAuthenticationBean raAuthenticationBean;
    public void setRaAuthenticationBean(final RaAuthenticationBean raAuthenticationBean) { this.raAuthenticationBean = raAuthenticationBean; }

    @ManagedProperty(value="#{raLocaleBean}")
    private RaLocaleBean raLocaleBean;
    public void setRaLocaleBean(final RaLocaleBean raLocaleBean) { this.raLocaleBean = raLocaleBean; }

    private final List<RaSearchCertificate> resultsFiltered = new ArrayList<>();
    private Map<Integer,String> eepIdToNameMap = null;
    private Map<Integer,String> cpIdToNameMap = null;
    private Map<String,String> caSubjectToNameMap = new HashMap<>();
    private List<SelectItem> availableEeps = new ArrayList<>();
    private List<SelectItem> availableCps = new ArrayList<>();
    private List<SelectItem> availableCas = new ArrayList<>();

    private RaCertificateSearchRequest stagedRequest = new RaCertificateSearchRequest();
    private RaCertificateSearchRequest lastExecutedRequest = null;
    private RaCertificateSearchResponse lastExecutedResponse = null;

    private String expiresAfter = "";
    private String expiresBefore = "";
    private String updatedAfter = "";
    private String updatedBefore = "";

    public String getGenericSearchString() { return stagedRequest.getGenericSearchString(); }
    public void setGenericSearchString(final String genericSearchString) { stagedRequest.setGenericSearchString(genericSearchString); }
    
    public void searchAndFilterAction() {
        searchAndFilterCommon();
    }

    public void searchAndFilterAjaxListener(final AjaxBehaviorEvent event) {
        searchAndFilterCommon();
    }
    
    private void searchAndFilterCommon() {
        final int compared = stagedRequest.compareTo(lastExecutedRequest);
        boolean search = compared>0;
        if (compared<=0 && lastExecutedResponse!=null) {
            // More narrow search → filter and check if there are sufficient results left
            log.info("DEVELOP: More narrow or same → filter");
            filterTransformSort();
            // Check if there are sufficient results to fill screen and search for more
            if (resultsFiltered.size()<lastExecutedRequest.getMaxResults() && lastExecutedResponse.isMightHaveMoreResults()) {
                log.info("DEVELOP: Trying to load more results since filter left too few results");
                search = true;
            } else {
                search = false;
            }
        }
        if (search) {
            // Wider search → Query back-end
            log.info("DEVELOP: Wider → Query");
            lastExecutedResponse = raMasterApiProxyBean.searchForCertificates(raAuthenticationBean.getAuthenticationToken(), stagedRequest);
            lastExecutedRequest = stagedRequest;
            stagedRequest = new RaCertificateSearchRequest(stagedRequest);
            filterTransformSort();
        }
    }

    private void filterTransformSort() {
        resultsFiltered.clear();
        if (lastExecutedResponse != null) {
            for (final CertificateDataWrapper cdw : lastExecutedResponse.getCdws()) {
                // ...we don't filter if the requested maxResults is lower than the search request
                if (!stagedRequest.getGenericSearchString().isEmpty() && (
                        (cdw.getCertificateData().getUsername() == null || !cdw.getCertificateData().getUsername().contains(stagedRequest.getGenericSearchString())) &&
                        (cdw.getCertificateData().getSubjectDN() == null || !cdw.getCertificateData().getSubjectDN().contains(stagedRequest.getGenericSearchString())))) {
                    continue;
                }
                /*
                if (!stagedRequest.getEepIds().isEmpty() && !stagedRequest.getEepIds().contains(cdw.getCertificateData().getEndEntityProfileIdOrZero())) {
                    continue;
                }
                */
                if (!stagedRequest.getCpIds().isEmpty() && !stagedRequest.getCpIds().contains(cdw.getCertificateData().getCertificateProfileId())) {
                    continue;
                }
                if (!stagedRequest.getCaIds().isEmpty() && !stagedRequest.getCaIds().contains(cdw.getCertificateData().getIssuerDN().hashCode())) {
                    continue;
                }
                if (stagedRequest.getExpiresAfter()<Long.MAX_VALUE) {
                    if (cdw.getCertificateData().getExpireDate()<stagedRequest.getExpiresAfter()) {
                        continue;
                    }
                }
                if (stagedRequest.getExpiresBefore()>0L) {
                    if (cdw.getCertificateData().getExpireDate()>stagedRequest.getExpiresBefore()) {
                        continue;
                    }
                }
                if (stagedRequest.getUpdatedAfter()<Long.MAX_VALUE) {
                    if (cdw.getCertificateData().getUpdateTime()<stagedRequest.getUpdatedAfter()) {
                        continue;
                    }
                }
                if (stagedRequest.getUpdatedBefore()>0L) {
                    if (cdw.getCertificateData().getUpdateTime()>stagedRequest.getUpdatedBefore()) {
                        continue;
                    }
                }
                if (!stagedRequest.getStatuses().isEmpty() && !stagedRequest.getStatuses().contains(cdw.getCertificateData().getStatus())) {
                    continue;
                }
                if (!stagedRequest.getRevocationReasons().isEmpty() && !stagedRequest.getRevocationReasons().contains(cdw.getCertificateData().getRevocationReason())) {
                    continue;
                }
                // if (this or that) { ...
                resultsFiltered.add(new RaSearchCertificate(cdw));
            }
            sort();
        }
    }

    private void sort() {
        Collections.sort(resultsFiltered, new Comparator<RaSearchCertificate>() {
            @Override
            public int compare(RaSearchCertificate o1, RaSearchCertificate o2) {
                switch (sortBy) {
                case EEP:
                    return o1.eepName.compareTo(o2.eepName) * (sortAscending ? 1 : -1);
                case CP:
                    return o1.cpName.compareTo(o2.cpName) * (sortAscending ? 1 : -1);
                case CA:
                    return o1.caName.compareTo(o2.caName) * (sortAscending ? 1 : -1);
                case SERIALNUMBER:
                    return o1.serialnumber.compareTo(o2.serialnumber) * (sortAscending ? 1 : -1);
                case SUBJECT:
                    return (o1.subjectDn+o1.subjectAn).compareTo(o2.subjectDn+o2.subjectAn) * (sortAscending ? 1 : -1);
                case EXPIRATION:
                    return o1.expires.compareTo(o2.expires) * (sortAscending ? 1 : -1);
                case STATUS:
                    return o1.getStatus().compareTo(o2.getStatus()) * (sortAscending ? 1 : -1);
                case USERNAME:
                default:
                    return o1.username.compareTo(o2.username) * (sortAscending ? 1 : -1);
                }
            }
        });
    }

    private enum SortOrder { EEP, CP, CA, SERIALNUMBER, SUBJECT, USERNAME, EXPIRATION, STATUS };
    
    private SortOrder sortBy = SortOrder.USERNAME;
    private boolean sortAscending = true;
    
    public String getSortedByEep() { return getSortedBy(SortOrder.EEP); }
    public void sortByEep() { sortBy(SortOrder.EEP, true); }
    public String getSortedByCp() { return getSortedBy(SortOrder.CP); }
    public void sortByCp() { sortBy(SortOrder.CP, true); }
    public String getSortedByCa() { return getSortedBy(SortOrder.CA); }
    public void sortByCa() { sortBy(SortOrder.CA, true); }
    public String getSortedBySerialNumber() { return getSortedBy(SortOrder.SERIALNUMBER); }
    public void sortBySerialNumber() { sortBy(SortOrder.SERIALNUMBER, true); }
    public String getSortedBySubject() { return getSortedBy(SortOrder.SUBJECT); }
    public void sortBySubject() { sortBy(SortOrder.SUBJECT, true); }
    public String getSortedByExpiration() { return getSortedBy(SortOrder.EXPIRATION); }
    public void sortByExpiration() { sortBy(SortOrder.EXPIRATION, false); }
    public String getSortedByStatus() { return getSortedBy(SortOrder.STATUS); }
    public void sortByStatus() { sortBy(SortOrder.STATUS, true); }
    public String getSortedByUsername() { return getSortedBy(SortOrder.USERNAME); }
    public void sortByUsername() { sortBy(SortOrder.USERNAME, true); }

    private String getSortedBy(final SortOrder sortOrder) {
        if (sortBy.equals(sortOrder)) {
            return sortAscending ? "\u25bc" : "\u25b2";
        }
        return "";
    }
    private void sortBy(final SortOrder sortOrder, final boolean defaultAscending) {
        if (sortBy.equals(sortOrder)) {
            sortAscending = !sortAscending;
        } else {
            sortAscending = defaultAscending;
        }
        this.sortBy = sortOrder;
        sort();
    }
    
    public boolean isMoreResultsAvailable() {
        return lastExecutedResponse!=null && lastExecutedResponse.isMightHaveMoreResults();
    }

    private boolean moreOptions = false;
    public boolean isMoreOptions() { return moreOptions; };

    public void moreOptionsAction() {
        moreOptions = !moreOptions;
        // Reset any criteria in the advanced section
        stagedRequest.setMaxResults(RaCertificateSearchRequest.DEFAULT_MAX_RESULTS);
        stagedRequest.setExpiresAfter(Long.MAX_VALUE);
        stagedRequest.setExpiresBefore(0L);
        stagedRequest.setUpdatedAfter(Long.MAX_VALUE);
        stagedRequest.setUpdatedBefore(0L);
        expiresAfter = "";
        expiresBefore = "";
        updatedAfter = "";
        updatedBefore = "";
        searchAndFilterCommon();
    }

    public int getCriteriaMaxResults() { return stagedRequest.getMaxResults(); }
    public void setCriteriaMaxResults(final int criteriaMaxResults) { stagedRequest.setMaxResults(criteriaMaxResults); }
    public List<Integer> getAvailableMaxResults() {
        return Arrays.asList(new Integer[]{ RaCertificateSearchRequest.DEFAULT_MAX_RESULTS, 50, 100, 200, 400});
    }

    public int getCriteriaEepId() {
        return stagedRequest.getEepIds().isEmpty() ? 0 : stagedRequest.getEepIds().get(0);
    }
    public void setCriteriaEepId(final int criteriaEepId) {
        if (criteriaEepId==0) {
            stagedRequest.setEepIds(new ArrayList<Integer>());
        } else {
            stagedRequest.setEepIds(new ArrayList<>(Arrays.asList(new Integer[]{ criteriaEepId })));
        }
    }
    public boolean isOnlyOneEepAvailable() { return getAvailableEeps().size()==1; }
    public List<SelectItem> getAvailableEeps() {
        if (availableEeps.isEmpty()) {
            eepIdToNameMap = raMasterApiProxyBean.getAuthorizedEndEntityProfileIdsToNameMap(raAuthenticationBean.getAuthenticationToken());
            availableEeps.add(new SelectItem(0, raLocaleBean.getMessage("search_certs_page_criteria_eep_optionany")));
            for (final Entry<Integer,String> entry : getAsSortedByValue(eepIdToNameMap.entrySet())) {
                availableEeps.add(new SelectItem(entry.getKey(), "- " + entry.getValue()));
            }
        }
        return availableEeps;
    }

    public int getCriteriaCpId() {
        return stagedRequest.getCpIds().isEmpty() ? 0 : stagedRequest.getCpIds().get(0);
    }
    public void setCriteriaCpId(final int criteriaCpId) {
        if (criteriaCpId==0) {
            stagedRequest.setCpIds(new ArrayList<Integer>());
        } else {
            stagedRequest.setCpIds(new ArrayList<>(Arrays.asList(new Integer[]{ criteriaCpId })));
        }
    }
    public boolean isOnlyOneCpAvailable() { return getAvailableCps().size()==1; }
    public List<SelectItem> getAvailableCps() {
        if (availableCps.isEmpty()) {
            cpIdToNameMap = raMasterApiProxyBean.getAuthorizedCertificateProfileIdsToNameMap(raAuthenticationBean.getAuthenticationToken());
            availableCps.add(new SelectItem(0, raLocaleBean.getMessage("search_certs_page_criteria_cp_optionany")));
            for (final Entry<Integer,String> entry : getAsSortedByValue(cpIdToNameMap.entrySet())) {
                availableCps.add(new SelectItem(entry.getKey(), "- " + entry.getValue()));
            }
        }
        return availableCps;
    }

    public int getCriteriaCaId() {
        return stagedRequest.getCaIds().isEmpty() ? 0 : stagedRequest.getCaIds().get(0);
    }
    public void setCriteriaCaId(int criteriaCaId) {
        if (criteriaCaId==0) {
            stagedRequest.setCaIds(new ArrayList<Integer>());
        } else {
            stagedRequest.setCaIds(new ArrayList<>(Arrays.asList(new Integer[]{ criteriaCaId })));
        }
    }
    public boolean isOnlyOneCaAvailable() { return getAvailableCas().size()==1; }
    public List<SelectItem> getAvailableCas() {
        if (availableCas.isEmpty()) {
            final List<CAInfo> caInfos = new ArrayList<>(raMasterApiProxyBean.getAuthorizedCas(raAuthenticationBean.getAuthenticationToken()));
            Collections.sort(caInfos, new Comparator<CAInfo>() {
                @Override
                public int compare(final CAInfo caInfo1, final CAInfo caInfo2) {
                    return caInfo1.getName().compareTo(caInfo2.getName());
                }
            });
            for (final CAInfo caInfo : caInfos) {
                caSubjectToNameMap.put(caInfo.getSubjectDN(), caInfo.getName());
            }
            availableCas.add(new SelectItem(0, raLocaleBean.getMessage("search_certs_page_criteria_ca_optionany")));
            for (final CAInfo caInfo : caInfos) {
                availableCas.add(new SelectItem(caInfo.getCAId(), "- " + caInfo.getName()));
            }
        }
        return availableCas;
    }

    public String getExpiresAfter() {
        return getDateAsString(expiresAfter, stagedRequest.getExpiresAfter(), Long.MAX_VALUE);
    }
    public void setExpiresAfter(final String expiresAfter) {
        this.expiresAfter = expiresAfter;
        stagedRequest.setExpiresAfter(parseDateAndUseDefaultOnFail(expiresAfter, Long.MAX_VALUE));
    }
    public String getExpiresBefore() {
        return getDateAsString(expiresBefore, stagedRequest.getExpiresBefore(), 0L);
    }
    public void setExpiresBefore(final String expiresBefore) {
        this.expiresBefore = expiresBefore;
        stagedRequest.setExpiresBefore(parseDateAndUseDefaultOnFail(expiresBefore, 0L));
    }
    public String getUpdatedAfter() {
        return getDateAsString(updatedAfter, stagedRequest.getUpdatedAfter(), Long.MAX_VALUE);
    }
    public void setUpdatedAfter(final String updatedAfter) {
        this.updatedAfter = updatedAfter;
        stagedRequest.setUpdatedAfter(parseDateAndUseDefaultOnFail(updatedAfter, Long.MAX_VALUE));
    }
    public String getUpdatedBefore() {
        return getDateAsString(updatedBefore, stagedRequest.getUpdatedBefore(), 0L);
    }
    public void setUpdatedBefore(final String updatedBefore) {
        this.updatedBefore = updatedBefore;
        stagedRequest.setUpdatedBefore(parseDateAndUseDefaultOnFail(updatedBefore, 0L));
    }

    /** @return the current value if the staged request value if the default value */
    private String getDateAsString(final String stagedValue, final long value, final long defaultValue) {
        if (value==defaultValue) {
            return stagedValue;
        }
        return ValidityDate.formatAsISO8601ServerTZ(value, TimeZone.getDefault());
    }
    /** @return the staged request value if it is a parsable date and the default value otherwise */
    private long parseDateAndUseDefaultOnFail(final String input, final long defaultValue) {
        if (!input.trim().isEmpty()) {
            try {
                return ValidityDate.parseAsIso8601(input).getTime();
            } catch (ParseException e) {
                raLocaleBean.addMessageWarn("search_certs_page_warn_invaliddate");
            }
        }
        return defaultValue;
    }

    public String getCriteriaStatus() {
        final StringBuilder sb = new StringBuilder();
        final List<Integer> statuses = stagedRequest.getStatuses();
        final List<Integer> revocationReasons = stagedRequest.getRevocationReasons();
        if (statuses.contains(CertificateConstants.CERT_ACTIVE)) {
            sb.append(CertificateConstants.CERT_ACTIVE);
        } else if (statuses.contains(CertificateConstants.CERT_REVOKED)) {
            sb.append(CertificateConstants.CERT_REVOKED);
            if (!revocationReasons.isEmpty()) {
                sb.append("_").append(revocationReasons.get(0));
            }
        }
        return sb.toString();
    }
    public void setCriteriaStatus(final String criteriaStatus) {
        final List<Integer> statuses = new ArrayList<>();
        final List<Integer> revocationReasons = new ArrayList<>();
        if (!criteriaStatus.isEmpty()) {
            final String[] criteriaStatusSplit = criteriaStatus.split("_");
            if (String.valueOf(CertificateConstants.CERT_ACTIVE).equals(criteriaStatusSplit[0])) {
                statuses.addAll(Arrays.asList(new Integer[]{ CertificateConstants.CERT_ACTIVE, CertificateConstants.CERT_NOTIFIEDABOUTEXPIRATION }));
            } else {
                statuses.addAll(Arrays.asList(new Integer[]{ CertificateConstants.CERT_REVOKED, CertificateConstants.CERT_ARCHIVED }));
                if (criteriaStatusSplit.length>1) {
                    revocationReasons.addAll(Arrays.asList(new Integer[]{ Integer.parseInt(criteriaStatusSplit[1]) }));
                }
            }
        }
        stagedRequest.setStatuses(statuses);
        stagedRequest.setRevocationReasons(revocationReasons);
    }
    
    public List<SelectItem> getAvailableStatuses() {
        final List<SelectItem> ret = new ArrayList<>();
        ret.add(new SelectItem("", raLocaleBean.getMessage("search_certs_page_criteria_status_option_any")));
        ret.add(new SelectItem(String.valueOf(CertificateConstants.CERT_ACTIVE), raLocaleBean.getMessage("search_certs_page_criteria_status_option_active")));
        ret.add(new SelectItem(String.valueOf(CertificateConstants.CERT_REVOKED), raLocaleBean.getMessage("search_certs_page_criteria_status_option_revoked")));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_UNSPECIFIED));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_KEYCOMPROMISE));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_CACOMPROMISE));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_AFFILIATIONCHANGED));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_SUPERSEDED));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_CESSATIONOFOPERATION));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_REMOVEFROMCRL));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_PRIVILEGESWITHDRAWN));
        ret.add(getAvailableStatusRevoked(RevokedCertInfo.REVOCATION_REASON_AACOMPROMISE));
        return ret;
    }
    private SelectItem getAvailableStatusRevoked(final int reason) {
        return new SelectItem(CertificateConstants.CERT_REVOKED + "_" + reason, raLocaleBean.getMessage("search_certs_page_criteria_status_option_revoked_reason_"+reason));
    }

    public List<RaSearchCertificate> getFilteredResults() {
        return resultsFiltered;
    }

    private <T> List<Entry<T, String>> getAsSortedByValue(final Set<Entry<T, String>> entrySet) {
        final List<Entry<T, String>> entrySetSorted = new ArrayList<>(entrySet);
        Collections.sort(entrySetSorted, new Comparator<Entry<T, String>>() {
            @Override
            public int compare(final Entry<T, String> o1, final Entry<T, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        return entrySetSorted;
    }
}
