package org.mskcc.cbio.oncogkb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA



/**
 * DrugSensitivityEvidence generated by hbm2java
 */
public class DrugSensitivityEvidence  implements java.io.Serializable {


     private Integer drugSensitivityEvidenceId;
     private TumorType tumorType;
     private Drug drug;
     private Alteration alteration;
     private String knownEffect;
     private String descriptionOfKnownEffect;
     private String context;
     private String pmids;

    public DrugSensitivityEvidence() {
    }

	
    public DrugSensitivityEvidence(Alteration alteration, String knownEffect) {
        this.alteration = alteration;
        this.knownEffect = knownEffect;
    }
    public DrugSensitivityEvidence(TumorType tumorType, Drug drug, Alteration alteration, String knownEffect, String descriptionOfKnownEffect, String context, String pmids) {
       this.tumorType = tumorType;
       this.drug = drug;
       this.alteration = alteration;
       this.knownEffect = knownEffect;
       this.descriptionOfKnownEffect = descriptionOfKnownEffect;
       this.context = context;
       this.pmids = pmids;
    }
   
    public Integer getDrugSensitivityEvidenceId() {
        return this.drugSensitivityEvidenceId;
    }
    
    public void setDrugSensitivityEvidenceId(Integer drugSensitivityEvidenceId) {
        this.drugSensitivityEvidenceId = drugSensitivityEvidenceId;
    }
    public TumorType getTumorType() {
        return this.tumorType;
    }
    
    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }
    public Drug getDrug() {
        return this.drug;
    }
    
    public void setDrug(Drug drug) {
        this.drug = drug;
    }
    public Alteration getAlteration() {
        return this.alteration;
    }
    
    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }
    public String getKnownEffect() {
        return this.knownEffect;
    }
    
    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }
    public String getDescriptionOfKnownEffect() {
        return this.descriptionOfKnownEffect;
    }
    
    public void setDescriptionOfKnownEffect(String descriptionOfKnownEffect) {
        this.descriptionOfKnownEffect = descriptionOfKnownEffect;
    }
    public String getContext() {
        return this.context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    public String getPmids() {
        return this.pmids;
    }
    
    public void setPmids(String pmids) {
        this.pmids = pmids;
    }




}


