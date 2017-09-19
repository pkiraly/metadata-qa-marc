package de.gwdg.metadataqa.marc.definition.tags84x;

import de.gwdg.metadataqa.marc.definition.Cardinality;
import de.gwdg.metadataqa.marc.definition.DataFieldDefinition;
import de.gwdg.metadataqa.marc.definition.Indicator;
import de.gwdg.metadataqa.marc.definition.general.codelist.FormatSourceCodes;

/**
 * Foreign MARC Information Field
 * http://www.loc.gov/marc/bibliographic/bd886.html
 */
public class Tag886 extends DataFieldDefinition {

	private static Tag886 uniqueInstance;

	private Tag886() {
		initialize();
	}

	public static Tag886 getInstance() {
		if (uniqueInstance == null)
			uniqueInstance = new Tag886();
		return uniqueInstance;
	}

	private void initialize() {
		tag = "886";
		label = "Foreign MARC Information Field";
		cardinality = Cardinality.Repeatable;
		ind1 = new Indicator("Type of field").setCodes(
			"0", "Leader",
			"1", "Variable control fields (002-009)",
			"2", "Variable data fields (010-999)"
		);
		ind2 = new Indicator("");
		setSubfieldsWithCardinality(
			"a", "Tag of the foreign MARC field", "NR",
			"b", "Content of the foreign MARC field", "NR",
			"2", "Source of data", "NR"
		);
		// TODO:
		// * $a-z - Foreign MARC subfield (R)
		// * $0-9 - Foreign MARC subfield (R)
		getSubfield("2").setCodeList(FormatSourceCodes.getInstance());
	}
}