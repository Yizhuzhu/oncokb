
package org.mskcc.cbio.oncokb.dao.importor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.NccnGuidelineBo;
import org.mskcc.cbio.oncokb.bo.TreatmentBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Article;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.NccnGuideline;
import org.mskcc.cbio.oncokb.model.Treatment;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;
import org.mskcc.cbio.oncokb.util.NcbiEUtils;

/**
 *
 * @author jgao
 */
public final class QuestDocAnnotationParser {
    private static final String GENE_P = "Gene: ?(.+)";
    private static final String GENE_BACKGROUND_P = "^Background:? *";
        
    private static final String MUTATION_P = "Mutations?: ?(.+)";
    private static final String MUTATION_EFFECT_P = "Mutation effect: ?([^\\(]+)(\\(PMIDs?:.+\\))?";
    private static final String MUTATION_EFFECT_DESCRIPTION_P = "^Description of mutation effect:? *";
    
    private static final String TUMOR_TYPE_P = "Tumor type: ?(.+)";
    
    private static final String PREVALENCE_P = "Prevalence:? *";
    
    private static final String PROGNOSTIC_IMPLICATIONS_P = "Prognostic implications:? *";
    
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P = "Standard therapeutic implications for drug sensitivity:? ?";
    private static final String STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P = "Standard therapeutic implications for drug resistance:? ?";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P = "Investigational therapeutic implications for drug sensitivity:?";
    private static final String INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P = "Investigational therapeutic implications for drug resistance:?";
    private static final String SENSITIVE_TO_P = "Sensitive to: ?(.+)";
    private static final String RESISTANT_TO_P = "Resistant to: ?(.+)";
    private static final String HIGHEST_LEVEL_OF_EVIDENCE = "Highest level of evidence: ?(.*)";
    private static final String DESCRIPTION_OF_EVIDENCE_P = "Description of evidence:? ?(.*)";
    
    
    private static final String NCCN_GUIDELINES_P = "NCCN guidelines";
    private static final String NCCN_DISEASE_P = "Disease: ?(.+)";
    private static final String NCCN_VERSION_P = "Version: ?(.+)";
    private static final String NCCN_PAGES_P = "Pages: ?(.+)";
    private static final String NCCN_PAGES_RECOMMENDATION_CATEGORY = "Recommendation category: ?(.+)";
    private static final String NCCN_EVIDENCE_P = "Description of evidence:? ?";
    
    private static final String ONGOING_CLINICAL_TRIALS_P = "Ongoing clinical trials:?$";
    private static final String CLINICAL_TRIALS_P = "(NCT[0-9]+)";
    
    private static final String INVESTIGATIONAL_INTERACTING_GENE_ALTERATIONS_P = "Interacting gene alterations$";
    
    private static final String[] CANCER_HEADERS_P = new String[] {
        PREVALENCE_P,
        PROGNOSTIC_IMPLICATIONS_P,
        STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P,
        STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P,
        NCCN_GUIDELINES_P,
        INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P,
        INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P,
        ONGOING_CLINICAL_TRIALS_P,
        INVESTIGATIONAL_INTERACTING_GENE_ALTERATIONS_P
    };
    
    private QuestDocAnnotationParser() {
        throw new AssertionError();
    }
    
    private static final String QUEST_CURATION_FOLDER = "/Users/jgao/projects/oncokb-data/quest-annotation";
    private static final String QUEST_CURATION_FILE = "/data/quest-curations.txt";
    
    public static void main(String[] args) throws IOException {
        VariantConsequenceImporter.main(args);
//        PiHelperDrugImporter.main(args);
        
        parse(new FileInputStream(QUEST_CURATION_FOLDER+"/BRAF.docx.txt"));
        
//        List<String> files = FileUtils.getFilesInFolder(QUEST_CURATION_FOLDER, "txt");
//        for (String file : files) {
//            parse(new FileInputStream(file));
//        }
    }
    
    private static void parse(InputStream is) throws IOException {
        List<String> lines = FileUtils.readTrimedLinesStream(is);
        List<int[]> geneLines = extractLines(lines, 0, lines.size(), GENE_P, 1);
        for (int[] ix : geneLines) {
            parseGene(lines, ix[0], ix[1]);
        }
    }
    
