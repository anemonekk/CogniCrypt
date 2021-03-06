/********************************************************************************
 * Copyright (c) 2015-2019 TU Darmstadt, Paderborn University
 * 

 * http://www.eclipse.org/legal/epl-2.0. SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package de.cognicrypt.integrator.primitive.test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import de.cognicrypt.core.Constants;
import de.cognicrypt.integrator.primitive.providerUtils.ProviderFile;
import de.cognicrypt.utils.Utils;

public class ProviderFileWriterTest {

	boolean elementExists = false;
	File jarFile = new File(Utils.getResourceFromWithin(Constants.testPrimitverFolder) + "jarTest.jar");
	ProviderFile providerFile;
	File folder;

	@Before
	public void setUp() throws IOException {
		this.providerFile = new ProviderFile();
		this.folder = Utils.getResourceFromWithin(Constants.testPrimitverFolder);
		this.providerFile.zipProject(this.folder.getAbsolutePath(), this.jarFile, true);
	}

	@Test
	public void createJarFileTest() {
		try {

			final File[] files = this.folder.listFiles();
			for (final File file : files) {
				this.providerFile.zipProject(file.getAbsolutePath(), this.jarFile, true);

			}
			final JarFile jar = new JarFile(this.jarFile);
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				final String entryName = entry.getName();
				this.elementExists = fileExists(files, entryName);
			}
			assertEquals(this.elementExists, true);
			jar.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

	}

	@After
	public void deleteFile() {
		this.jarFile.delete();
	}

	private boolean fileExists(final File[] files, final String element) {
		boolean elementExists = false;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				if (files[i].getName().equals(element)) {
					elementExists = true;
				}
			}
		}

		return elementExists;
	}

}
