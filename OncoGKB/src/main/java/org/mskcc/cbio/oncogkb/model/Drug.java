package org.mskcc.cbio.oncogkb.model;
// Generated Dec 13, 2013 4:22:38 PM by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Drug generated by hbm2java
 */
public class Drug  implements java.io.Serializable {


     private Integer drugId;
     private String drugName;
     private String synonyms;
     private boolean fdaApproved;
     private Set<DrugSensitivityEvidence> drugSensitivityEvidences = new HashSet<DrugSensitivityEvidence>(0);

    public Drug() {
    }

	
    public Drug(String drugName, boolean fdaApproved) {
        this.drugName = drugName;
        this.fdaApproved = fdaApproved;
    }
    public Drug(String drugName, String synonyms, boolean fdaApproved, Set<DrugSensitivityEvidence> drugSensitivityEvidences) {
       this.drugName = drugName;
       this.synonyms = synonyms;
       this.fdaApproved = fdaApproved;
       this.drugSensitivityEvidences = drugSensitivityEvidences;
    }
   
    public Integer getDrugId() {
        return this.drugId;
    }
    
    public void setDrugId(Integer drugId) {
        this.drugId = drugId;
    }
    public String getDrugName() {
        return this.drugName;
    }
    
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }
    public String getSynonyms() {
        return this.synonyms;
    }
    
    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
    public boolean isFdaApproved() {
        return this.fdaApproved;
    }
    
    public void setFdaApproved(boolean fdaApproved) {
        this.fdaApproved = fdaApproved;
    }
    public Set<DrugSensitivityEvidence> getDrugSensitivityEvidences() {
        return this.drugSensitivityEvidences;
    }
    
    public void setDrugSensitivityEvidences(Set<DrugSensitivityEvidence> drugSensitivityEvidences) {
        this.drugSensitivityEvidences = drugSensitivityEvidences;
    }




}


