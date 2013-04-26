package com.boxysystems.scriptmonkey.intellij.util;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class JarExploder {

  private static final Logger logger = Logger.getLogger(JarExploder.class);

  public static void explodeJar(File destDir, File jarFile)
    throws IOException {
    try {
      ZipFile zf = new ZipFile(jarFile);
      Map<String, Integer> entrySizes = new HashMap<String, Integer>();

      Enumeration e = zf.entries();
      while (e.hasMoreElements()) {
        ZipEntry ze = (ZipEntry) e.nextElement();

        entrySizes.put(ze.getName(), (int) ze
          .getSize());
      }

      zf.close();

      FileInputStream fileInputStream = new FileInputStream(
        jarFile);
      BufferedInputStream bufferedInputStream = new BufferedInputStream(
        fileInputStream);
      ZipInputStream zipInputStream = new ZipInputStream(
        bufferedInputStream);

      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if (zipEntry.isDirectory()) {
          continue;
        }

        int size = (int) zipEntry.getSize();

        // -1 means unknown size.
        if (size == -1) {
          size = entrySizes
            .get(zipEntry.getName());
        }

        // Read the content of this zip entry
        byte[] b = new byte[size];
        int rb = 0;
        int chunk;
        while ((size - rb) > 0) {
          chunk = zipInputStream.read(b, rb, size - rb);
          if (chunk == -1) {
            break;
          }

          rb += chunk;
        }

        String savedFilePath = saveFile(destDir, zipEntry.getName(), b);
        File saveFile = new File(savedFilePath);
        saveFile.setLastModified(zipEntry.getTime());
      }
    } catch (FileNotFoundException e) {
      logger.error("File not found !", e);
    } catch (IOException e) {
      logger.error(e);
    }

  }

  private static String saveFile(File destDir, String fileName,
                                 byte[] bytes) throws IOException {
    File destFile = new File(destDir, fileName);
    FileOutputStream fos = null;
    try {
      if (!destFile.exists()) {
        destFile.getParentFile().mkdirs();
      }
      fos = new FileOutputStream(destFile);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      bos.write(bytes);
      bos.flush();
      return destFile.getAbsolutePath();
    } catch (java.io.FileNotFoundException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
    finally {
      if (fos != null) {
        fos.close();
      }
    }
    return null;
  }

}
