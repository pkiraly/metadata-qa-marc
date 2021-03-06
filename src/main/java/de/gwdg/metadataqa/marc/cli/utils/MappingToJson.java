package de.gwdg.metadataqa.marc.cli.utils;

import de.gwdg.metadataqa.marc.EncodedValue;
import de.gwdg.metadataqa.marc.cli.parameters.MappingParameters;
import de.gwdg.metadataqa.marc.definition.*;
import de.gwdg.metadataqa.marc.definition.structure.ControlfieldPositionDefinition;
import de.gwdg.metadataqa.marc.definition.structure.DataFieldDefinition;
import de.gwdg.metadataqa.marc.definition.structure.Indicator;
import de.gwdg.metadataqa.marc.definition.structure.SubfieldDefinition;
import de.gwdg.metadataqa.marc.definition.tags.control.*;
import de.gwdg.metadataqa.marc.definition.controlpositions.Control006Positions;
import de.gwdg.metadataqa.marc.definition.controlpositions.Control007Positions;
import de.gwdg.metadataqa.marc.definition.controlpositions.Control008Positions;
import de.gwdg.metadataqa.marc.definition.controlpositions.LeaderPositions;
import de.gwdg.metadataqa.marc.definition.general.codelist.CodeList;
import de.gwdg.metadataqa.marc.utils.MarcTagLister;
import de.gwdg.metadataqa.marc.utils.keygenerator.DataFieldKeyGenerator;
import net.minidev.json.JSONValue;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MappingToJson {

  private static final Logger logger = Logger.getLogger(MappingToJson.class.getCanonicalName());

  private static final List<String> nonMarc21TagLibraries = Arrays.asList(
    "oclctags", "fennicatags", "dnbtags", "sztetags", "genttags", "nkcrtags",
    "holdings"
  );

  private boolean exportSubfieldCodes = false;
  private boolean exportSelfDescriptiveCodes = false;
  private Map<String, Object> mapping;
  private final Options options;
  private MappingParameters parameters;

  public MappingToJson(String[] args) throws ParseException {
    parameters = new MappingParameters(args);
    options = parameters.getOptions();

    exportSubfieldCodes = parameters.doExportSubfieldCodes();
    exportSelfDescriptiveCodes = parameters.doExportSelfDescriptiveCodes();

    mapping = new LinkedHashMap<>();
    mapping.put("$schema", "https://format.gbv.de/schema/avram/schema.json");
    mapping.put("title", "MARC 21 Format for Bibliographic Data.");
    mapping.put("description", "MARC 21 Format for Bibliographic Data.");
    mapping.put("url", "https://www.loc.gov/marc/bibliographic/");
  }

  public void setExportSubfieldCodes(boolean exportSubfieldCodes) {
    this.exportSubfieldCodes = exportSubfieldCodes;
  }

  public String toJson() {
    return JSONValue.toJSONString(mapping);
  }

  public void build() {
    Map fields = new LinkedHashMap<>();

    Map<String, Object> tag = new LinkedHashMap<>();
    tag.put("repeatable", false);
    Map<String, Object> positions = new LinkedHashMap<>();

    LeaderPositions leaderSubfields = LeaderPositions.getInstance();
    for (ControlfieldPositionDefinition subfield : leaderSubfields.getPositionList()) {
      Map<String, Object> position = controlPositionToJson(subfield);
      String key = (String) position.remove("position");
      positions.put(key, position);
    }
    tag.put("positions", positions);
    fields.put("LDR", tag);

    List<DataFieldDefinition> simpleControlFields = Arrays.asList(
      Control001Definition.getInstance(),
      Control003Definition.getInstance(),
      Control005Definition.getInstance()
    );
    for (DataFieldDefinition field : simpleControlFields) {
      tag = new LinkedHashMap<>();
      tag.put("tag", field.getTag());
      tag.put("label", field.getLabel());
      tag.put("repeatable", resolveCardinality(field.getCardinality()));
      fields.put(field.getTag(), tag);
    }

    tag = new LinkedHashMap<>();
    tag.put("tag", "006");
    tag.put("label", Control006Definition.getInstance().getLabel());
    tag.put("repeatable", resolveCardinality(Control006Definition.getInstance().getCardinality()));
    Map<String, Object> types = new LinkedHashMap<>();
    for (String type : Control006Positions.getInstance().getPositions().keySet()) {
      Map<String, Object> typeMap = new LinkedHashMap<>();
      positions = new LinkedHashMap<>();
      for (ControlfieldPositionDefinition subfield : Control006Positions.getInstance().getPositions().get(type)) {
        Map<String, Object> position = controlPositionToJson(subfield);
        String key = (String) position.remove("position");
        positions.put(key, position);
      }
      typeMap.put("positions", positions);
      types.put(type, typeMap);
    }
    tag.put("types", types);
    fields.put("006", tag);

    tag = new LinkedHashMap<>();
    tag.put("tag", "007");
    tag.put("label", Control007Definition.getInstance().getLabel());
    tag.put("repeatable", resolveCardinality(Control007Definition.getInstance().getCardinality()));
    types = new LinkedHashMap<>();
    for (String category : Control007Positions.getInstance().getPositions().keySet()) {
      Map<String, Object> typeMap = new LinkedHashMap<>();
      positions = new LinkedHashMap<>();
      for (ControlfieldPositionDefinition subfield : Control007Positions.getInstance().getPositions().get(category)) {
        Map<String, Object> position = controlPositionToJson(subfield);
        String key = (String) position.remove("position");
        positions.put(key, position);
      }
      typeMap.put("positions", positions);
      types.put(category, typeMap);
    }
    tag.put("types", types);
    fields.put("007", tag);

    tag = new LinkedHashMap<>();
    tag.put("tag", "008");
    tag.put("label", Control008Definition.getInstance().getLabel());
    tag.put("repeatable", resolveCardinality(Control008Definition.getInstance().getCardinality()));
    types = new LinkedHashMap<>();
    for (String type : Control008Positions.getInstance().getPositions().keySet()) {
      Map<String, Object> typeMap = new LinkedHashMap<>();
      positions = new LinkedHashMap<>();
      for (ControlfieldPositionDefinition subfield : Control008Positions.getInstance().getPositions().get(type)) {
        Map<String, Object> position = controlPositionToJson(subfield);
        String key = (String) position.remove("position");
        positions.put(key, position);
      }
      typeMap.put("positions", positions);
      types.put(type, typeMap);
    }
    tag.put("types", types);
    fields.put("008", tag);

    for (Class<? extends DataFieldDefinition> tagClass : MarcTagLister.listTags()) {
      if (isNonMarc21Tag(tagClass))
        continue;

      Method getInstance;
      DataFieldDefinition fieldTag;
      try {
        getInstance = tagClass.getMethod("getInstance");
        fieldTag = (DataFieldDefinition) getInstance.invoke(tagClass);
        dataFieldToJson(fields, fieldTag);
      } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
        logger.log(Level.SEVERE, "build", e);
      }
    }
    mapping.put("fields", fields);
  }

  private static boolean isNonMarc21Tag(Class<? extends DataFieldDefinition> tagClass) {
    boolean isNonMarc21Tag = false;
    for (String nonCore : nonMarc21TagLibraries) {
      if (tagClass.getCanonicalName().contains(nonCore)) {
        isNonMarc21Tag = true;
        break;
      }
    }
    return isNonMarc21Tag;
  }

  private static Map<String, Object> controlPositionToJson(ControlfieldPositionDefinition subfield) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("position", subfield.formatPositon());
    values.put("label", subfield.getLabel());
    values.put("url", subfield.getDescriptionUrl());

    if (subfield.getCodes() != null) {
      LinkedHashMap<String, Object> codes = new LinkedHashMap<>();
      for (EncodedValue code : subfield.getCodes()) {
        Map<String, Object> codeMap = new LinkedHashMap<>();
        // codeMap.put("code", code.getCode());
        codeMap.put("label", code.getLabel());
        codes.put(code.getCode(), codeMap);
      }
      values.put("codes", codes);
    }
    if (subfield.getHistoricalCodes() != null) {
      LinkedHashMap<String, Object> codes = new LinkedHashMap<>();
      for (EncodedValue code : subfield.getHistoricalCodes()) {
        Map<String, Object> codeMap = new LinkedHashMap<>();
        // codeMap.put("code", code.getCode());
        codeMap.put("label", code.getLabel());
        codes.put(code.getCode(), codeMap);
      }
      values.put("historical-codes", codes);
    }
    return values;
  }

  private void dataFieldToJson(Map fields, DataFieldDefinition tag) {
    DataFieldKeyGenerator keyGenerator = new DataFieldKeyGenerator(tag, parameters.getSolrFieldType());

    Map<String, Object> tagMap = new LinkedHashMap<>();
    tagMap.put("tag", tag.getTag());
    tagMap.put("label", tag.getLabel());
    tagMap.put("url", tag.getDescriptionUrl());
    tagMap.put("repeatable", resolveCardinality(tag.getCardinality()));
    if (exportSelfDescriptiveCodes)
      tagMap.put("solr", keyGenerator.getIndexTag());

    if (parameters.doExportFrbrFunctions())
      extractFunctions(tagMap, tag.getFrbrFunctions());

    if (parameters.doExportCompilanceLevel())
      extractCompilanceLevel(tagMap, tag.getNationalCompilanceLevel(), tag.getMinimalCompilanceLevel());

    tagMap.put("indicator1", indicatorToJson(tag.getInd1()));
    tagMap.put("indicator2", indicatorToJson(tag.getInd2()));

    Map<String, Object> subfields = new LinkedHashMap<>();
    for (SubfieldDefinition subfield : tag.getSubfields()) {
      subfields.put(subfield.getCode(), subfieldToJson(subfield, keyGenerator));
    }
    tagMap.put("subfields", subfields);

    if (tag.getHistoricalSubfields() != null) {
      subfields = new LinkedHashMap<>();
      for (EncodedValue code : tag.getHistoricalSubfields()) {
        Map<String, Object> labelMap = new LinkedHashMap<>();
        labelMap.put("label", code.getLabel());
        subfields.put(code.getCode(), labelMap);
      }
      tagMap.put("historical-subfields", subfields);
    }

    fields.put(tag.getTag(), tagMap);
  }

  private void extractFunctions(Map<String, Object> tagMap, List<FRBRFunction> functions) {
    if (functions != null && !functions.isEmpty()) {
      List<String> paths = new ArrayList<>();
      for (FRBRFunction function : functions) {
        paths.add(function.getPath());
      }
      tagMap.put("frbr-functions", paths);
    }
  }

  private Map<String, Object> subfieldToJson(SubfieldDefinition subfield, DataFieldKeyGenerator keyGenerator) {
    Map<String, Object> codeMap = new LinkedHashMap<>();
    codeMap.put("label", subfield.getLabel());
    codeMap.put("repeatable", resolveCardinality(subfield.getCardinality()));

    if (exportSelfDescriptiveCodes)
      codeMap.put("solr", keyGenerator.forSubfield(subfield));

    if (subfield.getCodeList() != null
        && !subfield.getCodeList().getCodes().isEmpty()) {
      CodeList codeList = subfield.getCodeList();
      Map<String, Object> meta = new LinkedHashMap<>();
      meta.put("name", codeList.getName());
      meta.put("url", codeList.getUrl());

      if (exportSubfieldCodes
          && !codeList.getName().equals("MARC Organization Codes")) {
        Map<String, Object> codes = new LinkedHashMap<>();
        for (EncodedValue code : subfield.getCodeList().getCodes()) {
          Map<String, Object> codeListMap = new LinkedHashMap<>();
          codeListMap.put("label", code.getLabel());
          codes.put(code.getCode(), codeListMap);
        }
        meta.put("codes", codes);
      }
      codeMap.put("codelist", meta);
    }

    if (parameters.doExportFrbrFunctions())
      extractFunctions(codeMap, subfield.getFrbrFunctions());

    if (parameters.doExportCompilanceLevel())
      extractCompilanceLevel(codeMap, subfield.getNationalCompilanceLevel(), subfield.getMinimalCompilanceLevel());

    return codeMap;
  }

  private void extractCompilanceLevel(Map<String, Object> codeMap,
                                      CompilanceLevel nationalCompilanceLevel,
                                      CompilanceLevel minimalCompilanceLevel) {
    Map<String, String> levels = new LinkedHashMap<>();
    if (nationalCompilanceLevel != null)
      levels.put("national", nationalCompilanceLevel.getLabel());

    if (minimalCompilanceLevel != null)
      levels.put("minimal", minimalCompilanceLevel.getLabel());

    if (!levels.isEmpty())
      codeMap.put("compilance-level", levels);
  }

  private static Map<String, Object> indicatorToJson(Indicator indicator) {
    if (!indicator.exists()) {
      return null;
    }
    Map<String, Object> value = new LinkedHashMap<>();
    value.put("label", indicator.getLabel());
    Map<String, Object> codes = new LinkedHashMap<>();
    for (EncodedValue code : indicator.getCodes()) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("label", code.getLabel());
      codes.put(code.getCode(), map);
    }
    value.put("codes", codes);
    if (indicator.getHistoricalCodes() != null) {
      codes = new LinkedHashMap<>();
      for (EncodedValue code : indicator.getHistoricalCodes()) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", code.getLabel());
        codes.put(code.getCode(), map);
      }
      value.put("historical-codes", codes);
    }
    return value;
  }

  private boolean resolveCardinality(Cardinality cardinality) {
    return cardinality.getCode().equals("R");
  }

  public static void main(String[] args) throws ParseException {
    MappingToJson mapping = new MappingToJson(args);
    mapping.build();
    System.out.println(mapping.toJson());
  }
}
