package nl.isaac.dotcms.languagevariables.util;

/**
* dotCMS Twitter plugin by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2013 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * Checks if the LanguageVariables structure exists. If it doesn't, it will be created
 * 
 * @author jorith.vandenheuvel
 *
 */
public class LanguageVariablesStructureFactory {
	
	public static void createStructure() {
		if(!structureExists()) {
			addStructure();
		}
	}
	
	private static boolean structureExists() {
		Structure structure = StructureFactory.getStructureByVelocityVarName(Configuration.getStructureVelocityVarName());
		
		return UtilMethods.isSet(structure.getName());
	}

	private static void addStructure() {
		Structure structure = new Structure();
		
		Logger.info(LanguageVariablesStructureFactory.class, "Create structure");
		structure.setVelocityVarName(Configuration.getStructureVelocityVarName());
		structure.setName("Language Variables");
		structure.setStructureType(Structure.STRUCTURE_TYPE_CONTENT);
		structure.setHost("SYSTEM_HOST");
		
		try {
			StructureFactory.saveStructure(structure);

			Field key = new Field(Configuration.getStructureKeyFieldLabel(), FieldType.TEXT, Field.DataType.TEXT, structure, true, true, true, 0, false, false, true);
			key.setUnique(true);
			key.setFieldContentlet("text1");
			key.setVelocityVarName(Configuration.getStructureKeyField());
			
			Field value = new Field(Configuration.getStructureValueFieldLabel(), FieldType.TEXT_AREA, Field.DataType.TEXT, structure, false, false, true, 1, false, false, true);
			value.setFieldContentlet("text_area1");
			value.setVelocityVarName(Configuration.getStructureValueField());
			
			Field host = new Field("Host", FieldType.HOST_OR_FOLDER, Field.DataType.TEXT, structure, true, false, true, 2, false, false, true);
			host.setFieldContentlet("system_field1");
			host.setVelocityVarName("host1");
			
			Logger.info(LanguageVariablesStructureFactory.class, "Add field: " + key.getFieldName());
			FieldFactory.saveField(key);
			Logger.info(LanguageVariablesStructureFactory.class, "Add field: " + value.getFieldName());
			FieldFactory.saveField(value);
			Logger.info(LanguageVariablesStructureFactory.class, "Add field: " + host.getFieldName());
			FieldFactory.saveField(host);
		} catch (DotHibernateException e) {
			Logger.error(LanguageVariablesStructureFactory.class, "Error while creating Language Variables structure", e);
			throw new RuntimeException(e);
		}
		
		// Update the cache
		FieldsCache.removeFields(structure); 
		StructureCache.removeStructure(structure); 
		try {
			StructureFactory.saveStructure(structure);
		} catch (DotHibernateException e) {
			throw new RuntimeException(e.toString(), e);
		} 
		FieldsCache.addFields(structure, structure.getFieldsBySortOrder());
	}
}
