package org.charless.qxmaven.mojo.qooxdoo.utils;

/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

public class TestUtils {
	public static final int BUFFER_SIZE = 2048;

	/**
	 * Deletes a directory and its contents.
	 * 
	 * @param dir
	 *            The base directory of the included and excluded files.
	 * @throws IOException
	 * @throws MojoExecutionException
	 *             When a directory failed to get deleted.
	 */
	public static void removeDirectory(File dir) throws IOException {
		if (dir != null) {
			Log log = new SilentLog();
			FileSetManager fileSetManager = new FileSetManager(log, false);

			FileSet fs = new FileSet();
			fs.setDirectory(dir.getPath());
			fs.addInclude("**/**");
			fileSetManager.delete(fs);

		}
	}

	public static void setFileModifiedTime(File file)
			throws InterruptedException {
		Thread.sleep(100);
		// round down to the last second
		long time = System.currentTimeMillis();
		time = time - (time % 1000);
		file.setLastModified(time);
		// wait at least a second for filesystems that only record to the
		// nearest second.
		Thread.sleep(1000);
	}

	public static boolean unjar(File sourceFile, File destDirectory) {
		System.out.println("Extracting: " + sourceFile+" to "+destDirectory);
		try {
			BufferedOutputStream dest = null;

			FileInputStream fis = new FileInputStream(sourceFile);
			JarInputStream zis = new JarInputStream(new BufferedInputStream(fis));

			JarEntry entry = null;
			
			while ((entry = zis.getNextJarEntry()) != null) {
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					// write the file to the disk
					File out = new File(destDirectory,entry.getName());
					if (! out.getParentFile().isDirectory()) {
						 out.getParentFile().mkdirs();
					}
					if (entry.isDirectory()) {
						out.mkdirs();
					} else {
						FileOutputStream fos = new FileOutputStream(out);
						dest = new BufferedOutputStream(fos, BUFFER_SIZE);
						while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
							dest.write(data, 0, count);
						}
						// close the output streams
						dest.flush();
						dest.close();
					}
					
				}
			

			zis.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}


}
