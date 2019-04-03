package de.gwdg.metadataqa.marc.definition.tags.tags3xx;

import de.gwdg.metadataqa.marc.definition.Cardinality;
import de.gwdg.metadataqa.marc.definition.DataFieldDefinition;
import de.gwdg.metadataqa.marc.definition.Indicator;
import de.gwdg.metadataqa.marc.definition.general.codelist.CountryCodes;
import de.gwdg.metadataqa.marc.definition.general.codelist.OrganizationCodes;
import de.gwdg.metadataqa.marc.definition.general.parser.LinkageParser;
import static de.gwdg.metadataqa.marc.definition.FRBRFunction.*;

/**
 * Security Classification Control
 * http://www.loc.gov/marc/bibliographic/bd355.html
 */
public class Tag355 extends DataFieldDefinition {
  private static Tag355 uniqueInstance;

  private Tag355() {
    initialize();
    postCreation();
  }

  public static Tag355 getInstance() {
    if (uniqueInstance == null)
      uniqueInstance = new Tag355();
    return uniqueInstance;
  }

  private void initialize() {

    tag = "355";
    label = "Security Classification Control";
    mqTag = "SecurityClassificationControl";
    cardinality = Cardinality.Repeatable;
    descriptionUrl = "https://www.loc.gov/marc/bibliographic/bd355.html";

    ind1 = new Indicator("Controlled element")
      .setCodes(
        "0", "Document",
        "1", "Title",
        "2", "Abstract",
        "3", "Contents note",
        "4", "Author",
        "5", "Record",
        "8", "None of the above"
      )
      .setMqTag("controlledElement")
      .setFrbrFunctions(DiscoveryIdentify);

    ind2 = new Indicator();

    setSubfieldsWithCardinality(
      "a", "Security classification", "NR",
      "b", "Handling instructions", "R",
      "c", "External dissemination information", "R",
      "d", "Downgrading or declassification event", "NR",
      "e", "Classification system", "NR",
      "f", "Country of origin code", "NR",
      "g", "Downgrading date", "NR",
      "h", "Declassification date", "NR",
      "j", "Authorization", "R",
      "6", "Linkage", "NR",
      "8", "Field link and sequence number", "R"
    );

    getSubfield("f").setCodeList(CountryCodes.getInstance());
    getSubfield("j").setCodeList(OrganizationCodes.getInstance());

    getSubfield("6").setContentParser(LinkageParser.getInstance());

    getSubfield("a").setMqTag("rdf:value")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("b").setMqTag("handlingInstructions")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("c").setMqTag("externalDissemination")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("d").setMqTag("downgradingEvent")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("e").setMqTag("classificationSystem")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("f").setMqTag("countryOfOrigin")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("g").setMqTag("downgradingDate")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("h").setMqTag("declassificationDate")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("j").setBibframeTag("authorization")
      .setFrbrFunctions(UsageRestrict);
    getSubfield("6").setBibframeTag("linkage")
      .setFrbrFunctions(ManagementIdentify, ManagementProcess);
    getSubfield("8").setMqTag("fieldLink")
      .setFrbrFunctions(ManagementIdentify, ManagementProcess);
  }
}
