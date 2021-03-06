package org.jboss.tools.hibernate.search.actions;

import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.hibernate.console.ConsoleConfiguration;
import org.hibernate.console.execution.ExecutionContext.Command;
import org.hibernate.eclipse.console.utils.ClassLoaderHelper;
import org.hibernate.eclipse.console.utils.ProjectUtils;
import org.jboss.tools.hibernate.runtime.spi.IClassMetadata;
import org.jboss.tools.hibernate.runtime.spi.IConfiguration;
import org.jboss.tools.hibernate.search.runtime.spi.HSearchServiceLookup;
import org.jboss.tools.hibernate.search.runtime.spi.IHSearchService;

public class IndexRebuildAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
		IStructuredSelection selection = (IStructuredSelection) sel;

		for (Iterator<?> i = selection.iterator(); i.hasNext();) {
			Object node = i.next();
			if (!(node instanceof ConsoleConfiguration)) {
				continue;
			}
			final ConsoleConfiguration config = (ConsoleConfiguration) node;
			IJavaProject project = ProjectUtils.findJavaProject(config);
			URLClassLoader classloader = ClassLoaderHelper.getProjectClassLoader(project);
			Map<String, IClassMetadata> meta = config.getSessionFactory().getAllClassMetadata();
			final Set<Class> classes = new HashSet<Class>();
			try {
				for (String className : meta.keySet()) {
					classes.add(Class.forName(className, true, classloader));
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			try {
				config.execute(new Command() {
					public Object execute() {
						final IConfiguration cfg = config.getConfiguration();
						if (cfg == null) {
							return null;
						}
						IHSearchService service = HSearchServiceLookup.findService("4.5");
						service.newIndexRebuild(config.getSessionFactory(), classes);
						return null;
					}
				});
			} catch (Exception he) {

			}

		}
		return null;
	}
}
