package org.gbif;

import org.gbif.api.model.checklistbank.NameUsageMatch;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.file.CSVReader;
import org.gbif.occurrence.processor.interpreting.util.NubLookupInterpreter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsiaNodesLookup {

  private static final Logger LOG = LoggerFactory.getLogger(AsiaNodesLookup.class);
  private static final String[] GBIF_HEADERS =
    {"MatchType", "Confidence", "Synonym", "Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "ScientificName",
    "KingdomKey", "PhylumKey", "ClassKey", "OrderKey", "FamilyKey", "GenusKey", "NameUsageKey"};
  private static final String HEADER_PREFIX = "GBIF_";
  private static final byte[] NEWLINE = "\n".getBytes();
  private static final byte[] COMMA = ",".getBytes();

  public static void main(String[] args) throws IOException {
    String inputFileName = args[0];
    String outputFileName = args[1];
    File inputFile = new File(inputFileName);
    File outputFile = new File(outputFileName);
    FileOutputStream os = new FileOutputStream(outputFile);
    CSVReader csvReader = new CSVReader(inputFile, "UTF-8", ",", '"', 0);
    String[] headerRow = csvReader.next();

    for (String field : headerRow) {
      os.write(field.getBytes());
      os.write(COMMA);
    }
    for (String field : GBIF_HEADERS) {
      String name = HEADER_PREFIX + field;
      os.write(name.getBytes());
      os.write(COMMA);
    }
    os.write(NEWLINE);

    while (csvReader.hasNext()) {
      String[] fields = csvReader.next();
      logFields(fields);
      // ID, K, P, C, O, F, G, sp epithet, sciName
      ParseResult<NameUsageMatch> result = NubLookupInterpreter
        .nubLookup(fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], null);
      for (String field : fields) {
        os.write(field.getBytes());
        os.write(COMMA);
      }
      NameUsageMatch match = result.getPayload();
      writeField(os, match.getMatchType().toString());
      writeField(os, match.getConfidence().toString());
      writeField(os, match.isSynonym() ? "y" : "n");
      writeField(os, match.getKingdom());
      writeField(os, match.getPhylum());
      writeField(os, match.getClazz());
      writeField(os, match.getOrder());
      writeField(os, match.getFamily());
      writeField(os, match.getGenus());
      writeField(os, match.getScientificName());
      writeField(os, match.getKingdomKey());
      writeField(os, match.getPhylumKey());
      writeField(os, match.getClassKey());
      writeField(os, match.getOrderKey());
      writeField(os, match.getFamilyKey());
      writeField(os, match.getGenusKey());
      writeField(os, match.getUsageKey());

      os.write(NEWLINE);
    } os.close();
  }

  private static void writeField(FileOutputStream os, Integer key) throws IOException {
    if (key != null) {
      os.write(key.toString().getBytes("UTF-8"));
      os.write(",".getBytes("UTF-8"));
    }
  }

  private static void writeField(FileOutputStream os, String field) throws IOException {
    String quoted = '\"' + field + '\"';
    os.write(quoted.getBytes("UTF-8"));
    os.write(COMMA);
  }

  private static void logFields(String[] fields) {
    StringBuilder sb = new StringBuilder();
    for (String field : fields) {
      sb.append(field).append(' ');
    }
    LOG.debug(sb.toString());
  }
}
