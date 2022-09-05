package com.dbms.util.workers;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.service.*;
import com.dbms.web.dto.MQReportRelationsWorkerDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class UniquePTReportRelationsWorker implements Callable<MQReportRelationsWorkerDTO> {

    private static final Logger LOG = LoggerFactory
            .getLogger(UniquePTReportRelationsWorker.class);
    private static final String PT = "PT";
    private static final String SMQ1 = "SMQ1";
    private static final String SMQ2 = "SMQ2";
    private static final String SMQ3 = "SMQ3";
    private static final String LLT = "LLT";
    private static final String SMQ4 = "SMQ4";
    private static final String SMQ5 = "SMQ5";
    private static final String CHILD_SMQ = "Child SMQ";
    private CmqRelation190 relation;
    private String level = "", term = "", codeTerm = "", workerName = null, dictionaryVersion = "";
    Map<String, String> relationScopeMap;

    private final ISmqBaseService smqBaseService;
    private final IMeddraDictService meddraDictService;
    private boolean filterLltFlag;

    public UniquePTReportRelationsWorker(int workerId, CmqRelation190 relation, Map<String, String> relationScopeMap, boolean filterLltFlag, String dictionaryVersion, final ISmqBaseService smqBaseService, final IMeddraDictService meddraDictService) {
        this.workerName = "UniquePTReportRelationsWorker_" + workerId;
        this.relation = relation;
        this.relationScopeMap = relationScopeMap;
        this.filterLltFlag = filterLltFlag;
        this.dictionaryVersion = dictionaryVersion;
        this.smqBaseService = smqBaseService;
        this.meddraDictService = meddraDictService;
    }

    @Override
    public MQReportRelationsWorkerDTO call() throws Exception {
        int cpt = 0;
        MQReportRelationsWorkerDTO relationsWorkerDTO = new MQReportRelationsWorkerDTO();
        relationsWorkerDTO.setWorkerName(workerName);
        LOG.info("In {} Starting Callable.", this.workerName);
        try {
            if (relation.getSmqCode() != null) {
                String selectedScope = relationScopeMap.get(String.valueOf(relation.getSmqCode()));
                if(StringUtils.isEmpty(selectedScope)) {
                    selectedScope = relation.getTermScope();
                }
                List<Long> smqChildCodeList = new ArrayList<>();
                smqChildCodeList.add(relation.getSmqCode());

                SmqBase190 smqSearched = smqBaseService.findByCode(relation.getSmqCode());
                if (smqSearched != null) {
                    List<SmqBase190> smqBaseList = smqBaseService.findByLevelAndTerm(smqSearched.getSmqLevel(),	smqSearched.getSmqName());
                    if (smqBaseList != null) {
                        for (SmqBase190 smq : smqBaseList) {
                            if (smq.getSmqLevel() == 1) {
                                level = SMQ1;
                            } else if (smq.getSmqLevel() == 2) {
                                level = SMQ2;
                            } else if (smq.getSmqLevel() == 3) {
                                level = SMQ3;
                            } else if (smq.getSmqLevel() == 4) {
                                level = SMQ4;
                            } else if (smq.getSmqLevel() == 5) {
                                level = SMQ5;
                            }

                            /**
                             * Other SMQs
                             *
                             */
                            List<SmqBase190> smqs = smqBaseService.findChildSmqByParentSmqCodes(smqChildCodeList);

                            if (smqs != null) {
                                for (SmqBase190 smqC : smqs) {
                                    if (smqC.getSmqLevel() == 1) {
                                        level = SMQ1;
                                    } else if (smqC.getSmqLevel() == 2) {
                                        level = SMQ2;
                                    } else if (smqC.getSmqLevel() == 3) {
                                        level = SMQ3;
                                    } else if ((smqC.getSmqLevel() == 4)
                                            || (smqC.getSmqLevel() == 0)
                                            || (smqC.getSmqLevel() == 5)) {
                                        level = PT;
                                    }

                                    if ((smqC.getSmqLevel() != 5) || (!filterLltFlag && smqC.getSmqLevel() == 5)) {
                                        if(level.equals(PT)){
                                            relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getSmqName(), ""));
                                        }

                                        if (level.equals(SMQ1)) {
                                            smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
                                            if (smqSearched != null) {
                                                List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                if (list != null) {
                                                    for (SmqRelation190 smq3 : list) {
                                                        relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                                    }
                                                }
                                            }
                                            List<Long> codes = new ArrayList<>();
                                            codes.add(smqC.getSmqCode());

                                            //Others relations
                                            String levelS = "";
                                            if (smqSearched.getSmqLevel() == 2) {
                                                levelS = SMQ2;
                                            }
                                            if (smqSearched.getSmqLevel() == 3) {
                                                levelS = SMQ3;
                                            } else if (smqSearched.getSmqLevel() == 4) {
                                                levelS = PT;
                                            } else if (smqSearched.getSmqLevel() == 5) {
                                                levelS = LLT;
                                            } else if (smqSearched.getSmqLevel() == 0) {
                                                levelS = CHILD_SMQ;
                                            }

                                            if ((smqSearched.getSmqLevel() != 5) || (!filterLltFlag && smqSearched.getSmqLevel() == 5)) {
                                                List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
                                                if (smqChildren != null) {
                                                    for (SmqBase190 child : smqChildren) {
                                                        if(levelS.equals(PT)){
                                                            relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ""));
                                                        }

                                                        codes = new ArrayList<>();
                                                        codes.add(child.getSmqCode());

                                                        smqSearched = smqBaseService.findByCode(child.getSmqCode());
                                                        if (smqSearched != null) {
                                                            //List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
                                                            List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                            if (list != null) {
                                                                for (SmqRelation190 smq3 : list) {
                                                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (level.equals(SMQ2)) {
                                            smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
                                            if (smqSearched != null) {
                                                //List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
                                                List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                if (list != null) {
                                                    for (SmqRelation190 smq3 : list) {
                                                        relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                                    }
                                                }
                                            }
                                            List<Long> codes = new ArrayList<>();
                                            codes.add(smqC.getSmqCode());

                                            //Others relations
                                            Integer smqLevel = smqSearched.getSmqLevel();
                                            String levelS = "";
                                            if (smqLevel == 3) {
                                                levelS = SMQ3;
                                            } else if (smqLevel == 4) {
                                                levelS = PT;
                                            } else if (smqLevel == 5) {
                                                levelS = LLT;
                                            } else if (smqLevel == 0) {
                                                levelS = CHILD_SMQ;
                                            }

                                            if ((smqLevel != 5) || (!filterLltFlag && smqLevel == 5)) {
                                                List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
                                                if (smqChildren != null) {
                                                    for (SmqBase190 child : smqChildren) {
                                                        if(levelS.equals(PT)){
                                                            relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ""));
                                                        }

                                                        smqSearched = smqBaseService.findByCode(child.getSmqCode());
                                                        if (smqSearched != null) {
                                                            List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                            if (list != null) {
                                                                for (SmqRelation190 smq3 : list) {
                                                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (level.equals(SMQ3)) {
                                            smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
                                            if (smqSearched != null) {
                                                //List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
                                                List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                if (list != null) {
                                                    for (SmqRelation190 smq3 : list) {
                                                        relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                List<SmqRelation190> childSmqs =  smqBaseService.findSmqRelationsForSmqCodeAndScope(relation.getSmqCode(), selectedScope);

                if((null != childSmqs) && (childSmqs.size() > 0)) {
                    for (SmqRelation190 childSmq : childSmqs) {
                        if (childSmq.getSmqLevel() == 0) {
                            level = CHILD_SMQ;
                        }if (childSmq.getSmqLevel() == 1) {
                            level = SMQ1;
                        } else if (childSmq.getSmqLevel() == 2) {
                            level = SMQ2;
                        } else if (childSmq.getSmqLevel() == 3) {
                            level = SMQ3;
                        } else if (childSmq.getSmqLevel() == 4) {
                            level = PT;
                        } else if (childSmq.getSmqLevel() == 5) {
                            level = LLT;
                        }

                        if ((childSmq.getSmqLevel() != 5) || (!filterLltFlag && childSmq.getSmqLevel() == 5)) {
                            if(level.equals(PT)){
                                relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, childSmq.getPtCode() + "", childSmq.getPtName(), "", childSmq.getPtTermScope() + "", childSmq.getPtTermWeight() + "", childSmq.getPtTermCategory(), "", childSmq.getPtTermStatus()));
                            }

                            List<Long> codes = new ArrayList<>();
                            codes.add(childSmq.getSmqCode());

                            //List<SmqBase190> smqs = smqBaseService.findChildSmqByParentSmqCodes(codes);

                            smqSearched = smqBaseService.findByCode(Long.parseLong(childSmq.getPtCode() + ""));
                            if (smqSearched != null) {
                                //List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
                                List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                if (list != null) {
                                    for (SmqRelation190 smq3 : list) {
                                        if (smq3.getSmqLevel() == 4) {
                                            level = PT;
                                        } else if (smq3.getSmqLevel() == 5) {
                                            level = LLT;
                                        } else if (smq3.getSmqLevel() == 0) {
                                            level = CHILD_SMQ;
                                        }

                                        if ((smq3.getSmqLevel() != 5) || (!filterLltFlag && smq3.getSmqLevel() == 5)) {
                                            if(level.equals(PT)){
                                                relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), "", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
                                            }

                                            if (level.equals(CHILD_SMQ)) {
                                                smqSearched = smqBaseService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
                                                if (smqSearched != null) {
                                                    List<SmqRelation190> test =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                    System.out.println("\n ************ test size for " + smqSearched.getSmqName() + " = " + test.size());

                                                    if (test != null) {
                                                        for (SmqRelation190 tt : test) {
                                                            if (tt.getSmqLevel() == 4) {
                                                                level = PT;
                                                            } else if (tt.getSmqLevel() == 5) {
                                                                level = LLT;
                                                            } else if (tt.getSmqLevel() == 0) {
                                                                level = CHILD_SMQ;
                                                            }

                                                            if ((tt.getSmqLevel() != 5) || (!filterLltFlag && tt.getSmqLevel() == 5)) {
                                                                if(level.equals(PT)){
                                                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt.getPtCode() + "", tt.getPtName(), "",
                                                                            tt.getPtTermScope() + "", tt.getPtTermWeight() + "", tt.getPtTermCategory(), "", tt.getPtTermStatus()));
                                                                }

                                                                smqSearched = smqBaseService.findByCode(Long.parseLong(tt.getPtCode() + ""));

                                                                if (smqSearched != null) {
                                                                    List<SmqRelation190> test2 =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                                    System.out.println("\n ************ test2 size for " + smqSearched.getSmqName() + " = " + test2.size());

                                                                    if (test2 != null) {
                                                                        for (SmqRelation190 tt2 : test2) {
                                                                            if (tt2.getSmqLevel() == 4) {
                                                                                level = PT;
                                                                            } else if (tt2.getSmqLevel() == 5) {
                                                                                level = LLT;
                                                                            } else if (tt2.getSmqLevel() == 0) {
                                                                                level = CHILD_SMQ;
                                                                            }

                                                                            if ((tt2.getSmqLevel() != 5) || (!filterLltFlag && tt2.getSmqLevel() == 5)) {
                                                                                if(level.equals(PT)){
                                                                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt2.getPtCode() + "", tt2.getPtName(), "",
                                                                                            tt2.getPtTermScope() + "", tt2.getPtTermWeight() + "", tt2.getPtTermCategory(), "", tt2.getPtTermStatus()));
                                                                                }


                                                                                if (level.equals(CHILD_SMQ)) {
                                                                                    smqSearched = smqBaseService.findByCode(Long.parseLong(tt2.getPtCode() + ""));
                                                                                    if (smqSearched != null) {
                                                                                        List<SmqRelation190> test3 =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
                                                                                        System.out.println("\n ************ test size for " + smqSearched.getSmqName() + " = " + test3.size());

                                                                                        if (test3 != null) {
                                                                                            for (SmqRelation190 tt3 : test3) {
                                                                                                if (tt3.getSmqLevel() == 4) {
                                                                                                    level = PT;
                                                                                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt3.getPtCode() + "", tt3.getPtName(), "",
                                                                                                            tt3.getPtTermScope() + "", tt3.getPtTermWeight() + "", tt3.getPtTermCategory(), "", tt3.getPtTermStatus()));
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /**
             * HLT.
             * Iterate but don't add to response
             */
            if (relation.getHltCode() != null) {
                List<Long> hltCodesList = new ArrayList<>();
                hltCodesList.add(relation.getHltCode());
                List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
                for (MeddraDictHierarchySearchDto hlt : hlts) {
                    /**
                     * PT.
                     */
                    List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
                    List<Long> ptCodesList = new ArrayList<>();
                    for (MeddraDictHierarchySearchDto meddra : listPT) {
                        ptCodesList.add(Long.parseLong(meddra.getCode()));
                    }

                    List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
                    if (llts != null) {
                        for (MeddraDictHierarchySearchDto llt : llts) {
                            relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, llt.getCode() + "", llt.getTerm(), ""));
                        }
                    }
                    //}
                }
                //LOG.info("In {} Finished Loading HLT code relations.", this.workerName);
            }

            /**
             * PT
             */
            if (relation.getPtCode() != null) {
                //LOG.info("In {} Loading PT code relations.", this.workerName);
                List<Long> ptCodesList = new ArrayList<>();
                ptCodesList.add(relation.getPtCode());
                List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
                for (MeddraDictHierarchySearchDto pt : pts) {
                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, pt.getCode() + "", pt.getTerm(), ""));
                }
            }


            /**
             * SOC
             * Iterate but don't add to response
             */
            if (relation.getSocCode() != null) {
                //LOG.info("In {} Loading SOC code relations.", this.workerName);
                List<Long> socCodesList = new ArrayList<>();
                socCodesList.add(relation.getSocCode());
                List<MeddraDictHierarchySearchDto> socss = meddraDictService.findByCodes("SOC_", socCodesList,dictionaryVersion);
                for (MeddraDictHierarchySearchDto soc : socss) {
                    /**
                     * HLGT.
                     * Iterate but don't add to response
                     */
                    List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLGT_", "SOC_", Long.valueOf(soc.getCode()),dictionaryVersion);
                    List<Long> hlgtCodesList = new ArrayList<>();
                    for (MeddraDictHierarchySearchDto meddra : listHLGT) {
                        hlgtCodesList.add(Long.parseLong(meddra.getCode()));
                    }

                    List<MeddraDictHierarchySearchDto> hlgts = meddraDictService.findByCodes("HLGT_", hlgtCodesList,dictionaryVersion);
                    if (hlgts != null) {
                        for (MeddraDictHierarchySearchDto hlgt : hlgts) {
                            /**
                             * HLT.
                             * Iterate but don't add to response
                             */
                            List<MeddraDictHierarchySearchDto> listHLT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()),dictionaryVersion);
                            List<Long> hltCodesList = new ArrayList<>();
                            for (MeddraDictHierarchySearchDto meddra : listHLT) {
                                hltCodesList.add(Long.parseLong(meddra.getCode()));
                            }

                            List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
                            if (hlts != null) {
                                for (MeddraDictHierarchySearchDto hlt : hlts) {
                                    /**
                                     * PT.
                                     */
                                    List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
                                    List<Long> ptCodesList = new ArrayList<>();
                                    for (MeddraDictHierarchySearchDto meddra : listHT) {
                                        ptCodesList.add(Long.parseLong(meddra.getCode()));
                                    }

                                    List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
                                    if (pts != null) {
                                        for (MeddraDictHierarchySearchDto pt : pts) {
                                            relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, pt.getCode() + "", pt.getTerm(), ""));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /**
             *
             * HLGT.
             * Iterate but don't add to response
             */
            if (relation.getHlgtCode() != null) {
                List<Long> hlgtCodesList = new ArrayList<>();
                hlgtCodesList.add(relation.getHlgtCode());
                List<MeddraDictHierarchySearchDto> socDtos = meddraDictService.findByCodes("HLGT_", hlgtCodesList,dictionaryVersion);
                for (MeddraDictHierarchySearchDto hlgt : socDtos) {
                    /**
                     * HLT.
                     * Iterate but don't add to response
                     */
                    List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()),dictionaryVersion);
                    List<Long> hltCodesList = new ArrayList<>();
                    for (MeddraDictHierarchySearchDto meddra : listHLGT) {
                        hltCodesList.add(Long.parseLong(meddra.getCode()));
                    }

                    List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
                    if (hlts != null) {
                        for (MeddraDictHierarchySearchDto hlt : hlts) {
                            /**
                             * PT.
                             */
                            List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
                            List<Long> ptCodesList = new ArrayList<>();
                            for (MeddraDictHierarchySearchDto meddra : listHT) {
                                ptCodesList.add(Long.parseLong(meddra.getCode()));
                            }

                            List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
                            if (pts != null) {
                                for (MeddraDictHierarchySearchDto pt : pts) {
                                    relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(PT, pt.getCode() + "", pt.getTerm(), ""));
                                }
                            }
                        }
                    }
                }
            }
            relationsWorkerDTO.setSuccess(true);
            LOG.info("In {} Finished Callable.", this.workerName);
        } catch (Exception e) {
            relationsWorkerDTO.setSuccess(false);
            LOG.error("In {} Exception occured while processing", this.workerName, e);
        }
        return relationsWorkerDTO;
    }
}
