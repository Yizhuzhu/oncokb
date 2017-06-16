package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateSearchApiController implements PrivateSearchApi {
    private Integer TYPEAHEAD_RETURN_LIMIT = 5;

    @Override
    public ResponseEntity<Set<BiologicalVariant>> searchVariantsBiologicalGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) {
        Set<BiologicalVariant> variants = new HashSet<>();
        HttpStatus status = HttpStatus.OK;

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getBiologicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Set<ClinicalVariant>> searchVariantsClinicalGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) {
        HttpStatus status = HttpStatus.OK;
        Set<ClinicalVariant> variants = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getClinicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Set<Treatment>> searchTreatmentsGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "gene", required = false) String queryGene
        , @ApiParam(value = "The level of evidence.", defaultValue = "false") @RequestParam(value = "level", required = false) String queryLevel) {
        HttpStatus status = HttpStatus.OK;
        Gene gene = GeneUtils.getGene(queryGene);
        Set<Treatment> treatments = new HashSet<>();

        if (gene == null && queryLevel == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            if (queryLevel == null) {
                treatments = TreatmentUtils.getTreatmentsByGene(gene);
            } else {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(queryLevel);
                if (level == null) {
                    status = HttpStatus.BAD_REQUEST;
                } else if (!LevelUtils.getPublicLevels().contains(level)) {
                    status = HttpStatus.BAD_REQUEST;
                } else if (gene == null) {
                    treatments = TreatmentUtils.getTreatmentsByLevels(Collections.singleton(level));
                } else {
                    treatments = TreatmentUtils.getTreatmentsByGeneAndLevels(gene, Collections.singleton(level));
                }
            }
        }
        return new ResponseEntity<>(treatments, status);
    }

    @Override
    public ResponseEntity<LinkedHashSet<TypeaheadSearchResp>> searchTypeAheadGet(
        @ApiParam(value = "The search query, it could be hugoSymbol, entrezGeneId or variant.", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "5", required = false) Integer limit) {
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (limit == null) {
            limit = TYPEAHEAD_RETURN_LIMIT;
        }
        if (query != null) {
            List<String> keywords = Arrays.asList(query.trim().split("\\s+"));

            if (keywords.size() == 1) {
                // Blur search gene
                result.addAll(convertGene(GeneUtils.searchGene(keywords.get(0), false), keywords.get(0)));

                // Blur search variant
                result.addAll(convertVariant(AlterationUtils.lookupVariant(keywords.get(0), false, AlterationUtils.getAllAlterations()), keywords.get(0)));
            } else if (keywords.size() == 2) {
                // Assume one of the keyword is gene
                Map<String, Set<Gene>> map = new HashedMap();
                for (String keyword : keywords) {
                    map.put(keyword, GeneUtils.searchGene(keyword, true));
                }

                //Find exact match
                result.addAll(getMatch(map, keywords, true));
                result.addAll(getMatch(map, keywords, false));

                // If there is no match in OncoKB database, still try to annotate variant
                if (result.size() == 0) {
                    for (Map.Entry<String, Set<Gene>> entry : map.entrySet()) {
                        if (entry.getValue().size() > 0) {
                            for (Gene gene : entry.getValue()) {
                                for (String keyword : keywords) {
                                    if (!keyword.equals(entry.getKey())) {
                                        Alteration alteration =
                                            AlterationUtils.getAlteration(gene.getHugoSymbol(), keyword, null, null, null, null);
                                        AlterationUtils.annotateAlteration(alteration, keyword);
                                        TypeaheadSearchResp typeaheadSearchResp = newTypeaheadVariant(alteration);
                                        typeaheadSearchResp.setVariantExist(false);
                                        result.add(typeaheadSearchResp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ResponseEntity<>(getLimit(result, limit), HttpStatus.OK);
    }

    private LinkedHashSet<TypeaheadSearchResp> getMatch(Map<String, Set<Gene>> map, List<String> keywords, Boolean exactMatch) {
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (map == null || keywords == null) {
            return result;
        }
        if (exactMatch == null)
            exactMatch = false;
        for (Map.Entry<String, Set<Gene>> entry : map.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (Gene gene : entry.getValue()) {
                    Set<Alteration> alterations = AlterationUtils.getAllAlterations(gene);
                    for (String keyword : keywords) {
                        if (!keyword.equals(entry.getKey()))
                            result.addAll(convertVariant(AlterationUtils.lookupVariant(keyword, exactMatch, alterations), keyword));
                    }
                }
            }
        }
        return result;
    }

    private TreeSet<TypeaheadSearchResp> convertGene(Set<Gene> genes, String keyword) {
        TreeSet<TypeaheadSearchResp> result = new TreeSet<>(new GeneComp(keyword));
        if (genes != null) {
            for (Gene gene : genes) {
                TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
                typeaheadSearchResp.setGene(gene);
                typeaheadSearchResp.setVariantExist(false);
                typeaheadSearchResp.setLink("/genes/" + gene.getHugoSymbol());
                typeaheadSearchResp.setQueryType("gene");
                result.add(typeaheadSearchResp);
            }
        }
        return result;
    }

    private TreeSet<TypeaheadSearchResp> convertVariant(List<Alteration> alterations, String keyword) {
        TreeSet<TypeaheadSearchResp> result = new TreeSet<>(new VariantComp(keyword));
        if (alterations != null) {
            for (Alteration alteration : alterations) {
                result.add(newTypeaheadVariant(alteration));
            }
        }
        return result;
    }

    private TypeaheadSearchResp newTypeaheadVariant(Alteration alteration) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(alteration.getGene());
        typeaheadSearchResp.setVariant(alteration);
        typeaheadSearchResp.setVariantExist(true);

        Query query = new Query();
        query.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
        query.setAlteration(alteration.getAlteration());

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, null, null, false);
        typeaheadSearchResp.setOncogenicity(resp.getOncogenic());

        typeaheadSearchResp.setAnnotation(resp.getVariantSummary());
        // TODO: populate treatment info.
//        Set<Evidence> evidences = EvidenceUtils.getRelevantEvidences(query, null, null, MainUtils.getTreatmentEvidenceTypes(), LevelUtils.getPublicLevels());
//        if (!evidences.isEmpty()) {
//            Map<String, LevelOfEvidence> highestLevels = IndicatorUtils.findHighestLevelByEvidences(evidences);
//            if (highestLevels.get("sensitive") != null) {
//                typeaheadSearchResp.setHighestLevelOfSensitivity(highestLevels.get("sensitive").getLevel());
//            }
//            if (highestLevels.get("resistant") != null) {
//                typeaheadSearchResp.setHighestLevelOfResistance(highestLevels.get("resistant").getLevel());
//            }
//        }

        typeaheadSearchResp.setQueryType("variant");

        // TODO: switch to variant page once it's ready.
        typeaheadSearchResp.setLink("/genes/" + alteration.getGene().getHugoSymbol());
        return typeaheadSearchResp;
    }

    private LinkedHashSet<TypeaheadSearchResp> getLimit(LinkedHashSet<TypeaheadSearchResp> result, Integer limit) {
        if (limit == null)
            limit = TYPEAHEAD_RETURN_LIMIT;
        Integer count = 0;
        LinkedHashSet<TypeaheadSearchResp> firstFew = new LinkedHashSet<>();
        Iterator<TypeaheadSearchResp> itr = result.iterator();
        while (itr.hasNext() && count < limit) {
            firstFew.add(itr.next());
            count++;
        }
        return firstFew;
    }
}

class GeneComp implements Comparator<TypeaheadSearchResp> {
    private String keyword;

    public GeneComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(TypeaheadSearchResp e1, TypeaheadSearchResp e2) {
        if (e1 == null || e1.getGene() == null) {
            return 1;
        }
        if (e2 == null || e2.getGene() == null) {
            return -1;
        }
        return GeneUtils.compareGenesByKeyword(e1.getGene(), e2.getGene(), this.keyword);
    }
}

class VariantComp implements Comparator<TypeaheadSearchResp> {
    private String keyword;

    public VariantComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(TypeaheadSearchResp e1, TypeaheadSearchResp e2) {
        if (e1 == null || e1.getVariant() == null) {
            return 1;
        }
        if (e2 == null || e2.getVariant() == null) {
            return -1;
        }
        String name1 = e1.getVariant().getAlteration().toLowerCase();
        String name2 = e2.getVariant().getAlteration().toLowerCase();
        if (e1.getVariant().getName() != null && e2.getVariant().getName() != null) {
            name1 = e1.getVariant().getName().toLowerCase();
            name2 = e2.getVariant().getName().toLowerCase();
        }
        Integer index1 = name1.indexOf(this.keyword);
        Integer index2 = name2.indexOf(this.keyword);
        if (index1.equals(index2)) {
            Integer result = name1.compareTo(name2);
            if(result == 0) {
                return e1.getGene().getHugoSymbol().compareTo(e2.getGene().getHugoSymbol());
            }else{
                return result;
            }
        } else {
            if (index1.equals(-1))
                return 1;
            if (index2.equals(-1))
                return -1;
            return index1 - index2;
        }
    }
}
