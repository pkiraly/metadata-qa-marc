package de.gwdg.metadataqa.marc.utils.pica;

import de.gwdg.metadataqa.marc.MarcFactory;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PicaReader implements MarcReader {

  private static final Logger logger = Logger.getLogger(PicaReader.class.getCanonicalName());

  private BufferedReader bufferedReader = null;
  private String line = null;
  private boolean nextIsConsumed = false;
  private int lineNumber = 0;
  private List<PicaLine> lines = new ArrayList<>();

  public PicaReader(String fileName) {
    try {
      bufferedReader = new BufferedReader(new FileReader(fileName));
    } catch (IOException e) {
      logger.log(Level.WARNING, "error in PicaReader()", e);
    }
  }

  @Override
  public boolean hasNext() {
    if (lineNumber == 0 || nextIsConsumed) {
      try {
        line = bufferedReader.readLine();
      } catch (IOException e) {
        logger.log(Level.WARNING, "error in hasNext()", e);
      }
      lineNumber++;
      nextIsConsumed = false;
    }
    return (line != null);
  }

  @Override
  public Record next() {
    Record marc4jRecord = null;
    boolean finished = false;
    while (line != null && !finished) {
      PicaLine picaLine = new PicaLine(line, lineNumber);
      if (picaLine.isSkippable() && !lines.isEmpty()) {
        marc4jRecord = MarcFactory.createRecordFromPica(lines);
        finished = true;
        lines = new ArrayList<>();
      }

      if (picaLine.isValidTag()) {
        lines.add(picaLine);
      }

      try {
        line = bufferedReader.readLine();
        lineNumber++;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "next", e);
      }
    } // while

    if (line == null && !lines.isEmpty()) {
      marc4jRecord = MarcFactory.createRecordFromPica(lines);
    }
    return marc4jRecord;
  }
}
