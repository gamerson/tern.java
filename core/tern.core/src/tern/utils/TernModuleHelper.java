/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.utils;

import static tern.utils.ExtensionUtils.JSON_EXTENSION;
import static tern.utils.ExtensionUtils.JS_EXTENSION;
import static tern.utils.ExtensionUtils.TERN_SUFFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tern.ITernProject;
import tern.TernException;
import tern.metadata.ModuleDependenciesComparator;
import tern.metadata.TernModuleMetadata;
import tern.server.BasicTernDef;
import tern.server.BasicTernPlugin;
import tern.server.ITernDef;
import tern.server.ITernModule;
import tern.server.ITernModuleConfigurable;
import tern.server.ITernPlugin;
import tern.server.ModuleType;
import tern.server.TernDef;
import tern.server.TernModuleConfigurable;
import tern.server.TernPlugin;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Helper for {@link ITernModule}.
 *
 */
public class TernModuleHelper {

	/**
	 * Group the given list {@link ITernDef} by {@link ITernModule#getType()};
	 * 
	 * @param modules
	 * @param groupedModules
	 */
	public static void groupByType(ITernModule[] modules,
			List<ITernModule> groupedModules) {
		Map<String, TernModuleConfigurable> wrappers = null;
		for (ITernModule module : modules) {
			if (!isConfigurableModule(module)) {
				// module is not configurable, add it
				groupedModules.add(module);
			} else {
				// module is configurable (version can be customized, or
				// options), wrap it with TernModuleConfigurable
				if (wrappers == null) {
					wrappers = new HashMap<String, TernModuleConfigurable>();
				}
				TernModuleConfigurable wrapper = wrappers.get(module.getType());
				if (wrapper == null) {
					wrapper = new TernModuleConfigurable(module);
					wrappers.put(module.getType(), wrapper);
					groupedModules.add(wrapper);
				} else {
					wrapper.addModule(module);
				}
			}
		}
	}

	/**
	 * Returns true if the given module can be configured and false otherwise.
	 * 
	 * @param module
	 * @return true if the given module can be configured and false otherwise.
	 */
	public static boolean isConfigurableModule(ITernModule module) {
		TernModuleMetadata metadata = module.getMetadata();
		return !StringUtils.isEmpty(module.getVersion())
				|| (metadata != null && metadata.getOptions().size() > 0);
	}

	/**
	 * Update the given list of {@link ITernDef} or {@link ITernPlugin} by using
	 * the given module.
	 * 
	 * @param module
	 * @param defs
	 *            to update.
	 * @param plugins
	 *            to update.
	 */
	public static void update(List<ITernDef> defs, List<ITernPlugin> plugins,
			ITernModule module) {
		update(defs, plugins, null, module);
	}

	/**
	 * Update the given list of {@link ITernDef} or {@link ITernPlugin} by using
	 * the given module.
	 * 
	 * @param module
	 * @param defs
	 *            to update.
	 * @param plugins
	 *            to update.
	 */
	private static void update(List<ITernDef> defs, List<ITernPlugin> plugins,
			JsonObject options, ITernModule module) {
		switch (module.getModuleType()) {
		case Def:
			defs.add((ITernDef) module);
			break;
		case Plugin:
			plugins.add((ITernPlugin) module);
			break;
		case Configurable:
			ITernModule wrappedModule = ((ITernModuleConfigurable) module)
					.getWrappedModule();
			JsonObject wrappedOptions = ((ITernModuleConfigurable) module)
					.getOptions();
			update(defs, plugins, wrappedOptions, wrappedModule);
			break;
		}
	}

	/**
	 * Update the given tern project by using the given module.
	 * 
	 * @param module
	 * @param ternProject
	 */
	public static void update(ITernModule module, ITernProject ternProject) {
		update(module, null, ternProject);
	}

	/**
	 * Update the given tern project by using the given module.
	 * 
	 * @param module
	 * @param ternProject
	 */
	public static void update(ITernModule module, JsonObject options,
			ITernProject ternProject) {
		switch (module.getModuleType()) {
		case Def:
			ternProject.addLib((ITernDef) module);
			break;
		case Plugin:
			ternProject.addPlugin((ITernPlugin) module, options);
			break;
		case Configurable:
			ITernModule wrappedModule = ((ITernModuleConfigurable) module)
					.getWrappedModule();
			JsonObject wrappedOptions = ((ITernModuleConfigurable) module)
					.getOptions();
			update(wrappedModule, wrappedOptions, ternProject);
			break;
		}
	}

