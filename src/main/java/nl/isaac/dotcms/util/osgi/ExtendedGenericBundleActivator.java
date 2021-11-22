package nl.isaac.dotcms.util.osgi;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.servlet.Servlet;

import com.dotcms.filters.interceptor.FilterWebInterceptorProvider;
import com.dotcms.filters.interceptor.WebInterceptorDelegate;
import com.dotmarketing.filters.AutoLoginFilter;
import com.dotmarketing.util.Config;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.context.ViewContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.dotcms.rest.config.RestServiceUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHook;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPreHook;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.VelocityUtil;

/**
 * Provides convenience methods for adding Dotcms services
 * @author Maarten, Xander
 *
 */
public abstract class ExtendedGenericBundleActivator extends GenericBundleActivator {
	//	private List<ServiceTracker<ExtHttpService, ExtHttpService>> trackers = new ArrayList<>();
	private List<Runnable> cleanupFunctions = new ArrayList<>();
	private boolean languageVariablesNotAdded = true;
	private static final String DOTCMS_HOME;
	private String bundleName;

	private Scheduler scheduler;
	private Properties schedulerProperties;

	private LoggerContext pluginLoggerContext;

	static {
		String userDir = System.getProperty( "user.dir" );

		if (userDir.endsWith("tomcat")) {
			DOTCMS_HOME = userDir.substring(0, userDir.lastIndexOf(File.separator));
		} else {
			DOTCMS_HOME = userDir;
		}
		Logger.debug(ExtendedGenericBundleActivator.class, "DOTCMS_HOME: " + DOTCMS_HOME);
	}

	public abstract void init(BundleContext context);

	final public void start(BundleContext context) throws Exception {
		try {
			super.initializeServices(context);
			initializeLoggerContext();
			Logger.info(this.getClass().getName(), "Starting " + context.getBundle().getSymbolicName());
			addMonitoringServlet(context);
			init(context);
			Logger.info(this.getClass().getName(), "Started " + context.getBundle().getSymbolicName());
		} catch (Throwable t) {
			Logger.error(this, "Initialization of plugin: " + context.getBundle().getSymbolicName() + " failed with error:", t);
			throw t;
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		unregisterServices(context);
	}

	protected void addMonitoringServlet(BundleContext context) throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		bundleName = bundle.getHeaders().get("Bundle-Name");
		String servletPath = "/servlets/monitoring/" + bundleName;
		addServlet(context, new MonitoringServlet(bundleName), servletPath, false);

	}

	protected void addViewTool(BundleContext context, Class<?> viewtoolClass, String key, ViewToolScope scope) {
		OSGiSafeServletToolInfo viewtool = new OSGiSafeServletToolInfo();
		viewtool.setClassname(viewtoolClass);
		viewtool.setKey(key);
		switch (scope) {
			case APPLICATION:
				viewtool.setScope(ViewContext.APPLICATION);
				break;
			case REQUEST:
				viewtool.setScope(ViewContext.REQUEST);
				break;
			case RESPONSE:
				viewtool.setScope(ViewContext.RESPONSE);
				break;
			case SESSION:
				viewtool.setScope(ViewContext.SESSION);
				break;
			default:
				throw new RuntimeException("Unknown viewtoolscope: " + scope);
		}

		registerViewToolService(context, viewtool);
	}

