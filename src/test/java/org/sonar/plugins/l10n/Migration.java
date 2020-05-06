package org.sonar.plugins.l10n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.Test;

public class Migration {

  @Test
  public void migration() throws IOException {
    final String baseDir = this.getClass().getClassLoader().getResource("org/sonar/l10n").getPath();

    File coreProps = new File(baseDir + "/core.properties");
    File tempProps = new File(baseDir + "/core_temp.properties");

    if (!tempProps.createNewFile()) {
      System.err.println("Failed to create temp properties file");
      return;
    }

    Properties ko = getProperties(baseDir + "/core_ko.properties");

    try (
      BufferedReader br = new BufferedReader(new FileReader(coreProps));
      PrintWriter pw = new PrintWriter(new FileWriter(tempProps))
    ) {
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.trim().length() == 0) {
          pw.println(line);
          continue;
        }
        if (line.startsWith("#")) {
          pw.println(line);
          continue;
        }

        String[] array = line.trim().split("=");
        if (array.length != 2) {
          System.err.println(line);
          pw.println(line);
          continue;
        }

        String key = array[0];
        String value = array[1];
        String valueKo = ko.getProperty(key);

        pw.println(key + "=" + (valueKo == null ? value : valueKo));
        pw.flush();
      }
    } catch (Exception e) {
      throw e;
    }

    // Delete the original file
    if (!coreProps.delete()) {
      System.err.println("Could not delete file");
      return;
    }

    // Rename the new file to the filename the original file had.
    if (!tempProps.renameTo(coreProps)) {
      System.err.println("Could not rename file");
    }

  }

  private Properties getProperties(String pathname) throws IOException {
    final File sourceFile = new File(pathname);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8));) {
      String line = null;
      Properties prop = new Properties();

      while ((line = reader.readLine()) != null) {
        if (line.trim().length() == 0) {
          continue;
        }
        if (line.startsWith("#")) {
          continue;
        }
        String[] array = line.trim().split("=");
        if (array.length != 2) {
          System.out.println(line);
          continue;
        }
        String key = array[0];
        String value = array[1];
        prop.setProperty(key, value);
      }

      return prop;
    } catch (IOException e) {
      throw e;
    }
  }
}
