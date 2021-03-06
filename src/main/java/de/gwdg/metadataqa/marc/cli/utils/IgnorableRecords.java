package de.gwdg.metadataqa.marc.cli.utils;

import de.gwdg.metadataqa.marc.dao.DataField;
import de.gwdg.metadataqa.marc.dao.MarcRecord;
import de.gwdg.metadataqa.marc.MarcSubfield;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IgnorableRecords {

  private List<DataField> conditions;

  public void parseInput(String input) {
    if (StringUtils.isNotBlank(input)) {
      conditions = new ArrayList<>();
      for (String field : input.split(",")) {
        DataField f = parseField(field);
        if (f != null)
          conditions.add(f);
      }
    }
  }

  public List<DataField> getConditions() {
    return conditions;
  }

  public boolean isEmpty() {
    return conditions == null || conditions.isEmpty();
  }

  private DataField parseField(String field) {
    var pattern = Pattern.compile("^(.{3})\\$(.)=(.*)$");
    var matcher = pattern.matcher(field);
    if (matcher.matches()) {
      String tag = matcher.group(1);
      String subfield = matcher.group(2);
      String value = matcher.group(3);
      return new DataField(tag, "  $" + subfield + value);
    }
    return null;
  }

  public boolean isIgnorable(MarcRecord marcRecord) {
    if (isEmpty())
      return false;

    for (DataField condition : conditions) {
      List<DataField> recordFields = marcRecord.getDatafield(condition.getTag());
      if (recordFields == null || recordFields.isEmpty())
        continue;

      for (DataField recordField : recordFields) {
        MarcSubfield subfieldCond = condition.getSubfields().get(0);
        String code = subfieldCond.getCode();
        List<MarcSubfield> recordSubfields = recordField.getSubfield(code);

        if (recordSubfields == null || recordSubfields.isEmpty())
          continue;

        for (MarcSubfield recordSubfield : recordSubfields)
          if (recordSubfield.getValue().equals(subfieldCond.getValue()))
            return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return isEmpty() ? "" : conditions.toString();
  }
}