    private static void parseGene(List<String> lines, int start, int end) throws IOException {
        if (!lines.get(start).startsWith("Gene: ")) {
            System.err.println("Gene line should start with Gene: ");
        }
        
        System.out.println("##"+lines.get(start));
        
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        Pattern p = Pattern.compile(GENE_P);
        Matcher m = p.matcher(lines.get(start));
        m.matches();
        String hugo = m.group(1);
        Gene gene = geneBo.findGeneByHugoSymbol(hugo);
        if (gene == null) {
            System.out.println("Could not find gene "+hugo+". Loading from MyGene.Info...");
            gene = GeneAnnotatorMyGeneInfo2.readByHugoSymbol(hugo);
            if (gene == null) {
                System.err.println("Could not find gene "+hugo+" either.");
            }
            geneBo.save(gene);
        }
        
        // background
        int[] geneBgLines = parseGeneBackground(gene, lines, start, end);
        
        // mutations
        parseMutations(gene, lines, geneBgLines[1], end);
    }
    
    private static int[] parseGeneBackground(Gene gene, List<String> lines, int start, int end) {
        List<int[]> backgroundLines = extractLines(lines, start, end, GENE_BACKGROUND_P, MUTATION_P, 1);
        if (backgroundLines.size()!=1) {
            System.err.println("There should be one background section for gene: "+gene.getHugoSymbol());
        }
        
        System.out.println("##  Background");
        
        int s = backgroundLines.get(0)[0];
        int e = backgroundLines.get(0)[1];
        StringBuilder sb = new StringBuilder();
        for (int i=s+1; i<e; i++) {
            if (!lines.get(i).startsWith("cBioPortal link:") &&
                    !lines.get(i).startsWith("COSMIC link:") &&
                    !lines.get(i).startsWith("Mutations")) {
                sb.append(lines.get(i)).append("\n");
            }
        }
        String bg = sb.toString();
        
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.GENE_BACKGROUND);
        evidence.setGene(gene);
        evidence.setDescription(bg);
        setDocuments(bg, evidence);
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
        
