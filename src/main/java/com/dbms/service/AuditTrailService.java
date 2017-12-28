package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.dtos.AuditTrailDto;
import com.dbms.entity.cqt.dtos.CmqBaseDTO;
import com.dbms.util.ICqtEntityManagerFactory;

@ManagedBean(name = "AuditTrailService")
@ApplicationScoped
public class AuditTrailService implements IAuditTrailService{
	
	@ManagedProperty(value = "#{CqtEntityManagerFactory}")
	private ICqtEntityManagerFactory cqtEntityManagerFactory;
	private static final Logger LOG = LoggerFactory.getLogger(AuditTrailService.class);

	@Override
	public List<AuditTrailDto> findByCriterias(Long listCode, int dictionaryVersion,
			String auditTimeStampString) {
		List<AuditTrailDto> retVal = null;
		//String auditTimeStampString = new SimpleDateFormat("DD-MON-YYYY:HH24:MI:SS").format(auditTimeStamp);
		//String auditTimeStampString = auditTimeStamp;
 		String queryString = "select "
 				+"   cmq_all_audit.TABLE_NAME tableName "
 				+" , cmq_all_audit.COLUMN_NAME columnName "
 				+" , cmq_all_audit.TRANSACTION_ID transactionId "
 				+" , CASE WHEN cmq_all_audit.TABLE_NAME in ( 'CMQ_BASE_"+dictionaryVersion+"_AUDIT') THEN  "
 				+"	case when TRANSACTION_TYPE = 'I' THEN "
 				+"	 'New List (CMQ Code) added' "
 				+"	     when TRANSACTION_TYPE = 'U' THEN "
 				+"	 'List (CMQ Base) data updated' "
 				+"	 when TRANSACTION_TYPE = 'D' THEN "
 				+"	 'List (CMQ Code) Deleted' "
 				+"	 end "
 				+"	 WHEN TABLE_NAME IN( 'CMQ_PRODUCT_BASE_"+dictionaryVersion+"_AUDIT') THEN  "
 				+"	case when TRANSACTION_TYPE = 'I' THEN "
 				+"	 'New Product Inserted' "
 				+"	     when TRANSACTION_TYPE = 'U' THEN "
 				+"	 'Product Updated' "
 				+"	 when TRANSACTION_TYPE = 'D' THEN "
 				+"	 'Product Deleted' "
 				+"	 end "
 				+"	 WHEN TABLE_NAME IN ('CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT') THEN  "
 				+"	case when TRANSACTION_TYPE = 'I' THEN "
 				+"	 'New Relation Inserted' "
 				+"	     when TRANSACTION_TYPE = 'U' THEN "
 				+"	 'Relation Updated' "
 				+"	 when TRANSACTION_TYPE = 'D' THEN "
 				+"	 'Relation Deleted' "
 				+"	 end "
 				+"	 WHEN TABLE_NAME IN ('CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT') AND COLUMN_NAME IN ('CMQ_CHILD_CODE','CMQ_CHILD_NAME') THEN  "
 				+"	case when TRANSACTION_TYPE = 'I' THEN "
 				+"	 'New CHILD Inserted' "
 				+"	     when TRANSACTION_TYPE = 'U' THEN "
 				+"	 'CHILD Updated' "
 				+"	 when TRANSACTION_TYPE = 'D' THEN "
 				+"	 'CHILD Deleted' "
 				+"	 end "
 				+"	 WHEN TABLE_NAME IN ('CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT') AND COLUMN_NAME IN ('CMQ_PARENT_CODE','CMQ_PARENT_NAME') THEN  "
 				+"	case when TRANSACTION_TYPE = 'I' THEN "
 				+"	 'New PARENT Inserted' "
 				+"	     when TRANSACTION_TYPE = 'U' THEN "
 				+"	 'PARENT Updated' "
 				+"	 when TRANSACTION_TYPE = 'D' THEN "
 				+"	 'PARENT Deleted' "
 				+"	 end "
 				+"	 end transactionType "
 				+" , cb.CMQ_CODE cmqCode "
 				+" , cmq_all_audit.OLD oldValue "
 				+" , cmq_all_audit.NEW newValue "
 				+" , cmq_all_audit.FIRST_NAME firstName "
 				+" , cmq_all_audit.LAST_NAME lastName "
 				+" , cmq_all_audit.USERID userId "
 				+" , cmq_all_audit.GROUP_NAME groupName "
 				+" , cmq_all_audit.AUDIT_TIMESTAMP auditTimestamp "
 				+"from "
 				+"  (SELECT CMQ_ID "
 				+"  , TO_CHAR(DICTIONARY_VERSION_New) NEW "
 				+"  , TO_CHAR(DICTIONARY_VERSION_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'DICTIONARY_VERSION' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(DICTIONARY_VERSION_New),'xoxox') <> NVL(TO_CHAR(DICTIONARY_VERSION_old),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_CODE_New) NEW "
 				+"  , TO_CHAR(CMQ_CODE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_CODE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_CODE_New),'xoxox') <> NVL(TO_CHAR(CMQ_CODE_old),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_NAME_New) NEW "
 				+"  , TO_CHAR(CMQ_NAME_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_NAME' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_NAME_New),'xoxox') <> NVL(TO_CHAR(CMQ_NAME_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_TYPE_CD_New) NEW "
 				+"  , TO_CHAR(CMQ_TYPE_CD_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_TYPE_CD' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_TYPE_CD_New),'xoxox') <> NVL(TO_CHAR(CMQ_TYPE_CD_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_LEVEL_New) NEW "
 				+"  , TO_CHAR(CMQ_LEVEL_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_LEVEL' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_LEVEL_New),'xoxox') <> NVL(TO_CHAR(CMQ_LEVEL_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT PARENT_CMQ_ID CMQ_ID "
 				+"  , TO_CHAR(CMQ_CHILD_CODE_New) NEW "
 				+"  , TO_CHAR(CMQ_CHILD_CODE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_CHILD_CODE' column_name "
 				+"  ,'CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_CHILD_CODE_New),'xoxox') <> NVL(TO_CHAR(CMQ_CHILD_CODE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.PARENT_CMQ_ID) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT PARENT_CMQ_ID CMQ_ID "
 				+"  , TO_CHAR(CMQ_CHILD_NAME_New) NEW "
 				+"  , TO_CHAR(CMQ_CHILD_NAME_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_CHILD_NAME' column_name "
 				+"  ,'CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_CHILD_NAME_New),'xoxox') <> NVL(TO_CHAR(CMQ_CHILD_NAME_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.PARENT_CMQ_ID) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"	UNION "
 				+"	SELECT CHILD_CMQ_ID CMQ_ID "
 				+"  , TO_CHAR(CMQ_PARENT_CODE_New) NEW "
 				+"  , TO_CHAR(CMQ_PARENT_CODE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_PARENT_CODE' column_name "
 				+"  ,'CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_PARENT_CODE_New),'xoxox') <> NVL(TO_CHAR(CMQ_PARENT_CODE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.CHILD_CMQ_ID) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CHILD_CMQ_ID CMQ_ID "
 				+"  , TO_CHAR(CMQ_PARENT_NAME_New) NEW "
 				+"  , TO_CHAR(CMQ_PARENT_NAME_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_PARENT_NAME' column_name "
 				+"  ,'CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_PARENT_CHILD_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_PARENT_NAME_New),'xoxox') <> NVL(TO_CHAR(CMQ_PARENT_NAME_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.CHILD_CMQ_ID) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_DESCRIPTION_New) NEW "
 				+"  , TO_CHAR(CMQ_DESCRIPTION_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_DESCRIPTION' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_DESCRIPTION_New),'xoxox') <> NVL(TO_CHAR(CMQ_DESCRIPTION_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_STATUS_New) NEW "
 				+"  , TO_CHAR(CMQ_STATUS_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_STATUS' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_STATUS_New),'xoxox') <> NVL(TO_CHAR(CMQ_STATUS_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_STATE_New) NEW "
 				+"  , TO_CHAR(CMQ_STATE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_STATE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_STATE_New),'xoxox') <> NVL(TO_CHAR(CMQ_STATE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_SOURCE_New) NEW "
 				+"  , TO_CHAR(CMQ_SOURCE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_SOURCE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_SOURCE_New),'xoxox') <> NVL(TO_CHAR(CMQ_SOURCE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_NOTE_New) NEW "
 				+"  , TO_CHAR(CMQ_NOTE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_NOTE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_NOTE_New),'xoxox') <> NVL(TO_CHAR(CMQ_NOTE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_ALGORITHM_New) NEW "
 				+"  , TO_CHAR(CMQ_ALGORITHM_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_ALGORITHM' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_ALGORITHM_New),'xoxox') <> NVL(TO_CHAR(CMQ_ALGORITHM_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"    , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='PROGRAM' "
 				+"     and codelist_internal_value = cba.cmq_program_cd_new "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PROGRAM' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PROGRAM' "
 				+"             and codelist_internal_value = cba.cmq_program_cd_new "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PROGRAM' "
 				+"              and codelist_internal_value = cba.cmq_program_cd_new "
 				+"              and active_flag='Y') "
 				+"      ) NEW "
 				+"  ,  (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='PROGRAM' "
 				+"     and codelist_internal_value = cba.cmq_program_cd_old "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PROGRAM' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PROGRAM' "
 				+"             and codelist_internal_value = cba.cmq_program_cd_old "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PROGRAM' "
 				+"              and codelist_internal_value = cba.cmq_program_cd_old "
 				+"              and active_flag='Y') "
 				+"      ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_PROGRAM_CD' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_PROGRAM_CD_New),'xoxox') <> NVL(TO_CHAR(CMQ_PROGRAM_CD_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='PROTOCOL' "
 				+"     and codelist_internal_value = cba.cmq_protocol_cd_new "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PROTOCOL' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PROTOCOL' "
 				+"             and codelist_internal_value = cba.cmq_protocol_cd_new "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PROTOCOL' "
 				+"              and codelist_internal_value = cba.cmq_protocol_cd_new "
 				+"              and active_flag='Y') "
 				+"    ) NEW "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='PROTOCOL' "
 				+"     and codelist_internal_value = cba.cmq_protocol_cd_old "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PROTOCOL' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PROTOCOL' "
 				+"             and codelist_internal_value = cba.cmq_protocol_cd_old "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PROTOCOL' "
 				+"              and codelist_internal_value = cba.cmq_protocol_cd_old "
 				+"              and active_flag='Y') "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_PROTOCOL_CD' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_PROTOCOL_CD_New),'xoxox') <> NVL(TO_CHAR(CMQ_PROTOCOL_CD_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_DESIGNEE_New) NEW "
 				+"  , TO_CHAR(CMQ_DESIGNEE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_DESIGNEE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_DESIGNEE_New),'xoxox') <> NVL(TO_CHAR(CMQ_DESIGNEE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_DESIGNEE2_New) NEW "
 				+"  , TO_CHAR(CMQ_DESIGNEE2_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_DESIGNEE2' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_DESIGNEE2_New),'xoxox') <> NVL(TO_CHAR(CMQ_DESIGNEE2_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_DESIGNEE3_New) NEW "
 				+"  , TO_CHAR(CMQ_DESIGNEE3_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_DESIGNEE3' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_DESIGNEE3_New),'xoxox') <> NVL(TO_CHAR(CMQ_DESIGNEE3_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_GROUP_New) NEW "
 				+"  , TO_CHAR(CMQ_GROUP_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_GROUP' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_GROUP_New),'xoxox') <> NVL(TO_CHAR(CMQ_GROUP_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_DUE_DATE_New) NEW "
 				+"  , TO_CHAR(CMQ_DUE_DATE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_DUE_DATE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_DUE_DATE_New),'xoxox') <> NVL(TO_CHAR(CMQ_DUE_DATE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_WF_DESC_New) NEW "
 				+"  , TO_CHAR(CMQ_WF_DESC_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_WF_DESC' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_WF_DESC_New),'xoxox') <> NVL(TO_CHAR(CMQ_WF_DESC_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(IMPACT_TYPE_New) NEW "
 				+"  , TO_CHAR(IMPACT_TYPE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'IMPACT_TYPE' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(IMPACT_TYPE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(DICTIONARY_NAME_New) NEW "
 				+"  , TO_CHAR(DICTIONARY_NAME_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'DICTIONARY_NAME' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(DICTIONARY_NAME_New),'xoxox') <> NVL(TO_CHAR(DICTIONARY_NAME_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_SUBVERSION_New) NEW "
 				+"  , TO_CHAR(CMQ_SUBVERSION_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_SUBVERSION' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_SUBVERSION_New),'xoxox') <> NVL(TO_CHAR(CMQ_SUBVERSION_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_APPROVE_REASON_New) NEW "
 				+"  , TO_CHAR(CMQ_APPROVE_REASON_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_APPROVE_REASON' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_APPROVE_REASON_New),'xoxox') <> NVL(TO_CHAR(CMQ_APPROVE_REASON_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_CRITICAL_EVENT_New) NEW "
 				+"  , TO_CHAR(CMQ_CRITICAL_EVENT_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_CRITICAL_EVENT' column_name "
 				+"  ,'CMQ_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_CRITICAL_EVENT_New),'xoxox') <> NVL(TO_CHAR(CMQ_CRITICAL_EVENT_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type = 'PRODUCT' "
 				+"     and codelist_internal_value = cba.cmq_product_cd_new "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PRODUCT' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PRODUCT' "
 				+"             and codelist_internal_value = cba.cmq_product_cd_new "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PRODUCT' "
 				+"              and codelist_internal_value = cba.cmq_product_cd_new "
 				+"              and active_flag='Y') "
 				+"    ) NEW "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type = 'PRODUCT' "
 				+"     and codelist_internal_value = cba.cmq_product_cd_old "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type ='PRODUCT' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type ='PRODUCT' "
 				+"             and codelist_internal_value = cba.cmq_product_cd_old "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type ='PRODUCT' "
 				+"              and codelist_internal_value = cba.cmq_product_cd_old "
 				+"              and active_flag='Y') "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_PRODUCT_CD' column_name "
 				+"  ,'CMQ_PRODUCT_BASE_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_PRODUCT_BASE_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_PRODUCT_CD_New),'xoxox') <> NVL(TO_CHAR(CMQ_PRODUCT_CD_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(CMQ_CODE_New) NEW "
 				+"  , TO_CHAR(CMQ_CODE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'CMQ_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(CMQ_CODE_New),'xoxox') <> NVL(TO_CHAR(CMQ_CODE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct soc_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where soc_code = cba.soc_code_new "
 				+"    ) NEW "
 				+"  , (select distinct soc_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where soc_code = cba.soc_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'SOC_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(SOC_CODE_New),'xoxox') <> NVL(TO_CHAR(SOC_CODE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct hlgt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlgt_code = cba.hlgt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct hlgt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlgt_code = cba.hlgt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'HLGT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(HLGT_CODE_New),'xoxox') <> NVL(TO_CHAR(HLGT_CODE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct hlt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlt_code = cba.hlt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct hlt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlt_code = cba.hlt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'HLT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(HLT_CODE_New),'xoxox') <> NVL(TO_CHAR(HLT_CODE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct pt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where pt_code = cba.pt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct pt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where pt_code = cba.pt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'PT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(PT_CODE_New),'xoxox') <> NVL(TO_CHAR(PT_CODE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct llt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where llt_code = cba.llt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct llt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where llt_code = cba.llt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'LLT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(LLT_CODE_New),'xoxox') <> NVL(TO_CHAR(LLT_CODE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct smq_name  "
 				+"       from SMQ_BASE_"+dictionaryVersion+" "
 				+"      where smq_code = cba.smq_code_new "
 				+"    ) NEW "
 				+"  , (select distinct smq_name  "
 				+"       from SMQ_BASE_"+dictionaryVersion+" "
 				+"      where smq_code = cba.smq_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'SMQ_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(SMQ_CODE_New),'xoxox') <> NVL(TO_CHAR(SMQ_CODE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(TERM_WEIGHT_New) NEW "
 				+"  , TO_CHAR(TERM_WEIGHT_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'TERM_WEIGHT' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(TERM_WEIGHT_New),'xoxox') <> NVL(TO_CHAR(TERM_WEIGHT_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(TERM_CATEGORY_New) NEW "
 				+"  , TO_CHAR(TERM_CATEGORY_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'TERM_CATEGORY' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(TERM_CATEGORY_New),'xoxox') <> NVL(TO_CHAR(TERM_CATEGORY_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='SMQ_FILTER_LEVELS' "
 				+"     and codelist_internal_value = cba.TERM_SCOPE_NEW "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"             and codelist_internal_value = cba.TERM_SCOPE_NEW "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"              and codelist_internal_value = cba.TERM_SCOPE_NEW "
 				+"              and active_flag='Y') "
 				+"      ) NEW "
 				+"  , (select DISTINCT codelist_value from ref_config_codelist  "
 				+"     where codelist_configuration_type ='SMQ_FILTER_LEVELS' "
 				+"     and codelist_internal_value = cba.TERM_SCOPE_OLD "
 				+"     and active_flag='Y' "
 				+"     UNION ALL "
 				+"     select DISTINCT codelist_internal_value from ref_config_codelist  "
 				+"     where "
 				+"        active_flag='N' "
 				+"        and codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"        and serialnum =  "
 				+"            (select max(serialnum) from ref_config_codelist "
 				+"             where codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"             and codelist_internal_value = cba.TERM_SCOPE_OLD "
 				+"             and active_flag='N') "
 				+"        and not exists "
 				+"             (select 1 from ref_config_codelist "
 				+"              where codelist_configuration_type = 'SMQ_FILTER_LEVELS' "
 				+"              and codelist_internal_value = cba.TERM_SCOPE_OLD "
 				+"              and active_flag='Y') "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'TERM_SCOPE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(TERM_SCOPE_New),'xoxox') <> NVL(TO_CHAR(TERM_SCOPE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(RELATION_TYPE_New) NEW "
 				+"  , TO_CHAR(RELATION_TYPE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'RELATION_TYPE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_TYPE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , TO_CHAR(RELATION_IMPACT_TYPE_New) NEW "
 				+"  , TO_CHAR(RELATION_IMPACT_TYPE_old) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'RELATION_IMPACT_TYPE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"      and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct llt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where llt_code = cba.llt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct llt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where llt_code = cba.llt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'LLT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"  AND (trim(LLT_CODE_NEW||LLT_CODE_OLD) IS NOT NULL) "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct pt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where pt_code = cba.pt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct pt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where pt_code = cba.pt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'PT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"  AND (trim(PT_CODE_NEW||PT_CODE_OLD) IS NOT NULL) "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct hlt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlt_code = cba.hlt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct hlt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlt_code = cba.hlt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'HLT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  AND (trim(HLT_CODE_NEW||HLT_CODE_OLD) IS NOT NULL) "
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct hlgt_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlgt_code = cba.hlgt_code_new "
 				+"    ) NEW "
 				+"  , (select distinct hlgt_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where hlgt_code = cba.hlgt_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'HLGT_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"  AND (trim(HLGT_CODE_NEW||HLGT_CODE_OLD) IS NOT NULL) "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct soc_term  "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where soc_code = cba.soc_code_new "
 				+"    ) NEW "
 				+"  , (select distinct soc_term "
 				+"       from meddra_dict_"+dictionaryVersion+" "
 				+"      where soc_code = cba.soc_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'SOC_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"  AND (trim(SOC_CODE_NEW||SOC_CODE_OLD) IS NOT NULL) "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  UNION "
 				+"  SELECT CMQ_ID "
 				+"  , (select distinct smq_name  "
 				+"       from SMQ_BASE_"+dictionaryVersion+" "
 				+"      where smq_code = cba.smq_code_new "
 				+"    ) NEW "
 				+"  , (select distinct smq_name  "
 				+"       from SMQ_BASE_"+dictionaryVersion+" "
 				+"      where smq_code = cba.smq_code_old "
 				+"    ) OLD "
 				+"  , TRANSACTION_ID "
 				+"  , TRANSACTION_TYPE "
 				+"  , FIRST_NAME "
 				+"  , LAST_NAME "
 				+"  , USERID "
 				+"  , GROUP_NAME "
 				+"  , AUDIT_TIMESTAMP "
 				+"  ,'SMQ_CODE' column_name "
 				+"  ,'CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT' table_name "
 				+"  FROM CMQ_RELATIONS_"+dictionaryVersion+"_AUDIT cba "
 				+"  WHERE NVL(TO_CHAR(RELATION_IMPACT_TYPE_New),'xoxox') <> NVL(TO_CHAR(RELATION_IMPACT_TYPE_OLD),'xoxox') "
 				+"  AND (trim(SMQ_CODE_NEW||SMQ_CODE_OLD) IS NOT NULL) "
 				+"    and exists "
 				+"        (select 1 from "
 				+"         CMQ_BASE_"+dictionaryVersion+" "
 				+"         where cmq_code = :listCode "
 				+"         and cmq_id = cba.cmq_id) "
 				+"    and audit_timestamp <= :auditTimeStampString"
 				+"  ) cmq_all_audit, "
 				+"  cmq_base_"+dictionaryVersion+" cb "
 				+"  where  "
 				+"        cb.cmq_code=:listCode "
 				+"    and cmq_all_audit.cmq_id = cb.cmq_id "
 				+"  order by  "
 				+"    cmq_all_audit.TRANSACTION_ID, "
 				+"    cmq_all_audit.AUDIT_TIMESTAMP,  "
 				+"    cmq_all_audit.TABLE_NAME, "
 				+"    cmq_all_audit.COLUMN_NAME";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("tableName", StandardBasicTypes.STRING);
			query.addScalar("columnName", StandardBasicTypes.STRING);
			query.addScalar("transactionId", StandardBasicTypes.LONG);
			query.addScalar("transactionType", StandardBasicTypes.STRING);
			query.addScalar("cmqCode", StandardBasicTypes.LONG);
			query.addScalar("oldValue", StandardBasicTypes.STRING);
			query.addScalar("newValue", StandardBasicTypes.STRING);
			query.addScalar("firstName", StandardBasicTypes.STRING);
			query.addScalar("lastName", StandardBasicTypes.STRING);
			query.addScalar("userId", StandardBasicTypes.STRING);
			query.addScalar("groupName", StandardBasicTypes.STRING);
			query.addScalar("auditTimestamp", StandardBasicTypes.TIMESTAMP);
			
			if (!StringUtils.isBlank(auditTimeStampString)) {
				 //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("DD-MMM-YYYY:HH:mm:ss");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				 LocalDateTime date = LocalDateTime.parse(auditTimeStampString, formatter);
				 Timestamp timestamp = Timestamp.valueOf(date);
				 query.setParameter("auditTimeStampString", timestamp);
			}
			if (null!=listCode) {
				query.setParameter("listCode", listCode);
			}
			 
			//query.setFetchSize(400);
 			query.setResultTransformer(Transformers.aliasToBean(AuditTrailDto.class));
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findByCriterias on AuditTrail ")
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public ICqtEntityManagerFactory getCqtEntityManagerFactory() {
		return cqtEntityManagerFactory;
	}

	public void setCqtEntityManagerFactory(ICqtEntityManagerFactory cqtEntityManagerFactory) {
		this.cqtEntityManagerFactory = cqtEntityManagerFactory;
	}

	@Override
	public List<String> findAuditTimestamps(int dictionaryVersion) {
		List<String> retVal = null;
		String queryString = "select distinct AUDIT_TIMESTAMP from CMQ_BASE_"+dictionaryVersion+"_AUDIT order by AUDIT_TIMESTAMP";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("AUDIT_TIMESTAMP", StandardBasicTypes.STRING);
			 
			//query.setFetchSize(400);
 			//query.setResultTransformer(Transformers.aliasToBean(String.class));
			query.setCacheable(true);
			retVal = query.list();
		}catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findAuditTimestamps on AuditTrail ")
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAuditTimestampsForHistoricalView(int dictionaryVersion) {
		List<String> retVal = null;
		String queryString = "select distinct AUDIT_TIMESTAMP as audit_ts, to_char(AUDIT_TIMESTAMP, 'DD-MON-YYYY:HH24:MI:SS') as AUDIT_TIMESTAMP "
				+ " from CMQ_BASE_"+dictionaryVersion+"_AUDIT order by audit_ts desc";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("AUDIT_TIMESTAMP", StandardBasicTypes.STRING);
			 
			//query.setFetchSize(400);
 			//query.setResultTransformer(Transformers.aliasToBean(String.class));
			query.setCacheable(true);
			retVal = query.list();
		}catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findAuditTimestamps on AuditTrail ")
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	
	
	}
	
