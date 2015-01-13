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
package tern.metadata;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TernModuleMetadataManagerTest {

	@Before
	public void init() {
		TernModuleMetadataManager.getInstance().init(new File("../tern.core"));
	}

	@Test
	public void metadataExists() {
		TernModuleMetadata metadata = TernModuleMetadataManager.getInstance()
				.getMetadata("jquery");
		Assert.assertNotNull(metadata);
		Assert.assertEquals("jquery", metadata.getName());
		Assert.assertEquals("jQuery", metadata.getLabel());
	}

	@Test
	public void jQueryDependencies() {
		TernModuleMetadata metadata = TernModuleMetadataManager.getInstance()
				.getMetadata("jquery");
		Assert.assertNotNull(metadata);
		Assert.assertEquals("jquery", metadata.getName());
		Collection<String> dependencies = metadata.getDependencies(null);
		Assert.assertNotNull(dependencies);
		Assert.assertEquals(2, dependencies.size());
		Assert.assertTrue(dependencies.contains("ecma5"));
		Assert.assertTrue(dependencies.contains("browser"));
	}
	
	@Test
	public void AlloyUIDependencies() {
		TernModuleMetadata metadata = TernModuleMetadataManager.getInstance()
				.getMetadata("aui");
		Assert.assertNotNull(metadata);
		Assert.assertEquals("aui", metadata.getName());
		Assert.assertEquals("AlloyUI", metadata.getLabel());
		Collection<String> dependencies = metadata.getDependencies("2.0.x");
		Assert.assertNotNull(dependencies);
		Assert.assertEquals(3, dependencies.size());
		Assert.assertTrue(dependencies.contains("ecma5"));
		Assert.assertTrue(dependencies.contains("browser"));
		Assert.assertTrue(dependencies.contains("yui3"));
	}
}
