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

package org.azzyzt.jee.tools.mwe.projectgen.workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.azzyzt.jee.tools.mwe.projectgen.ProjectGen;
import org.azzyzt.jee.tools.project.AzzyztToolsProject;
import org.azzyzt.jee.tools.project.Context;
import org.azzyzt.jee.tools.project.DynamicWebProject;
import org.azzyzt.jee.tools.project.EarProject;
import org.azzyzt.jee.tools.project.EjbProject;
import org.azzyzt.jee.tools.project.JavaProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author advman
 *
 */
public class NewAzzyztedProjectWorker {
	
	private final Context context = new Context();

	public NewAzzyztedProjectWorker() { }

	/**
	 * The main workflow method. It creates all required projects and adds them to an EAR project
	 * 
	 * @throws InterruptedException
	 * @throws CoreException
	 */
	public void generate() 
	throws InterruptedException, CoreException 
	{
		context.initializeRuntimeSpecificFacets();
		context.setCreateEjbClient(true);
		
		if (!context.isValid()) throw new CoreException(context.getErrorStatus());
		
		context.getMonitor().beginTask("Generating azzyzted project "+context.getProjectBaseName(), 100);
		
		try {
			advanceProgress(0, "Make sure Azzyzt is installed in the workspace");
			
			AzzyztToolsProject azzyztToolsProject = new AzzyztToolsProject(
					ProjectGen.AZZYZT_RELEASE, 
					ProjectGen.getJeeToolsMweJarUrl(), 
					ProjectGen.getToolsLibJarUrls(), 
					ProjectGen.getJeeRuntimeJarUrl(),
					ProjectGen.getJeeRuntimeSiteJarUrl(),
					context
			);
			
			advanceProgress(10, "Create EAR project");
			
			EarProject ear = new EarProject(
					ProjectGen.AZZYZT_RELEASE, 
					context.getEarProjectName(), 
					context, ProjectGen.getAllRuntimeJarUrls()
			);
			
			advanceProgress(20, "Create EJB project");
			
			EjbProject ejb =
				new EjbProject(
						context.getEjbProjectName(), context, 
						ear, 
						new ArrayList<JavaProject>(),
						Arrays.asList(ProjectGen.AZZYZTED_NATURE_ID),
						azzyztToolsProject.extraURLsForToolMainClass()
				);
	
			advanceProgress(70, "Creating servlet project");
			
			new DynamicWebProject(
					context.getServletProjectName(), context, 
					ear, 
					Collections.singletonList((JavaProject)ejb)
				);
		} finally {
			context.getMonitor().done();
		}
	}

	private void advanceProgress(int amountFinished, String nowBeginning) 
	throws InterruptedException 
	{
		IProgressMonitor monitor = context.getMonitor();
		if (monitor.isCanceled()) {
			monitor.done();
			throw new InterruptedException();
		}
		monitor.worked(amountFinished);
		monitor.subTask(nowBeginning);
	}
	

	public Context getContext() {
		return context;
	}

}
