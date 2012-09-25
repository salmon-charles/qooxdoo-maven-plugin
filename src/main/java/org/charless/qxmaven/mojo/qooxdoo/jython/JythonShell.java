package org.charless.qxmaven.mojo.qooxdoo.jython;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;
import org.python.util.JLineConsole;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * Use the Jython shell to start an interactive Jython console, to execute a Python script or Python code
 */
public class JythonShell {
  private final InteractiveConsole console;

  public JythonShell(Properties properties, final Collection<String> dependenciesPath, final String[] args) {
    if(dependenciesPath != null && dependenciesPath.size() > 0) {
      properties.setProperty("python.path", Joiner.on(File.pathSeparator).join(dependenciesPath));
    }
    PySystemState.initialize(PySystemState.getBaseProperties(),properties);
    PySystemState systemState = Py.getSystemState();
	systemState.argv.clear ();
	for (String arg: args) {
		systemState.argv.append (new PyString (arg));
	}
    console = getConsole();
  }

  public void interact() {
    console.interact();
  }
  
  public void exec(String line) {
    console.exec(line);
  }
  
  public void execFile(String fileName) {
    console.execfile(fileName);
  }
  
  public void execFile(InputStream stream) {
    console.execfile(stream);
  }

  public static void main(String[] args) {
    JythonShell shell = new JythonShell(System.getProperties(), null, null);
    // process args
    shell.interact();
  }

  private static InteractiveConsole getConsole() {
    String interpreter = PySystemState.registry.getProperty("python.console", "");
    if (Strings.isNullOrEmpty(interpreter)) {
      return new JLineConsole();
    } else {
      try {
        return (InteractiveConsole) Class.forName(interpreter).newInstance();
      } catch (Throwable e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
