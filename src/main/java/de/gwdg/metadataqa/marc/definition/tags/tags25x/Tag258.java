package de.gwdg.metadataqa.marc.definition.tags.tags25x;

import de.gwdg.metadataqa.marc.definition.Cardinality;
import de.gwdg.metadataqa.marc.definition.structure.DataFieldDefinition;
import de.gwdg.metadataqa.marc.definition.structure.Indicator;
import de.gwdg.metadataqa.marc.definition.general.parser.LinkageParser;
import static de.gwdg.metadataqa.marc.definition.FRBRFunction.*;

/**
 * Philatelic Issue Data
 * http://www.loc.gov/marc/bibliographic/bd258.html
 */
public class Tag258 extends DataFieldDefinition {
  private static Tag258 uniqueInstance;

  private Tag258() {
    initialize();
    postCreation();
  }

  public static Tag258 getInstance() {
    if (uniqueInstance == null)
      uniqueInstance = new Tag258();
    return uniqueInstance;
  }

  private void initialize() {
    tag = "258";
    label = "Philatelic Issue Data";
    mqTag = "PhilatelicIssue";
    cardinality = Cardinality.Repeatable;
    descriptionUrl = "https://www.loc.gov/marc/bibliographic/bd258.html";
    setCompilanceLevels("O");

    ind1 = new Indicator();
    ind2 = new Indicator();

    setSubfieldsWithCardinality(
      "a", "Issuing jurisdiction", "NR",
      "b", "Denomination", "NR",
      "6", "Linkage", "NR",
      "8", "Field link and sequence number", "R"
    );

    getSubfield("6").setContentParser(LinkageParser.getInstance());

    getSubfield("a")
      .setMqTag("jurisdiction")
      .setCompilanceLevels("A");

    getSubfield("b")
      .setMqTag("denomination")
      .setCompilanceLevels("A");

    getSubfield("6")
      .setBibframeTag("linkage")
      .setFrbrFunctions(ManagementIdentify, ManagementProcess)
      .setCompilanceLevels("A");

    getSubfield("8")
      .setMqTag("fieldLink")
      .setFrbrFunctions(ManagementIdentify, ManagementProcess)
      .setCompilanceLevels("O");
  }
}
