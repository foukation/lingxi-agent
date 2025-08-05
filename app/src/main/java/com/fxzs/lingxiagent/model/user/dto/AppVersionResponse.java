package com.fxzs.lingxiagent.model.user.dto;

public class AppVersionResponse {
    private String version;
    private Boolean forceUpdate;
    private String apkUrl;
    private String udpateDesc;
    private String logo;
    private String privacyPolicyUrl;
    private String licenseAgreementUrl;
    private String recordNumber;
    private String learnMoreUrl;
    private Boolean needUpdate;
    
    // 默认构造函数
    public AppVersionResponse() {
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Boolean getForceUpdate() {
        return forceUpdate;
    }
    
    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
    
    public String getApkUrl() {
        return apkUrl;
    }
    
    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }
    
    public String getUdpateDesc() {
        return udpateDesc;
    }
    
    public void setUdpateDesc(String udpateDesc) {
        this.udpateDesc = udpateDesc;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }
    
    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }
    
    public String getLicenseAgreementUrl() {
        return licenseAgreementUrl;
    }
    
    public void setLicenseAgreementUrl(String licenseAgreementUrl) {
        this.licenseAgreementUrl = licenseAgreementUrl;
    }
    
    public String getRecordNumber() {
        return recordNumber;
    }
    
    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }
    
    public String getLearnMoreUrl() {
        return learnMoreUrl;
    }
    
    public void setLearnMoreUrl(String learnMoreUrl) {
        this.learnMoreUrl = learnMoreUrl;
    }
    
    public Boolean getNeedUpdate() {
        return needUpdate;
    }
    
    public void setNeedUpdate(Boolean needUpdate) {
        this.needUpdate = needUpdate;
    }
}