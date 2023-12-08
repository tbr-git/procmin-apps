package org.processmining.processcomparator.tbutils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.processmining.framework.util.ProMClassLoader;

public class MyProMDebugClassLoader extends ProMClassLoader {
  
  private URL[] gradlePackedClasspath = null;

  // Before
  // -ea -Xmx8G
  // -Djava.library.path=C:\Users\brockhoff\.ProM\packages\lpsolve-5.5.4\lib\win64
  // -Djava.util.Arrays.useLegacyMergeSort=true
  // -Djava.system.class.loader=org.processmining.framework.util.ProMClassLoader
  // --add-opens java.base/java.net=ALL-UNNAMED -verbose
  public MyProMDebugClassLoader(ClassLoader loader) {
    super(loader);
    // TODO Auto-generated constructor stub
  }

  public void appendToClassPathForInstrumentation(String path) throws MalformedURLException {
    addURL(new File(path).toURI().toURL());
  }

  @Override
  public URL[] getURLs() {
    URL[] res = super.getURLs();
    // Gradle "hides" the actual class path in a packaging jar
    res = expandGradle(res);
    return res;
  }
  
  /**
   * Hacky way to unfold the packed classpath.
   * <p>
   * Checks if first URL on classpath seems to be a packed jar (by name matching).
   * Finds the manifest (should be the one in the first jar, which is the packed jar).
   * Extract the classpath from the jar and flattens it.
   * 
   *  
   * @param urls 
   * @return
   */
  private URL[] expandGradle(URL[] urls) {
    if (urls[0].getFile().contains("gradle-javaexec")) {
      // Unfolding the packaging jar's class path
      if (gradlePackedClasspath == null) {
        try {
          URL urlManifest = this.findResource("META-INF/MANIFEST.MF");
          Manifest manifest = new Manifest(urlManifest.openStream());
          Attributes attr = manifest.getMainAttributes();
          String classPath = attr.getValue("Class-Path");
          String[] onClassPath = classPath.split(" ");
          gradlePackedClasspath = new URL[onClassPath.length];
          int i = 0;
          for (String entry : onClassPath) {
            gradlePackedClasspath[i] = Path.of(entry.substring(6)).toUri().toURL();
            i++;
          }
        } catch (IOException e) {
          e.printStackTrace();
          return urls;
        }
      }
      URL[] res = new URL[gradlePackedClasspath.length + urls.length - 1];
      int i = 0;
      for (URL url : gradlePackedClasspath) {
        res[i] = url;
        i++;
      }
      boolean first = true;
      for (URL url : urls) {
        if (first) {
          first = false;
          continue;
        }
        res[i] = url;
        i++;
      }
      return res;
    }
    else {
      return urls;
    }
  }
  
  

}