	/**
	 * Retrieve {@link ITernModuleConfigurable} for the given module inside the
	 * list of modules.
	 * 
	 * @param module
	 * @param options
	 * @param allModules
	 * @return
	 * @throws TernException
	 */
	public static ITernModuleConfigurable findConfigurable(ITernModule module,
			JsonValue options, ITernModule[] allModules) throws TernException {
		String version = module.getVersion();
		for (ITernModule f : allModules) {
			if (f.getModuleType() == ModuleType.Configurable
					&& f.getType() == module.getType()) {
				if (!StringUtils.isEmpty(version)) {
					((ITernModuleConfigurable) f).setVersion(version);
				}
				if (options instanceof JsonObject) {
					// set a copy of the options.
					((ITernModuleConfigurable) f).setOptions(new JsonObject(
							(JsonObject) options));
				}
				return (ITernModuleConfigurable) f;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given module has options and false otherwise.
	 * 
	 * @param module
	 * @return true if the given module has options and false otherwise.
	 */
	public static boolean hasOptions(ITernModule module) {
		if (module == null || module.getMetadata() == null) {
			return false;
		}
		return module.getMetadata().getOptions().size() > 0;
	}

	public static ITernModule getModule(String filename) {
		if (filename.startsWith(TERN_SUFFIX)) {
			String name = filename.substring(TERN_SUFFIX.length(),
					filename.length());
			return getPlugin(name);
		}
		int index = filename.lastIndexOf('.');
		if (index == -1) {
			return null;
		}
		String fileExtension = filename.substring(index + 1, filename.length());
		if (fileExtension.equals(JSON_EXTENSION)) {
			String name = filename.substring(0, index);
			return getDef(name);
		} else if (fileExtension.equals(JS_EXTENSION)) {
			String name = filename.substring(0, index);
			return getPlugin(name);
		}
		return null;
	}

	private static ITernDef getDef(String name) {
		ITernDef def = TernDef.getTernDef(name);
		if (def != null) {
			return def;
		}
		return new BasicTernDef(name);
	}

	/**
	 * Return the tern plugin by name.
	 * 
	 * @param name
	 * @return
	 */
	private static ITernPlugin getPlugin(String name) {
		ITernPlugin plugin = TernPlugin.getTernPlugin(name);
		if (plugin != null) {
			return plugin;
		}
		return new BasicTernPlugin(name);
	}

	/**
	 * Returns the file path as string.
	 * 
	 * @param file
	 * @return the file path as string.
	 */
	public static String getPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return file.getPath();
		}
	}

	/**
	 * Returns the file name of the given tern module.
	 * 
	 * @param module
	 * @return the file name of the given tern module.
	 */
	public static String getFileName(ITernModule module) {
		switch (module.getModuleType()) {
		case Def:
			return new StringBuilder(module.getName()).append('.')
					.append(JSON_EXTENSION).toString();
		default:
			return new StringBuilder(module.getName()).append('.')
					.append(JS_EXTENSION).toString();
		}
	}

	/**
	 * Sort the given list of modules by dependencies.
	 * 
	 * @param modules
	 */
	public static void sort(List<ITernModule> modules) {
		new ModuleDependenciesComparator(modules);
		/*TernModuleMetadata metadata = null;
		Collection<String> dependencies = null;
		Integer moduleIndex = null;
		for (ITernModule module : new ArrayList<ITernModule>(modules)) {
			metadata = module.getMetadata();
			if (metadata != null) {
				dependencies = metadata.getDependencies(module.getVersion());
				if (dependencies != null) {
					for (String dependency : dependencies) {
						moduleIndex = getModuleIndex(dependency, modules);
						if (moduleIndex != null) {
							int oldIndex = modules.indexOf(module);
							if (oldIndex < moduleIndex) {
								modules.set(moduleIndex + 1, module);
							}
						}
					}
				}
			}
		}*/
	}

	private static Integer getModuleIndex(String name, List<ITernModule> modules) {
		ITernModule module = null;
		for (int i = 0; i < modules.size(); i++) {
			module = modules.get(i);
			if (module.getName().equals(name)) {
				return i;
			}
		}
		return null;
	}

}
