/*
 * Copyright (c) 2011, Municipiality of Vienna, Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package org.azzyzt.jee.runtime.meta;

import java.util.Date;

public class InvocationMetaInfo {

	private Date started;
	
	private String authenticatedUserName;
	
	private boolean return200OnError = false;

	private Credentials credentials;
	
	public InvocationMetaInfo() {
		this.started = new Date();
	}

	public Date getStarted() {
		return started;
	}

	public String getAuthenticatedUserName() {
		return authenticatedUserName;
	}

	public void setAuthenticatedUserName(String authenticatedUserName) {
		this.authenticatedUserName = authenticatedUserName;
	}

	public boolean isReturn200OnError() {
		return return200OnError;
	}

	public void setReturn200OnError(boolean return200OnError) {
		this.return200OnError = return200OnError;
	}

	public void setCredentials(Credentials c) {
		this.credentials = c;
	}

	public Credentials getCredentials() {
		return credentials;
	}

}
