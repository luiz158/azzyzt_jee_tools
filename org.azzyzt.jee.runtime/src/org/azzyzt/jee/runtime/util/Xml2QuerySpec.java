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

package org.azzyzt.jee.runtime.util;

import org.azzyzt.jee.runtime.dto.query.QuerySpec;
import org.azzyzt.jee.runtime.exception.QuerySyntaxException;

/**
 * Interface implemented by parsers for XML representations of query specifications.
 * Java is a single inheritance language, and our parsers already inherit from 
 * <code>org.xml.sax.helpers.DefaultHandler</code>. Thus, to hide the fact that we
 * could use different parsers for different XML formats, we let all parsers implement
 * this interface.
 * 
 * @see QuerySpec
 * @see AttributedTags2QuerySpec
 * 
 */
public interface Xml2QuerySpec {

	/**
	 * @param xml an XML representation of a <code>QuerySpec</code>
	 * @return a <code>QuerySpec</code>
	 * @throws QuerySyntaxException
	 */
	public abstract QuerySpec fromXML(String xml) throws QuerySyntaxException;

}