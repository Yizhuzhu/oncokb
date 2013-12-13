package org.mskcc.cbio.oncogkb.model;
// Generated Dec 13, 2013 4:22:38 PM by Hibernate Tools 3.2.1.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Gene generated by hbm2java
 */
public class Gene  implements java.io.Serializable {


     private int entrezGeneId;
     private String hugoSymbol;
     private String aliases;
     private Set<Alteration> alterations = new HashSet<Alteration>(0);
     private Set<GeneLabel> geneLabels = new HashSet<GeneLabel>(0);

    public Gene() {
    }

	
    public Gene(int entrezGeneId, String hugoSymbol) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
    }
    public Gene(int entrezGeneId, String hugoSymbol, String aliases, Set<Alteration> alterations, Set<GeneLabel> geneLabels) {
       this.entrezGeneId = entrezGeneId;
       this.hugoSymbol = hugoSymbol;
       this.aliases = aliases;
       this.alterations = alterations;
       this.geneLabels = geneLabels;
    }
   
    public int getEntrezGeneId() {
        return this.entrezGeneId;
    }
    
    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }
    public String getHugoSymbol() {
        return this.hugoSymbol;
    }
    
    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }
    public String getAliases() {
        return this.aliases;
    }
    
    public void setAliases(String aliases) {
        this.aliases = aliases;
    }
    public Set<Alteration> getAlterations() {
        return this.alterations;
    }
    
    public void setAlterations(Set<Alteration> alterations) {
        this.alterations = alterations;
    }
    public Set<GeneLabel> getGeneLabels() {
        return this.geneLabels;
    }
    
    public void setGeneLabels(Set<GeneLabel> geneLabels) {
        this.geneLabels = geneLabels;
    }




}


