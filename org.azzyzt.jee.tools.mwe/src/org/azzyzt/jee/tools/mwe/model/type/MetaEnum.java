/*
 * Copyright (c) 2011, Municipiality of Vienna, Austria
 *
 * Licensed under the EUPL, Version 1.1 or � as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * 
 * For convenience a plain text copy of the English version 
 * of the Licence can be found in the file LICENCE.txt in
 * the top-level directory of this software distribution.
 * 
 * You may obtain a copy of the Licence in any of 22 European
 * Languages at:
 *
 * http://www.osor.eu/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package org.azzyzt.jee.tools.mwe.model.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.azzyzt.jee.tools.mwe.exception.ToolError;


public class MetaEnum extends MetaClass {
	
	private List<MetaEnumConstant> enumConstants = new ArrayList<MetaEnumConstant>();
	private Map<String, MetaEnumConstant> enumConstantsByShortName = new HashMap<String, MetaEnumConstant>();

	public static MetaEnum forName(Class<?> clazz, String packageName, String simpleName) {
		MetaEnum result = getOrConstruct(clazz, packageName, simpleName);
		return result;
	}
	
	public static MetaEnum forType(Class<?> clazz) {
		String packageName = clazz.getPackage().getName();
		String simpleName = clazz.getSimpleName();
		MetaEnum result = getOrConstruct(clazz, packageName, simpleName);
		return result;
	}

	protected static MetaEnum getOrConstruct(Class<?> clazz,
			String packageName, String simpleName) {
		MetaEnum result = (MetaEnum)MetaTypeRegistry.metaTypeForName(createFqName(packageName, simpleName));
		if (result == null) {
			result = new MetaEnum(clazz, packageName, simpleName);
			result.postConstructionAnalysis();
		}
		return result;
	}
	
	@Override
	public void postConstructionAnalysis() {
		super.postConstructionAnalysis();
		Class<?> clazz = getClazz();
		Enum<?>[] enumConstantArray = (Enum<?>[])clazz.getEnumConstants();
		for (Enum<?> e : enumConstantArray) {
			String shortName = e.name();
			String shortQualifiedName = clazz.getSimpleName()+"."+e.name();
			MetaEnumConstant mec = new MetaEnumConstant(shortName, shortQualifiedName, e.ordinal());
			enumConstants.add(mec);
			enumConstantsByShortName.put(shortName, mec);
		}
	}

	protected MetaEnum(Class<?> clazz, String packageName, String simpleName) {
		super(clazz, packageName, simpleName);
	}

	public void addEnumConstant(MetaEnumConstant enumConstant) {
		this.enumConstants.add(enumConstant);
	}

	public List<MetaEnumConstant> getEnumConstants() {
		return enumConstants;
	}
	
	public MetaEnumConstant getMetaEnumConstantByName(String shortName) {
		if (!enumConstantsByShortName.containsKey(shortName)) {
			throw new ToolError(shortName+": No such constant in enum "+this);
		}
		return enumConstantsByShortName.get(shortName);
	}

}
