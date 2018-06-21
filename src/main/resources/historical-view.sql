select distinct
  cmq_base.cmq_code as cmqCode,
  cmq_base.list_name as listName,
  cmq_base.list_type as listType,
  cmq_base.product,
  cmq_base.drug_program as drugProgram,
  cmq_base.protocol_number as protocolNumber,
  cmq_base.list_level as listLevel,
  cmq_parent_child.parent_list as parentListName,
  cmq_base.status,
  cmq_base.state,
  cmq_base.creation_date as creationDate,
  cmq_base.created_by as createdBy,
  cmq_base.algorithm,
  cmq_base.last_activation_date,
  cmq_base.last_activation_date as lastActivationDate,
  cmq_base.last_activation_by as lastActivationBy,
  cmq_base.description,
  cmq_relations.term,
  cmq_relations.term_dict_level as termDictLevel,
  cmq_relations.term_code as termCode,
  cmq_relations.scope as termScope,
  cmq_base.dictionary_version as dictionaryVersion,
  cmq_base.designee,
  cmq_base.designee2,
  cmq_base.designee3,
  cmq_base.medical_concept as medicalConcept
from
  (select 
    nvl(cba.cmq_code_old,cba.cmq_code_new) "CMQ_CODE",
    nvl(cba.cmq_name_old,cba.cmq_name_new) "LIST_NAME",
     (select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
     where 
         codelist_configuration_type ='LIST_EXTENSION_TYPES'
     and (codelist_internal_value = nvl(cba.cmq_type_cd_old,cba.cmq_type_cd_new)) 
     and active_flag='Y'
     UNION ALL
     select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
     where
        active_flag='N'
        and codelist_configuration_type ='LIST_EXTENSION_TYPES'
        and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='LIST_EXTENSION_TYPES'
             and (codelist_internal_value = nvl(cba.cmq_type_cd_old,cba.cmq_type_cd_new))
             and active_flag='N')
        and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='LIST_EXTENSION_TYPES'
              and (codelist_internal_value = nvl(cba.cmq_type_cd_old,cba.cmq_type_cd_new))
              and active_flag='Y')
     ) "LIST_TYPE",    
    (select DISTINCT codelist_value from opencqt.ref_config_codelist 
     where codelist_configuration_type ='PRODUCT'
     and codelist_internal_value = cpb_upsert.cmq_product_cd_new
     and active_flag='Y'
     UNION ALL
     select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
     where
        active_flag='N'
        and codelist_configuration_type ='PRODUCT'
        and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='PRODUCT'
             and codelist_internal_value = cpb_upsert.cmq_product_cd_new
             and active_flag='N')
        and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='PRODUCT'
              and codelist_internal_value = cpb_upsert.cmq_product_cd_new
              and active_flag='Y')
    ) "PRODUCT",
    (select DISTINCT codelist_value from opencqt.ref_config_codelist 
     where codelist_configuration_type ='PROGRAM'
     and codelist_internal_value = nvl(cba.cmq_program_cd_new,cba.cmq_program_cd_old)
     and active_flag='Y'
     UNION ALL
     select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
     where
        active_flag='N'
        and codelist_configuration_type ='PROGRAM'
        and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='PROGRAM'
             and codelist_internal_value = nvl(cba.cmq_program_cd_new,cba.cmq_program_cd_old)
             and active_flag='N')
        and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='PROGRAM'
              and codelist_internal_value = nvl(cba.cmq_program_cd_new,cba.cmq_program_cd_old)
              and active_flag='Y')
    ) "DRUG_PROGRAM",
    (select DISTINCT codelist_value from opencqt.ref_config_codelist 
     where codelist_configuration_type ='PROTOCOL'
     and codelist_internal_value = nvl(cba.cmq_protocol_cd_new,cba.cmq_protocol_cd_old)
     and active_flag='Y'
     UNION ALL
     select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
     where
        active_flag='N'
        and codelist_configuration_type ='PROTOCOL'
        and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='PROTOCOL'
             and codelist_internal_value = nvl(cba.cmq_protocol_cd_new,cba.cmq_protocol_cd_old)
             and active_flag='N')
        and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='PROTOCOL'
              and codelist_internal_value = nvl(cba.cmq_protocol_cd_new,cba.cmq_protocol_cd_old)
              and active_flag='Y')
    ) "PROTOCOL_NUMBER",   
    nvl(cba.cmq_level_new,cba.cmq_level_old) "LIST_LEVEL",
    --nvl(cba.cmq_parent_name_new,cba.cmq_parent_name_old) "PARENT_LIST_NAME",
    nvl(cba.cmq_algorithm_new,cba.cmq_algorithm_old) "ALGORITHM", 
    nvl(cba.activation_date_new,cba.activation_date_old) "LAST_ACTIVATION_DATE",
    nvl(cba.activated_by_new,cba.activated_by_old) "LAST_ACTIVATION_BY",
    (select max(created_by) 
       from opencqt.cmq_base_&&MedDRAAuditVersion.
       where cmq_id = cba.cmq_id) "CREATED_BY",
    (select max(creation_date)
       from opencqt.cmq_base_&&MedDRAAuditVersion.
       where cmq_id = cba.cmq_id) "CREATION_DATE",
    nvl(cba.cmq_status_new,cba.cmq_status_old) "STATUS",
    nvl(cba.cmq_state_new,cba.cmq_state_old) "STATE",
    nvl(cba.cmq_designee_new,cba.cmq_designee_old) "DESIGNEE",
    nvl(cba.cmq_designee2_new,cba.cmq_designee2_old) "DESIGNEE2",
    nvl(cba.cmq_designee3_new,cba.cmq_designee3_old) "DESIGNEE3",
    nvl(cba.cmq_description_new,cba.cmq_description_old) "DESCRIPTION",
    substr(nvl(cba.DICTIONARY_VERSION_old,cba.DICTIONARY_VERSION_new),1,2)||'.'||substr(nvl(cba.DICTIONARY_VERSION_old,cba.DICTIONARY_VERSION_new),3,1) "DICTIONARY_VERSION",
    (select DISTINCT nvl(codelist_value,'No Group') from opencqt.ref_config_codelist 
     where codelist_configuration_type ='GROUP'
     and codelist_internal_value = nvl(cba.cmq_group_old,cba.cmq_group_new)
     and active_flag='Y'
     UNION ALL
     select DISTINCT nvl(codelist_internal_value,'No Group') from opencqt.ref_config_codelist 
     where
        active_flag='N'
        and codelist_configuration_type ='GROUP'
        and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='GROUP'
             and codelist_internal_value = nvl(cba.cmq_group_old,cba.cmq_group_new)
             and active_flag='N')
        and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='GROUP'
              and codelist_internal_value = nvl(cba.cmq_group_old,cba.cmq_group_new)
              and active_flag='Y')
    ) "MEDICAL_CONCEPT"
   from 
      (select 
           cmq_code_new, cmq_product_cd_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_product_base_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, cmq_product_cd_new
      ) cpb_upsert,
     (select 
           cmq_code_old, cmq_product_cd_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_product_base_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, cmq_product_cd_old
      ) cpb_delete,
      opencqt.cmq_base_&&MedDRAAuditVersion._audit cba
   where 
          cba.cmq_code_new = cpb_upsert.cmq_code_new
      and cpb_upsert.cmq_code_new = cpb_delete.cmq_code_old (+)
      and cpb_upsert.cmq_product_cd_new = cpb_delete.cmq_product_cd_old (+)
      and cpb_upsert.max_audit_timestamp >=
         nvl(cpb_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
      and cba.audit_timestamp = 
         (select max(audit_timestamp) 
          from opencqt.cmq_base_&&MedDRAAuditVersion._audit 
          where
              cmq_code_new = cba.cmq_code_new
              and cmq_code_new is NOT NULL
              and transaction_type in ('U','I')
              and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
          )
      and not exists
         (select 1 from 
              opencqt.cmq_base_&&MedDRAAuditVersion._audit
           where
              cmq_code_old = cba.cmq_code_new
           and audit_timestamp <=
               to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
           and audit_timestamp >=
               (select max(audit_timestamp) 
                from opencqt.cmq_base_&&MedDRAAuditVersion._audit 
                where
                    --cmq_code_new = cba.cmq_parent_code_new
                    cmq_code_new is NOT NULL
                    and transaction_type in ('U','I')
                    and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
               )
           and transaction_type = 'D'
         )
      and cba.transaction_type in ('U','I')  
  ) cmq_base,
 (  select distinct
      md.soc_term "TERM",
      md.soc_code "TERM_CODE",
      'SOC' "TERM_DICT_LEVEL",
      nvl(cra_delete.term_scope_old,cra_upsert.term_scope_new) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, soc_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, soc_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, soc_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, soc_code_old, term_scope_old
      ) cra_delete,
      opencqt.meddra_dict_&&MedDRAAuditVersion md
   where 
          md.soc_code = cra_upsert.soc_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.soc_code_new = cra_delete.soc_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
   UNION ALL
  select distinct
      md.hlgt_term "TERM",
      md.hlgt_code "TERM_CODE",
      'HLGT' "TERM_DICT_LEVEL",
      nvl(cra_delete.term_scope_old,cra_upsert.term_scope_new) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, hlgt_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, hlgt_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, hlgt_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, hlgt_code_old, term_scope_old
      ) cra_delete,
      opencqt.meddra_dict_&&MedDRAAuditVersion md
   where 
          md.hlgt_code = cra_upsert.hlgt_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.hlgt_code_new = cra_delete.hlgt_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
   UNION ALL  
  select distinct
      md.hlt_term "TERM",
      md.hlt_code "TERM_CODE",
      'HLT' "TERM_DICT_LEVEL",
      nvl(cra_delete.term_scope_old,cra_upsert.term_scope_new) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, hlt_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, hlt_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, hlt_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, hlt_code_old, term_scope_old
      ) cra_delete,
      opencqt.meddra_dict_&&MedDRAAuditVersion md
   where 
          md.hlt_code = cra_upsert.hlt_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.hlt_code_new = cra_delete.hlt_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
   UNION ALL
  select distinct
      md.pt_term "TERM",
      md.pt_code "TERM_CODE",
      'PT' "TERM_DICT_LEVEL",
      nvl(cra_delete.term_scope_old,cra_upsert.term_scope_new) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, pt_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, pt_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, pt_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, pt_code_old, term_scope_old
      ) cra_delete,
      opencqt.meddra_dict_&&MedDRAAuditVersion md
   where 
          md.pt_code = cra_upsert.pt_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.pt_code_new = cra_delete.pt_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
   UNION ALL
  select distinct
      md.llt_term "TERM",
      md.llt_code "TERM_CODE",
      'LLT' "TERM_DICT_LEVEL",
      nvl(cra_delete.term_scope_old,cra_upsert.term_scope_new) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, llt_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, llt_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, llt_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, llt_code_old, term_scope_old
      ) cra_delete,
      opencqt.meddra_dict_&&MedDRAAuditVersion md
   where 
          md.llt_code = cra_upsert.llt_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.llt_code_new = cra_delete.llt_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
   UNION ALL
  select distinct
      md.smq_name "TERM",
      trim(to_char(md.smq_code)) "TERM_CODE",
      'SMQ'||md.SMQ_LEVEL "TERM_DICT_LEVEL",
      (select DISTINCT codelist_value from opencqt.ref_config_codelist 
       where codelist_configuration_type ='SMQ_FILTER_LEVELS'
       and codelist_internal_value = cra_upsert.term_scope_new 
       and active_flag='Y'
       UNION ALL
       select DISTINCT codelist_internal_value from opencqt.ref_config_codelist 
       where
          active_flag='N'
          and codelist_configuration_type ='SMQ_FILTER_LEVELS'
          and serialnum = 
            (select max(serialnum) from opencqt.ref_config_codelist
             where codelist_configuration_type ='SMQ_FILTER_LEVELS'
             and codelist_internal_value = cra_upsert.term_scope_new
             and active_flag='N')
          and not exists
             (select 1 from opencqt.ref_config_codelist
              where codelist_configuration_type ='SMQ_FILTER_LEVELS'
              and codelist_internal_value = cra_upsert.term_scope_new
              and active_flag='Y')
       ) "SCOPE",
      cra_upsert.cmq_code_new "CMQ_CODE",
      cra_upsert.max_audit_timestamp "MAX_UPSERT_AUDIT_TS"
   from 
      (select 
           cmq_code_new, smq_code_new, term_scope_new, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_new = &&CMQCodeForAudit
        and transaction_type in ('I','U')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_new, smq_code_new, term_scope_new
      ) cra_upsert,
     (select 
           cmq_code_old, smq_code_old, term_scope_old, max(audit_timestamp) max_audit_timestamp
      from opencqt.cmq_relations_&&MedDRAAuditVersion._audit 
      where 
            cmq_code_old = &&CMQCodeForAudit 
	and transaction_type in ('D')
        and audit_timestamp <= to_date('&&CMQAuditTimestamp','DD-MON-RRRR:HH24:MI:SS')
      group by 
            cmq_code_old, smq_code_old, term_scope_old
      ) cra_delete,
      opencqt.smq_base_&&MedDRAAuditVersion md
   where 
          md.smq_code = cra_upsert.smq_code_new 
      and cra_upsert.cmq_code_new = cra_delete.cmq_code_old (+)
      and cra_upsert.smq_code_new = cra_delete.smq_code_old (+)
      and cra_upsert.max_audit_timestamp >=
         nvl(cra_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
     ) cmq_relations,
     
     
     (select NVL(cpc_upsert.cmq_parent_name_new,cpc_delete.cmq_parent_name_old) parent_list,cpc_upsert.cmq_code cmq_code
   from 
        (select cmq_parent_name_new,cmq_child_code_new cmq_code,max(audit_timestamp) max_audit_timestamp
        from cmq_parent_child_&&MedDRAAuditVersion._audit 
        where child_cmq_id=(select cmq_id from cmq_base_&&MedDRAAuditVersion. where cmq_code=&&CMQCodeForAudit)
        and transaction_type in ('I','U')
        and audit_timestamp <= :CMQAuditTimestamp
        group by cmq_parent_name_new, cmq_child_code_new)cpc_upsert,
        
        (select cmq_parent_name_old,cmq_child_code_old cmq_code,max(audit_timestamp) max_audit_timestamp 
        from cmq_parent_child_&&MedDRAAuditVersion._audit 
        where child_cmq_id=(select cmq_id from cmq_base_&&MedDRAAuditVersion. where cmq_code=&&CMQCodeForAudit)
        and transaction_type in ('D')
        and audit_timestamp <= :CMQAuditTimestamp
        group by cmq_parent_name_old, cmq_child_code_old)cpc_delete
        
   where 
      cpc_upsert.cmq_parent_name_new= cpc_delete.cmq_parent_name_old (+)
      and cpc_upsert.max_audit_timestamp >=
         nvl(cpc_delete.max_audit_timestamp,to_date('1-JAN-1900:00:00:00','DD-MON-RRRR:HH24:MI:SS'))
         )cmq_parent_child
 
  where cmq_base.cmq_code = cmq_relations.cmq_code (+)
  and cmq_base.cmq_code = cmq_parent_child.cmq_code (+)
  and cmq_base.cmq_code = &&CMQCodeForAudit
  