        return backgroundLines.get(0);
    }
    
    private static void parseMutations(Gene gene, List<String> lines, int start, int end) {
        List<int[]> mutationLines = extractLines(lines, start, end, MUTATION_P, MUTATION_P, -1);
        for (int[] ixMutationLines : mutationLines) {
            int startMutation = ixMutationLines[0];
            int endMutation = ixMutationLines[1];
            parseMutation(gene, lines, startMutation, endMutation);
        }
        
    }
    
    private static void parseMutation(Gene gene, List<String> lines, int start, int end) {
        // mutation
        Pattern p = Pattern.compile(MUTATION_P);
        Matcher m = p.matcher(lines.get(start));
        if (!m.matches()) {
            System.err.println("wrong format of mutation line: "+lines.get(0));
        }
        
        String mutationStr = m.group(1);
        if (mutationStr.contains("[")) {
            mutationStr = mutationStr.substring(0, mutationStr.indexOf("["));
        }
        mutationStr = mutationStr.trim();
        
        System.out.println("##  Mutation: "+mutationStr);
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        AlterationType type = AlterationType.MUTATION; //TODO: cna and fution
        
        Set<Alteration> alterations = new HashSet<Alteration>();
        for (String mutation : parseMutationString(mutationStr)) {
            Alteration alteration = alterationBo.findAlteration(gene, type, mutation);
            if (alteration==null) {
                alteration = new Alteration();
                alteration.setGene(gene);
                alteration.setAlterationType(type);
                alteration.setAlteration(mutation);
                AlterationUtils.annotateAlteration(alteration);
                alterationBo.save(alteration);
            }
            alterations.add(alteration);
        }
        
        // mutation effect
        String mutationEffectStr = lines.get(start+1);
        
        p = Pattern.compile(MUTATION_EFFECT_P);
        m = p.matcher(mutationEffectStr);
        if (!m.matches()) {
            System.err.println("wrong format of mutation effect line: "+mutationEffectStr);
        } else {
            String effect = m.group(1);
            if (effect!=null && !effect.isEmpty()) {
                System.out.println("##    Effect: "+alterations.toString());

                effect = effect.trim();

                // Description of mutation effect
                List<int[]> mutationEffectDescLine = extractLines(lines, start+2, end, MUTATION_EFFECT_DESCRIPTION_P, TUMOR_TYPE_P, 1);
                String descMutationEffectStr = joinLines(lines, mutationEffectDescLine.get(0)[0]+1, mutationEffectDescLine.get(0)[1]);

                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setDescription(descMutationEffectStr);
                evidence.setKnownEffect(effect);
                setDocuments(descMutationEffectStr, evidence);
                
                EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
                evidenceBo.save(evidence);
            }
        }
        
        // cancers
        List<int[]> cancerLines = extractLines(lines, start+1, end, TUMOR_TYPE_P, TUMOR_TYPE_P, -1);
        for (int[] ixcancerLines : cancerLines) {
            int startCancer = ixcancerLines[0];
            int endCancer = ixcancerLines[1];
            parseCancer(gene, alterations, lines, startCancer, endCancer);
        }
    }
    
    private static Set<String> parseMutationString(String mutationStr) {
        Set<String> ret = new HashSet<String>();
        String[] parts = mutationStr.split(", *");
        Pattern p = Pattern.compile("([A-Z][0-9]+)([^0-9/]+/.+)");
        for (String part : parts) {
            Matcher m = p.matcher(part);
            if (m.find()) {
                String ref = m.group(1);
                for (String var : m.group(2).split("/")) {
                    ret.add(ref+var);
                }
            } else {
                ret.add(part);
            }
        }
        return ret;
    }
    
    private static void parseCancer(Gene gene, Set<Alteration> alterations, List<String> lines, int start, int end) {
        String line = lines.get(start);
        Pattern p = Pattern.compile(TUMOR_TYPE_P);
        Matcher m = p.matcher(line);
        if (!m.matches()) {
            System.err.println("wrong format of type type line: "+line);
        }
        String cancer = m.group(1).trim();
        
        System.out.println("##    Cancer type: " + cancer);
        
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        TumorType tumorType = tumorTypeBo.findTumorTypeByName(cancer);
        if (tumorType==null) {
            tumorType = new TumorType(cancer, cancer, cancer);
            tumorTypeBo.save(tumorType);
        }
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        // Prevalance
        List<int[]> prevalenceLines = extractLines(lines, start+1, end, PREVALENCE_P, CANCER_HEADERS_P, 1);
        if (!prevalenceLines.isEmpty()) {
            System.out.println("##      Prevalance: " + alterations.toString());
            String prevalenceTxt = joinLines(lines, prevalenceLines.get(0)[0]+1, prevalenceLines.get(0)[1]).trim();
            if (!prevalenceTxt.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.PREVALENCE);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setDescription(prevalenceTxt);
                setDocuments(prevalenceTxt, evidence);

                evidenceBo.save(evidence);
            }
        } else {
            System.out.println("##      No Prevalance for " + alterations.toString());
        }
        
        // Prognostic implications
        List<int[]> prognosticLines = extractLines(lines, start+1, end, PROGNOSTIC_IMPLICATIONS_P, CANCER_HEADERS_P, 1);
        if (!prognosticLines.isEmpty()) {
            System.out.println("##      Proganostic implications:" + alterations.toString());
            String prognosticTxt = joinLines(lines, prognosticLines.get(0)[0]+1, prognosticLines.get(0)[1]).trim();
            if (!prognosticTxt.isEmpty()) {

                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.PROGNOSTIC_IMPLICATION);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setDescription(prognosticTxt);
                setDocuments(prognosticTxt, evidence);

                evidenceBo.save(evidence);
            }
        } else {
            System.out.println("##      No Proganostic implications "+alterations.toString());
        }
        
        // standard therapeutic implications of drug sensitivity
        List<int[]> standardSensitivityLines = extractLines(lines, start+1, end, STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P, CANCER_HEADERS_P, 1);
        if (!standardSensitivityLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, standardSensitivityLines.get(0)[0], standardSensitivityLines.get(0)[1],
                    EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, "Sensitive", SENSITIVE_TO_P);
        } else {
            System.out.println("##      No "+STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P+" for "+alterations.toString());
        }
        
        // standard therapeutic implications of drug resistance
        List<int[]> standardResistanceLines = extractLines(lines, start+1, end, STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P, CANCER_HEADERS_P, 1);
        if (!standardResistanceLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, standardResistanceLines.get(0)[0], standardResistanceLines.get(0)[1],
                    EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, "Resistant", RESISTANT_TO_P); 
        } else {
            System.out.println("##      No "+STANDARD_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P+" for "+alterations.toString());
        }
        
        // NCCN
        List<int[]> nccnLines = extractLines(lines, start+1, end, NCCN_GUIDELINES_P, CANCER_HEADERS_P, 1);
        if (!nccnLines.isEmpty()) {
            System.out.println("##      NCCN for "+alterations.toString());
            for (int[] nccnLine: nccnLines) {
                List<int[]> nccnOneDiseaseLines = extractLines(lines, nccnLine[0]+1, nccnLine[1], NCCN_DISEASE_P, NCCN_DISEASE_P, -1);
                for (int[] nccnOneDiseaseLine : nccnOneDiseaseLines) {
                    parseNCCN(gene, alterations, tumorType, lines, nccnOneDiseaseLine[0], nccnOneDiseaseLine[1]);
                }
            }
        } else {
            System.out.println("##      No NCCN for "+alterations.toString());
        }
        
        
        // Investigational therapeutic implications of drug sensitivity
        List<int[]> investigationalSensitivityLines = extractLines(lines, start+1, end, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P, CANCER_HEADERS_P, 1);
        if (!investigationalSensitivityLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, investigationalSensitivityLines.get(0)[0], investigationalSensitivityLines.get(0)[1],
                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, "Sensitive", SENSITIVE_TO_P);
        } else {
            System.out.println("##      No "+INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY_P+" for "+alterations.toString());
        }
        
        // Investigational therapeutic implications of drug resistance
        List<int[]> investigationalResistanceLines = extractLines(lines, start+1, end, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P, CANCER_HEADERS_P, 1);
        if (!investigationalResistanceLines.isEmpty()) {
            parseTherapeuticImplcations(gene, alterations, tumorType, lines, investigationalResistanceLines.get(0)[0], investigationalResistanceLines.get(0)[1],
                EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, "Resistant", RESISTANT_TO_P);
        } else {
            System.out.println("##      No "+INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE_P+" for "+alterations.toString());
        }
        
        List<int[]> clinicalTrialsLines = extractLines(lines, start+1, end, ONGOING_CLINICAL_TRIALS_P, CANCER_HEADERS_P, 1);
        if (!clinicalTrialsLines.isEmpty()) {
            System.out.println("##      Clincial trials for "+alterations.toString());
            parseClinicalTrials(gene, alterations, tumorType, lines, clinicalTrialsLines.get(0)[0], clinicalTrialsLines.get(0)[1]);
        } else {
            System.out.println("##      No Clincial trials for "+alterations.toString());
        }
    }
    
    private static void parseClinicalTrials(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern p = Pattern.compile(CLINICAL_TRIALS_P);
        for (int i=start; i<end; i++) {
            Matcher m = p.matcher(lines.get(i));
            if (m.find()) {
                String nctId = m.group(1);
                ClinicalTrial ct = clinicalTrialBo.findClinicalTrialByPmid(nctId);
                if (ct==null) {
                    ct = new ClinicalTrial(nctId);
                    clinicalTrialBo.save(ct);
                }
                
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(EvidenceType.CLINICAL_TRIAL);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setClinicalTrials(Collections.singleton(ct));
                evidenceBo.save(evidence);
            }
        }
        
    }
    
    private static void parseTherapeuticImplcations(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end,
            EvidenceType evidenceType, String knownEffectOfEvidence, String sensitivieP) {
        System.out.println("##      "+evidenceType+" for "+alterations.toString()+" "+tumorType.getName());
        
        List<int[]> drugLines = extractLines(lines, start+1, end, sensitivieP, sensitivieP, -1);
        if (drugLines.isEmpty()) return;
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        {
            // general description
            String desc = joinLines(lines, start+1, drugLines.get(0)[0]).trim();
            if (!desc.isEmpty()) {
                Evidence evidence = new Evidence();
                evidence.setEvidenceType(evidenceType);
                evidence.setAlterations(alterations);
                evidence.setGene(gene);
                evidence.setTumorType(tumorType);
                evidence.setKnownEffect(knownEffectOfEvidence);
                evidence.setDescription(desc);
                setDocuments(desc, evidence);
                evidenceBo.save(evidence);
            }
        }
        
        // specific evidence
        Pattern pSensitiveTo = Pattern.compile(sensitivieP);
        Pattern pLevel = Pattern.compile(HIGHEST_LEVEL_OF_EVIDENCE);
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();
        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
        
        for (int[] drugLine : drugLines) {
            Evidence evidence = new Evidence();
            evidence.setEvidenceType(evidenceType);
            evidence.setAlterations(alterations);
            evidence.setGene(gene);
            evidence.setTumorType(tumorType);
            evidence.setKnownEffect(knownEffectOfEvidence);
            
            // sensitive to
            List<int[]> sensitiveLines = extractLines(lines, drugLine[0], drugLine[1], sensitivieP, 1);
            if (sensitiveLines.isEmpty()) continue;
            
            Matcher m = pSensitiveTo.matcher(lines.get(sensitiveLines.get(0)[0]));
            if (!m.matches()) continue;
            
            String[] drugTxts = m.group(1).trim().replaceAll("\\([^\\)]*\\)", "").split(",");

            Set<Treatment> treatments = new HashSet<Treatment>();
            for (String drugTxt : drugTxts) {
                String[] drugNames = drugTxt.split(" \\+");
                
                Set<Drug> drugs = new HashSet<Drug>();
                for (String drugName : drugNames) {
                    drugName = drugName.trim();
                    Drug drug = drugBo.guessUnambiguousDrug(drugName);
                    if (drug==null) {
                        drug = new Drug(drugName);
                        drugBo.save(drug);
                    }
                    drugs.add(drug);
                }
                
                Treatment treatment = new Treatment();
                treatment.setDrugs(drugs);
                treatmentBo.save(treatment);
                
                treatments.add(treatment);
            }
            evidence.setTreatments(treatments);
            
            // highest level of evidence
            List<int[]> levelLines = extractLines(lines, drugLine[0], drugLine[1], HIGHEST_LEVEL_OF_EVIDENCE, 1);
            if (!levelLines.isEmpty()) {
                m = pLevel.matcher(lines.get(levelLines.get(0)[0]));
                if (m.matches())  {
                    evidence.setLevelOfEvidence(LevelOfEvidence.getByLevel(m.group(1)));
                }
            } else {
                if (evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE
                        || evidenceType == EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY) {
                    evidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
                }
            }
            
            // description
            List<int[]> descLines = extractLines(lines, drugLine[0], drugLine[1], DESCRIPTION_OF_EVIDENCE_P, 1);
            if (!descLines.isEmpty()) {
                String desc = joinLines(lines, descLines.get(0)[0]+1, descLines.get(0)[1]).trim();
                if (!desc.isEmpty()) {
                    evidence.setDescription(desc);
                    setDocuments(desc, evidence);
                }
            }
            
            evidenceBo.save(evidence);
        }
    }
    
    private static void parseNCCN(Gene gene, Set<Alteration> alterations, TumorType tumorType, List<String> lines, int start, int end) {
        // disease
        String txt = lines.get(start).trim();
        Pattern p = Pattern.compile(NCCN_DISEASE_P);
        Matcher m = p.matcher(txt);
        if (!m.matches()) {
            System.err.println("Problem with NCCN disease line: "+txt);
            return;
        }
        
        String disease = m.group(1);
        
        if (disease == null) {
            return;
        }
        
        // version
        txt = lines.get(start+1).trim();
        p = Pattern.compile(NCCN_VERSION_P);
        m = p.matcher(txt);
        String version = null;
        if (!m.matches()) {
            System.err.println("Problem with NCCN version line: "+txt);
        } else {
            version = m.group(1);
        }
        
        // pages
        txt = lines.get(start+2);
        p = Pattern.compile(NCCN_PAGES_P);
        m = p.matcher(txt);
        String pages = null;
        if (!m.matches()) {
            System.err.println("Problem with NCCN pages line: "+txt);
        } else {
            pages = m.group(1);
        }
        
        // Recommendation category
        txt = lines.get(start+3);
        p = Pattern.compile(NCCN_PAGES_RECOMMENDATION_CATEGORY);
        m = p.matcher(txt);
        String category = null;
        if (!m.matches()) {
            System.err.println("Problem with NCCN category line: "+txt);
        } else {
            category = m.group(1);
        }
        
        // description
        txt = lines.get(start+4);
        p = Pattern.compile(NCCN_EVIDENCE_P);
        m = p.matcher(txt);
        String nccnDescription = null;
        if (!m.matches()) {
            System.err.println("Problem with NCCN description line: "+txt);
        } else {
            String desc = joinLines(lines, start+5, end).trim();
            if (!desc.isEmpty()) {
                nccnDescription = desc;
            }
        }
        
        
        Evidence evidence = new Evidence();
        evidence.setEvidenceType(EvidenceType.NCCN_GUIDELINES);
        evidence.setAlterations(alterations);
        evidence.setGene(gene);
        evidence.setTumorType(tumorType);
        
        NccnGuidelineBo nccnGuideLineBo = ApplicationContextSingleton.getNccnGuidelineBo();
        
        NccnGuideline nccnGuideline = nccnGuideLineBo.findNccnGuideline(disease, version, pages);
        if (nccnGuideline==null) {
            nccnGuideline = new NccnGuideline();
            nccnGuideline.setDisease(disease);
            nccnGuideline.setVersion(version);
            nccnGuideline.setPages(pages);
            nccnGuideline.setCategory(category);
            nccnGuideline.setDescription(nccnDescription);
            nccnGuideLineBo.save(nccnGuideline);
        }

        evidence.setNccnGuidelines(Collections.singleton(nccnGuideline));
                
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        evidenceBo.save(evidence);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, int limit) {
        return extractLines(lines, start, end, startLinePatten, (String[])null, limit);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, String endLinePattern, int limit) {
        return extractLines(lines, start, end, startLinePatten, new String[]{endLinePattern}, limit);
    }
    
    private static List<int[]> extractLines(List<String> lines, int start, int end, String startLinePatten, String[] endLinePatterns, int limit) {
        List<int[]> indices = new ArrayList<int[]>();

        int s=start, e=start;
        
        while (s<end && e<end) {
            // find start line
            s = e;
            while (s<end && !lines.get(s).matches(startLinePatten)) {
                s++;
            }

            // find end line
            e = endLinePatterns==null ? end : (s + 1);
            while (e<end && !matchAnyPattern(lines.get(e), endLinePatterns)) {
                e++;
            }

            if (s<end) {
                indices.add(new int[]{s,e});
            }
            
            if (limit>0 && indices.size()>=limit) {
                break;
            }
        }
        
        return indices;
    }
    
    private static boolean matchAnyPattern(String s, String[] patterns) {
        for (String p : patterns) {
            if (s.matches(p)) {
                return true;
            }
        }
        return false;
    }
    
    private static String joinLines(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i=start; i<end; i++) {
            sb.append(lines.get(i)).append("\n");
        }
        return sb.toString();
    }
    
    private static void setDocuments(String str, Evidence evidence) {
        Set<Article> docs = new HashSet<Article>();
        Set<ClinicalTrial> clinicalTrials = new HashSet<ClinicalTrial>();
        ArticleBo articleBo = ApplicationContextSingleton.getArticleBo();
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        Pattern pmidPattern = Pattern.compile("\\(PMIDs?:([^\\);]+).*\\)");
        Matcher m = pmidPattern.matcher(str);
        int start = 0;
        while (m.find(start)) {
            String pmids = m.group(1).trim();
            for (String pmid : pmids.split(", *(PMID:)? *")) {
                if (pmid.startsWith("NCT")) {
                    // support NCT numbers
                    String[] nctIds = pmid.split(", *");
                    for (String nctId : nctIds) {
                        ClinicalTrial ct = clinicalTrialBo.findClinicalTrialByPmid(nctId);
                        if (ct==null) {
                            ct = new ClinicalTrial(nctId);
                            clinicalTrialBo.save(ct);
                        }
                        clinicalTrials.add(ct);
                    }
                }
                
                Article doc = articleBo.findArticleByPmid(pmid);
                if (doc==null) {
                    doc = NcbiEUtils.readPubmedArticle(pmid);
                    articleBo.save(doc);
                }
                docs.add(doc);
            }
            start = m.end();
        }
        
        evidence.setArticles(docs);
        evidence.setClinicalTrials(clinicalTrials);
    }
}