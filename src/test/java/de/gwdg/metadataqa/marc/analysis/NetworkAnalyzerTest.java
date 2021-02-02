package de.gwdg.metadataqa.marc.analysis;

import de.gwdg.metadataqa.api.util.FileUtils;
import de.gwdg.metadataqa.marc.DataField;
import de.gwdg.metadataqa.marc.MarcFactory;
import de.gwdg.metadataqa.marc.MarcRecord;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NetworkAnalyzerTest {

  MarcRecord record;

  @Before
  public void setup() throws IOException, URISyntaxException {
    List<String> lines = FileUtils.readLines("marctxt/010000011.mrctxt");
    record = MarcFactory.createFromFormattedText(lines);
  }

  @Test
  public void process() {
    NetworkAnalyzer analyzer = new NetworkAnalyzer(record);
    Set<DataField> collector = analyzer.process(1);
    assertEquals(5, collector.size());
    assertEquals(
      Arrays.asList("084", "655", "710", "810", "810"),
      collector.stream()
        .map(DataField::getTag)
        .sorted()
        .collect(Collectors.toList())
    );
  }
}