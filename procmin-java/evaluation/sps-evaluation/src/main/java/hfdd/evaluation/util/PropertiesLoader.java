package hfdd.evaluation.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class PropertiesLoader {
  
  /**
   * Find in and parse the provided .property file. 
   * 
   * Retrieves the classloader of the provided class and attempts to load the given config file.
   * 
   * @param clazz Class which classloader is used to open the resource stream
   * @param fileName Name of the config file
   * @return
   * @throws IOException
   */
  public static Properties readPropertiesFileFromResources(Class<?> clazz, String fileName) throws IOException {
    InputStream fis = null;
    Properties prop = null;
    try {
      fis = clazz.getClassLoader().getResourceAsStream(fileName);
      prop = new Properties();
      prop.load(fis);
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } finally {
      fis.close();
    }
    return prop;
  }
  
  public static Optional<Double> parsePropertyDouble(Properties prop, String key) {
    String value = prop.getProperty(key);
    if (value == null) {
      return Optional.empty();
    }
    else {
      try {
        Double v = Double.parseDouble(value);
        return Optional.of(v);
      }
      catch (Exception e) {
        return Optional.empty();
      }
    }
  }

  public static Optional<Integer> parsePropertyInt(Properties prop, String key) {
    String value = prop.getProperty(key);
    if (value == null) {
      return Optional.empty();
    }
    else {
      try {
        Integer v = Integer.parseInt(value);
        return Optional.of(v);
      }
      catch (Exception e) {
        return Optional.empty();
      }
    }
  }

}