	protected void addServlet(BundleContext context, final Class<? extends Servlet> clazz, final String path) {

		Validate.notNull(clazz, "Servlet class may not be null");

		final Servlet servlet;
		try {
			servlet = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		addServlet(context, servlet, path, false);
	}

	/**
	 * @param handleBundleServices is used to add/remove bundleServices, which are needed for the DispatcherServlet
	 */
	private void addServlet(BundleContext context, final Servlet servlet, final String path, final boolean handleBundleServices) {
		Validate.notEmpty(path, "Servlet path may not be null");
		Validate.isTrue(path.startsWith("/"), "Servlet path must start with a /");

		Logger.info(this.getClass().getName(), "Registering Servlet " + servlet.getClass().getSimpleName() + " on /app" + path);

		final FilterWebInterceptorProvider filterWebInterceptorProvider =
				FilterWebInterceptorProvider.getInstance(Config.CONTEXT);

		final WebInterceptorDelegate delegate =
				filterWebInterceptorProvider.getDelegate(AutoLoginFilter.class);

		final ServletWebInterceptor servletWebInterceptor = new ServletWebInterceptor(context, servlet, path);
		delegate.addFirst(servletWebInterceptor);

		// For some reason when other plugins use this class the below code fails when using lambdas
		cleanupFunctions.add(new Runnable() {
			@Override
			public void run() {
				delegate.remove(servletWebInterceptor.getName(), true);
			}
		});
	}

//	protected void addFilter(BundleContext context, final Class <? extends Filter> clazz, final String regex) {
//		Validate.notNull(clazz, "Filter class may not be null");
//		Validate.notEmpty(regex, "Filter regex may not be null");
//		Validate.isTrue(regex.startsWith("/"), "Filter regex must start with a /");
//
//		final Filter filterToRegister;
//		try {
//			filterToRegister = clazz.newInstance();
//		} catch (InstantiationException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		}
//
//		Logger.info(this.getClass().getName(), "Registering Filter " + filterToRegister.getClass().getSimpleName());
//
//		ServiceTracker<ExtHttpService, ExtHttpService> tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
//			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
//				ExtHttpService extHttpService = super.addingService(reference);
//
//				try {
//					extHttpService.unregisterFilter(filterToRegister);
//				} catch (Throwable t) {
//					// Do nothing, it was probably not registered
//				}
//
//				try {
//					extHttpService.registerFilter(filterToRegister, regex, null, trackers.size(), null);
//				} catch (ServletException e) {
//					throw new RuntimeException("Failed to register filter " + filterToRegister.getClass().getSimpleName(), e);
//				}
//				return extHttpService;
//			}
//			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
//				extHttpService.unregisterFilter(filterToRegister);
//				super.removedService(reference, extHttpService);
//			}
//		};
//
//		tracker.open();
//		this.trackers.add(tracker);
//	}

	protected void addMacros(BundleContext context) {
		Logger.info(this.getClass().getName(), "Registering macros");

		final VelocityEngine engine = VelocityUtil.getEngine();
		URL macrosExtUrl = context.getBundle().getResource("conf/macros-ext.vm");

		InputStream instream = null;
		try {
			instream = macrosExtUrl.openStream();
			engine.evaluate(VelocityUtil.getBasicContext(), new StringWriter(), context.getBundle().getSymbolicName(), new InputStreamReader(instream, Charset.forName("UTF-8")));
		} catch (IOException e) {
			Logger.warn(this.getClass().getName(), "Exception while reading macros-ext.vm", e);
		} finally {
			try {
				if(instream != null) {
					instream.close();
				}
			} catch (IOException e) {
				Logger.warn(this.getClass().getName(), "Exception while closing stream to macros-ext.vm", e);
			}
		}
	}

	private void addLanguageVariables(Map<String, String> languageVariables, Language language) {
		Map<String, String> emptyMap = new HashMap<>();
		Set<String> emptySet = new HashSet<>();
		try {

			Logger.info(this.getClass().getName(), "Registering " + languageVariables.keySet().size() + " language variable(s)");
			APILocator.getLanguageAPI().saveLanguageKeys(language, languageVariables, emptyMap, emptySet);

		} catch (DotDataException e) {
			Logger.warn(this.getClass().getName(), "Unable to register language variables", e);
		}
	}

	/**
	 * Registers the language variables that are saved in the conf/language-ext.properties file in the English (en_US) Language
	 */
	protected void addLanguageVariables(BundleContext context) {
		Language defaultLanguage = APILocator.getLanguageAPI().getLanguage("en", "US");
		addLanguageVariables(context, defaultLanguage);
	}

	/**
	 * Registers the language variables that are saved in the conf/language-ext.properties file in the given Language
	 */
	protected void addLanguageVariables(BundleContext context, Language language) {
		if(languageVariablesNotAdded) {
			languageVariablesNotAdded = false;
			try {

				// Read all the language variables from the properties file
				URL resourceURL = context.getBundle().getResource("conf/Language-ext.properties");
				PropertyResourceBundle resourceBundle = new PropertyResourceBundle(resourceURL.openStream());

				// Put the properties in a map
				Map<String, String> languageVariables = new HashMap<>();
				for(String key: resourceBundle.keySet()) {
					languageVariables.put(key, resourceBundle.getString(key));
				}

				// Register the variables in the given language
				addLanguageVariables(languageVariables, language);

			} catch (IOException e) {
				Logger.warn(this.getClass().getName(), "Exception while registering language variables", e);
			}
		}
	}

	protected void addPreHook(BundleContext context, Class <? extends ContentletAPIPreHook> clazz) {
		Logger.info(this.getClass().getName(), "Registering PreHook " + clazz.getSimpleName());
		try {
			super.addPreHook(clazz.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void addPostHook(BundleContext context, Class <? extends ContentletAPIPostHook> clazz) {
		Logger.info(this.getClass().getName(), "Registering PostHook " + clazz.getSimpleName());
		try {
			super.addPostHook(clazz.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * @deprecated Use {@link #addPostHook(BundleContext, Class)}
	 * @param posthook Must be an instance!
	 * @throws Exception
	 */
	@Override
	@Deprecated
	protected void addPostHook(Object posthook) throws Exception {
		super.addPostHook(posthook);
	}
	/**
	 * @deprecated Use {@link #addPostHook(BundleContext, Class)}
	 * @param posthook Must be an instance!
	 * @throws Exception
	 */
	@Deprecated
	protected void addPostHook(Class<? extends ContentletAPIPostHook> posthook) throws Exception {
		addPostHook(null, posthook);
	}
	/**
	 * @deprecated Use {@link #addPreHook(BundleContext, Class)}
	 * @param prehook Must be an instance!
	 * @throws Exception
	 */
	@Override
	@Deprecated
	protected void addPreHook(Object prehook) throws Exception {
		super.addPreHook(prehook);
	}
	/**
	 * @deprecated Use {@link #addPreHook(BundleContext, Class)}
	 * @param prehook Must be an instance!
	 * @throws Exception
	 */
	@Deprecated
	protected void addPreHook(Class<? extends ContentletAPIPreHook> prehook) throws Exception {
		addPreHook(null, prehook);
	}


	protected void addRestService(BundleContext context, final Class<?> clazz) {
		final Class thisClass = this.getClass();

		Logger.info(thisClass, "Added REST service " + clazz.getSimpleName());
		RestServiceUtil.addResource(clazz);

		cleanupFunctions.add(new Runnable() {
			@Override
			public void run() {
				RestServiceUtil.removeResource(clazz);
			}
		});
	}



	protected void addPortlets(BundleContext context) {
		if(languageVariablesNotAdded) {
			addLanguageVariables(context);
		}

		Logger.info(this.getClass().getName(), "Registering portlet(s)");

		try {
			registerPortlets(context, new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml"});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		CacheLocator.getVeloctyResourceCache().clearCache();
	}

	/**
	 * Set the properties that the org.quartz Scheduler will use. Can only be called once, and only before
	 * a Job is added.
	 */
	protected void initializeSchedulerProperties(Properties properties) {
		if(this.schedulerProperties != null) {
			throw new IllegalStateException("Can't overwrite scheduler properties when they are already set. Set the properties before adding Jobs, and do not change them afterwards.");
		}

		this.schedulerProperties = properties;
	}

	protected Properties getDefaultSchedulerProperties() {
		Properties properties = new Properties();

		//Default properties, retrieved from a quartz.properties file
		//We only changed the threadcount to 1
		properties.setProperty("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
		properties.setProperty("org.quartz.scheduler.rmi.export", "false");
		properties.setProperty("org.quartz.scheduler.rmi.proxy", "false");
		properties.setProperty("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");
		properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		properties.setProperty("org.quartz.threadPool.threadCount", "1");
		properties.setProperty("org.quartz.threadPool.threadPriority", "5");
		properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
		properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
		properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

		return properties;
	}

	/**
	 * Adds a Job, and starts a Scheduler when none was yet started
	 */
	protected void addJob(BundleContext context, Class<? extends Job> clazz, String cronExpression) {
		String jobName = clazz.getName();
		String jobGroup = FrameworkUtil.getBundle(clazz).getSymbolicName();
		JobDetail job = new JobDetail(jobName, jobGroup, clazz);
		job.setDurability(false);
		job.setVolatility(true);
		job.setDescription(jobName);

		try {
			CronTrigger trigger = new CronTrigger(jobName, jobGroup, cronExpression);

			if(scheduler == null) {
				if(schedulerProperties == null) {
					schedulerProperties = getDefaultSchedulerProperties();
				}
				scheduler = new StdSchedulerFactory(schedulerProperties).getScheduler();
				scheduler.start();
			}

			Date date = scheduler.scheduleJob(job, trigger);

			Logger.info(this.getClass().getName(), "Scheduled job " + jobName + ", next trigger is on " + date);

		} catch (ParseException e) {
			Logger.error(this, "Cron expression '" + cronExpression + "' has an exception. Throwing IllegalArgumentException", e);
			throw new IllegalArgumentException(e);
		} catch (SchedulerException e) {
			Logger.error(this, "Unable to schedule job " + jobName, e);
		}

	}

	@Override
	protected void unregisterServices(BundleContext context) throws Exception {
		super.unregisterServices(context);
		removeTrackedServices();
		if(scheduler != null) {
			scheduler.shutdown(false);
			scheduler = null;
			schedulerProperties = null;
		}

		closeLoggerContext();
	}

	/**
	 * Removes Dotcms services that are tracked by the ExtendedGenericBundleActivator. These are
	 * services that require more than just a simple register/unregister. For instance Servlets and Filters.
	 */
	protected void removeTrackedServices() {
		for(Runnable runnable: cleanupFunctions) {
			runnable.run();
		}
	}

	/**
	 * <p>Deploys all files under src/main/resources/ROOT to the Dotcms file structure. Subdirectories
	 * of ROOT must be relative to the dotserver directory. So, if you want do deploy a file in
	 * <strong>dotcms/dotserver/dotCMS/html/js/test.js</strong>, you need to place it in <strong>src/main/resources/ROOT/dotCMS/html/js/test.js</strong>.<p>
	 *
	 * <p>If the file that you want to deploy already exists in Dotcms, a backup is created in dotserver/_original.</p>
	 *
	 * <p><strong><u>Important:</u></strong> if this method is called in the start() of an OSGi Activator, {@link #undeployFiles(BundleContext)} needs
	 * to be called in stop() to make sure the files will be undeployed when the plugin stops</p>
	 *
	 *
	 * @param context The current BundleContext
	 * @throws IOException If an error occurs when deploying the files or backing up the original ones.
	 */
	protected void deployFiles ( BundleContext context) throws IOException {
		//Find all files under /ROOT
		Enumeration<URL> entries = context.getBundle().findEntries( "ROOT", "*.*", true );

		if (entries != null) {
			while ( entries.hasMoreElements() ) {

				URL entryUrl = entries.nextElement();
				String fileName = entryUrl.getPath().substring(6);
				File resourceFile = new File(DOTCMS_HOME + File.separator + fileName);

				if ( resourceFile.exists() ) {
					Logger.info(this.getClass().getName(), "File Already Exists, creating backup: "+fileName);
					backupOriginalFile(entryUrl);
				}

				copyFile(entryUrl, resourceFile);
			}
		} else {
			Logger.warn(this.getClass().getName(), "Source folder not found");
		}
	}

	/**
	 * Undeploys all files that are deployed using {@link #deployFiles(BundleContext)}. If
	 * a backup of the original file was created, this file will also be placed back.
	 *
	 * @param context The current BundleContext
	 * @throws IOException If an error occurs when undeploying the files.
	 */
	protected void undeployFiles(BundleContext context) throws IOException {
		//Find all files under /ROOT
		Enumeration<URL> entries = context.getBundle().findEntries( "ROOT", "*.*", true );

		if (entries != null) {
			while ( entries.hasMoreElements() ) {

				URL entryUrl = entries.nextElement();
				String fileName = entryUrl.getPath().substring(6);
				File resourceFile = new File(DOTCMS_HOME + File.separator + fileName);
				if ( resourceFile.exists() ) {
					Logger.info(this.getClass().getName(), "Undeploying file: " + resourceFile.getAbsolutePath());

					resourceFile.delete();
					recoverOriginalFile(entryUrl);
				}
			}
		} else {
			Logger.warn(this.getClass().getName(), "Source folder not found");
		}
	}

	private void backupOriginalFile(URL resourceURL) throws IOException {
		File backupFile = new File(DOTCMS_HOME + File.separator + "_original" + resourceURL.getPath().substring(5));
		File originalFile = new File(DOTCMS_HOME + File.separator + resourceURL.getPath().substring(6));

		if (backupFile.exists()) {
			Logger.debug(this, "Backup already created earlier, so we don't create a new one");
		} else {
			backupFile.getParentFile().mkdirs();
			copyFile(originalFile.toURI().toURL(), backupFile);
		}

	}

	private void recoverOriginalFile(URL resourceURL) throws IOException {
		File backupFile = new File(DOTCMS_HOME + File.separator + "_original" + resourceURL.getPath().substring(5));
		File originalFile = new File(DOTCMS_HOME + File.separator + resourceURL.getPath().substring(6));

		if (backupFile.exists()) {
			copyFile(backupFile.toURI().toURL(), originalFile);
			backupFile.delete();
		}
	}

	private void copyFile(URL source, File destination) throws IOException {

		Logger.info(this.getClass().getName(), "Creating file: " + destination.getAbsolutePath());
		InputStream in = null;
		OutputStream out = null;

		try {
			if ( !destination.getParentFile().exists() ) {
				destination.getParentFile().mkdirs();
			}
			destination.createNewFile();

			in = source.openStream();
			out = new FileOutputStream( destination );

			byte[] buffer = new byte[1024];
			int length;
			while ( (length = in.read( buffer )) > 0 ) {
				out.write( buffer, 0, length );
			}
		} finally {
			if ( in != null ) {
				in.close();
			}
			if ( out != null ) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * https://dotcms.com/docs/latest/osgi-dynamic-plugin-logging
	 */
	private void initializeLoggerContext() {
		//Initializing log4j...
		LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();

		if (dotcmsLoggerContext != null) {

			//Initialing the log4j context of this plugin based on the dotCMS logger context
			pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(),
					false,
					dotcmsLoggerContext,
					dotcmsLoggerContext.getConfigLocation());
		}

	}

	private void closeLoggerContext() {
		if (pluginLoggerContext != null) {
			Log4jUtil.shutdown(pluginLoggerContext);
		}
	}
}
