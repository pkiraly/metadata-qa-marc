package de.gwdg.metadataqa.marc.definition;

public enum MarcVersion {
  MARC21("MARC21", "MARC21"),
  BL("BL", "British Library"),
  DNB("DNB", "Deutsche Nationalbibliothek"),
  FENNICA("FENNICA", "National Library of Finland"),
  GENT("GENT", "Universiteitsbibliotheek Gent"),
  NKCR("NKCR", "National Library of the Czech Republic"),
  OCLC("OCLC", "OCLC"),
  SZTE("SZTE", "Szegedi Tudományegyetem"),
  UNIMARC("UNIMARC", "UNIMARC"),
  MARC21NO("MARC21NO", "MARC21 profile for Norwegian public libraries")
  ;

  String code;
  String label;

  MarcVersion(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public static MarcVersion byCode(String code) {
    for (MarcVersion version : values())
      if (version.code.equals(code))
        return version;
    return null;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }
}