	@Override
	public List<CmqBaseDTO> findLists(List<RefConfigCodeList> dictionaryVersions) {
		List<CmqBaseDTO> retVal = null;
		String queryL = "";
		int count = 0;
		for (RefConfigCodeList dict : dictionaryVersions) {
			queryL += "select distinct cmq.cmq_code as listCode, cmq.cmq_name as listName, aud.cmq_id from cmq_base_" + dict.getValue() 
					+ "_audit aud, cmq_base_" + dict.getValue() + " cmq where cmq.cmq_id = aud.cmq_id";
			
			count++;
			if (count < dictionaryVersions.size())
				queryL += " union ";
		}
		
  		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryL);
 			query.addScalar("listName", StandardBasicTypes.STRING);
 			query.addScalar("listCode", StandardBasicTypes.STRING);

 			query.setResultTransformer(Transformers.aliasToBean(CmqBaseDTO.class));
			query.setCacheable(true);
			retVal = query.list();
		}catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findLists on AuditTrail ")
					.append(" Query used was ->")
					.append(queryL);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;	
	}
	
	/**
	 * Excel Report.
	 */
	@Override
	public StreamedContent generateExcel(List<AuditTrailDto> datas, String user) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("AuditTrail_ListSearch");
		XSSFRow row = null;
		int rowCount = 6;

		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);

		// User name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("User name: " + user);

		rowCount++;
		Calendar cal = Calendar.getInstance();
		//.setTime(new Date());
		String date = getWeekDay(cal.get(Calendar.DAY_OF_WEEK)) + ", " + 
				getTwoDigits(cal.get(Calendar.DAY_OF_MONTH) + 1) + "-" + 
				getMonth(cal.get(Calendar.MONTH)) + "-" + 
				cal.get(Calendar.YEAR) + " : " + 
				getTwoDigits(cal.get(Calendar.HOUR)) + ":" + 
				getTwoDigits(cal.get(Calendar.MINUTE)) + ":" + 
				getTwoDigits(cal.get(Calendar.SECOND)) + " EST";
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + date);
		
		
		cell = row.createCell(1);

		//Columns
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		
		cell = row.createCell(0);
		cell.setCellValue("Change Type");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(1);
		cell.setCellValue("Field Name");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(2);
		cell.setCellValue("Old Value");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(3);
		cell.setCellValue("New Value");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(4);
		cell.setCellValue("User Name");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(5);
		cell.setCellValue("User Group");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(6);
		cell.setCellValue("Audit Timestamp");
		setCellStyleColumn(workbook, cell);
		
		rowCount++;
 
		if (datas != null)
 		for (AuditTrailDto dto : datas) {
 			row = worksheet.createRow(rowCount);
 			// Cell 0
 			cell = row.createCell(0);
 			cell.setCellValue(dto.getTransactionType());

 			// Cell 1
 			cell = row.createCell(1);
 			cell.setCellValue(dto.getColumnName());

 			// Cell 2
 			cell = row.createCell(2);
 			cell.setCellValue(dto.getOldValue());
 			
 			// Cell 3
 			cell = row.createCell(3);
 			cell.setCellValue(dto.getNewValue());

 			// Cell 4
 			cell = row.createCell(4);
 			cell.setCellValue(dto.getLastName() + ", " + dto.getFirstName());

 			// Cell 5
 			cell = row.createCell(5);
 			cell.setCellValue(dto.getGroupName());
 			
 			// Cell 6
 			cell = row.createCell(6);
 			cell.setCellValue(dto.getAuditTimestamp());
 			
 			rowCount++;
		}
  

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] xls = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(xls);
			content = new DefaultStreamedContent(
					bais,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					 "AuditTrail_ListSearch.xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	private void setCellStyleColumn(XSSFWorkbook wb, XSSFCell cell) {
		XSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);

		XSSFFont defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 12);
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.BLACK.getIndex());
		defaultFont.setBold(true);
		defaultFont.setItalic(false);

		cellStyle.setFont(defaultFont);
		cell.setCellStyle(cellStyle);
	}
	
	private String getTwoDigits(int number) {
		if (number < 10)
			return "0"+number;
		else return number+"";
		
	}
	
	private String getWeekDay(int weekday) {
		switch (weekday) {
		case 0:
			return "Monday";
		case 1:
			return "Tuesday";
		case 2:
			return "Wednesday";
		case 3:
			return "Thursday";
		case 4:
			return "Friday";
		case 5:
			return "Saturday";
		case 6:
			return "Sunday";

		default:
			break;
		}
		return "";
	}
	
	private String getMonth(int month) {
		switch (month) {
		case 0:
			return "Jan";
		case 1:
			return "Feb";
		case 2:
			return "Mar";
		case 3:
			return "Apr";
		case 4:
			return "May";
		case 5:
			return "Jun";
		case 6:
			return "Jul";
		case 7:
			return "Aug";
		case 8:
			return "Sep";
		case 9:
			return "Oct";
		case 10:
			return "Nov";
		case 11:
			return "Dec";

		default:
			break;
		}
		return "";
	}
	
	private void insertExporLogoImage(XSSFSheet sheet, XSSFWorkbook wb)
			throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		final FileInputStream stream = new FileInputStream(
				ec.getRealPath("/image/logo.jpg"));
		final CreationHelper helper = wb.getCreationHelper();
		final Drawing drawing = sheet.createDrawingPatriarch();

		final ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType(ClientAnchor.DONT_MOVE_AND_RESIZE);

		final int pictureIndex = wb.addPicture(stream,
				Workbook.PICTURE_TYPE_PNG);

//		anchor.setCol1(0);
//		anchor.setRow1(0); // same row is okay
		final Picture pict = drawing.createPicture(anchor, pictureIndex);
		pict.resize();
	}

}
