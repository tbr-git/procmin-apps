package de.rwth.processmining.tb.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.xeslite.parser.XesLiteXmlParser;

public class LogLoader {
	private final static Logger logger = LogManager.getLogger( LogLoader.class );
	
	public static XLog loadLog(String pathLog) throws FileNotFoundException, IOException {
		logger.info("Loading log: {} ...", pathLog);
		File logFile = new File(pathLog);
		return loadLog(logFile);
  }
	
	public static XLog loadLog(File logFile) throws FileNotFoundException, IOException {
		InputStream logInputStream;

		if (logFile.getName().endsWith(".gz")) {
      logInputStream = new GZIPInputStream(new FileInputStream(logFile), 65536);
		}
		else if (logFile.getName().endsWith(".xes")) {
      logInputStream = new FileInputStream(logFile);
		}
		else {
		  return null;
		}
		logInputStream = new BufferedInputStream(logInputStream, 65536);
		
		
    List<XLog> parsedLogs = null;
    XesLiteXmlParser parserLog = new XesLiteXmlParser(true);
    try {
      parsedLogs = parserLog.parse(logInputStream);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Failed to load log");
      return null;
    }
    logger.info("Done loading log.");
    return parsedLogs.get(0);
	  
	}
}
	
