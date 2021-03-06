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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.azzyzt.jee.runtime.meta.AzzyztGeneratorOption;
import org.azzyzt.jee.tools.common.Util;
import org.azzyzt.jee.tools.mwe.projectgen.ProjectGen;
import org.azzyzt.jee.tools.project.AzzyztToolsProject;
import org.azzyzt.jee.tools.project.Context;
import org.azzyzt.jee.tools.project.CxfRestClientProject;
import org.azzyzt.jee.tools.project.EarProject;
import org.azzyzt.jee.tools.project.EjbProject;
import org.azzyzt.jee.tools.project.JavaProject;
import org.azzyzt.jee.tools.project.Project;
import org.azzyzt.jee.tools.project.ProjectUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class MWEGeneratorWorker {

	private IProgressMonitor monitor;
	private Context context;

	public void callMWEGenerator(IProject prj) 
	throws InterruptedException, CoreException 
	{
		monitor.beginTask("Generating code", 100);
		
		try {
			/*
			 * TODO fix this to use a plugin setting stored with the project
			 */
			String prjName = prj.getName();
			String ejbSuffix = Context.PROJECT_SUFFIX_EJB;
			if (!prjName.endsWith(ejbSuffix)) {
				throw Util.createCoreException(
						"The project name does not end with \""
						+ejbSuffix+"\", can't determine project stem", 
						null
				);
			}
			String projectBaseName = prjName.substring(0, prjName.length() - Context.PROJECT_SUFFIX_EJB.length());
			
			context = new Context(prj);
			context.setProjectBaseName(projectBaseName);
			context.setMonitor(monitor);
			
			AzzyztToolsProject azzyztToolsProject = new AzzyztToolsProject(
					ProjectGen.AZZYZT_RELEASE, 
					ProjectGen.getJeeToolsMweJarUrl(), 
					ProjectGen.getToolsLibJarUrls(), 
					ProjectGen.getJeeRuntimeJarUrl(),
					ProjectGen.getJeeRuntimeSiteJarUrl(),
					context
			);
			
			fixAndUpdateLegacyProjects();

			/*
			 * Two steps follow now: First we make sure prerequisites exist, then we load our model,
			 * Azzyztant included, and generate the project.
			 */
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			String[] args = {
					root.getLocation()+""+IPath.SEPARATOR,
					projectBaseName
			};
			URL[] classPathEntries = ProjectUtil.classPathURLsForToolMainClass(prj, azzyztToolsProject.extraURLsForToolMainClass());

			String fqMainClassName = "org.azzyzt.jee.tools.mwe.PrerequisiteGenerator";
			Util.callExternalMainClass("Ensure we have all prerequisites", classPathEntries, fqMainClassName, args);
			monitor.worked(5);
			
			/*
			 * Could have been created, better refresh. The next step automatically delays until 
			 * prerequisites can be loaded.
			 */
			refreshAzzyztedProject();

			String optionName = AzzyztGeneratorOption.AddCxfRestClient.name();
			String[] testArgs = {
					optionName,
					projectBaseName
			};
			fqMainClassName = "org.azzyzt.jee.tools.mwe.HasOption";
			boolean hasOption = Util.askExternalMainClass("Check is option "+optionName+" is requested", classPathEntries, fqMainClassName, testArgs);
			String cxfRestClientProjectName = projectBaseName+Context.PROJECT_SUFFIX_CXF_REST_CLIENT;
			if (hasOption) {
				IProject p = context.getRoot().getProject(cxfRestClientProjectName);
				if (!p.exists()) {
					ArrayList<JavaProject> projectsOnBuildPath = new ArrayList<JavaProject>();
					projectsOnBuildPath.add(
							// TODO this is ugly, create a uniform way to get at existing projects by name
							new JavaProject(context.getEjbClientProjectName(), context, null)
							);
					CxfRestClientProject cxfRestClientProject = 
							new CxfRestClientProject(
									cxfRestClientProjectName, 
									context, 
									projectsOnBuildPath,
									ProjectGen.getCxfRestClientLibJarUrls()
									);
					cxfRestClientProject.getP().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
				}
			} else {
				/*
				 * We could remove the project, but that would probably mean we throw away user code.
				 * Instead we simply do nothing
				 */
			}
			
			fqMainClassName = "org.azzyzt.jee.tools.mwe.StandardCodeGenerator";
			Util.callExternalMainClass("Generate code from entities", classPathEntries, fqMainClassName, args);
			monitor.worked(55);
			
			refreshAzzyztedProject();
		} finally {
			monitor.done();
		}
	}

	private void refreshAzzyztedProject() throws CoreException {
		for (String name : context.allProjectNames()) {
			IProject project = context.getRoot().getProject(name);
			if (project.exists()) {
				project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
			}
		}
	}

	private void fixAndUpdateLegacyProjects() 
	throws CoreException 
	{
		// update EAR libraries to current version 
		new EarProject(
				ProjectGen.AZZYZT_RELEASE, 
				context.getEarProjectName(), 
				context, ProjectGen.getAllRuntimeJarUrls()
		);
		
		// pre-1.2.0 did not have a "meta" package in EJB user folder
		Project ejbProject = new Project(context.getEjbProjectName(), context);
		String ejbDir = ejbProject.getP().getLocation().toString();
		File userFolder = new File(ejbDir + "/" + EjbProject.EJB_SRC_FOLDER_NAME);
		File entityDir = findSubdir(userFolder, "entity");
		if (entityDir == null) {
			throw Util.createCoreException(
					"The project does not contain a subdirectory \"entity\" , can't determine meta dir name", 
					null
			);
		}
		File packageBaseDir = entityDir.getParentFile();
		if (!hasMeta(packageBaseDir)) {
			new File(packageBaseDir.toString()+"/meta").mkdir();
			ejbProject.refresh();
		}
	}

	private boolean hasMeta(File packageBaseDir) {
		for (File d : packageBaseDir.listFiles()) {
			if (d.getName().equals("meta")) {
				if (!d.isDirectory()) {
					d.delete();
					return false;
				}
			}
			if (!d.isDirectory()) {
				continue;
			}
			if (d.getName().equals("meta")) {
				return true;
			}
		}
		return false;
	}

	private File findSubdir(File dir, String subdirName) {
		for (File d : dir.listFiles()) {
			if (!d.isDirectory()) {
				continue;
			}
			if (d.getName().equals(subdirName)) return d;
			 File subdir = findSubdir(d, subdirName);
			 if (subdir != null) return subdir;
		}
		return null;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
}
