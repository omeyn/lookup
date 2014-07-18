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
    {"MatchType", "Confidence", "Synonym", "Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "ScientificName"};

  public static void main(String[] args) throws IOException {
//    String inputFileName = "/Users/oliver/Documents/asian_nodes_mtg/ias_full.csv";
    String inputFileName = args[0];
    String outputFileName = args[1];
    File inputFile = new File(inputFileName);
    File outputFile = new File(outputFileName);
    FileOutputStream os = new FileOutputStream(outputFile);
    CSVReader csvReader = new CSVReader(inputFile, "UTF-8", ",", '"', 0);
    String[] headerRow = csvReader.next();
    byte[] comma = ",".getBytes();
    for (String field : headerRow) {
      os.write(field.getBytes());
      os.write(comma);
    }
    for (String field : GBIF_HEADERS) {
      String name = "GBIF_" + field;
      os.write(name.getBytes());
      os.write(comma);
    }
    os.write("\n".getBytes());

    while (csvReader.hasNext()) {
      String[] fields = csvReader.next();
      logFields(fields);
      // ID, K, P, C, O, F, G, sp epithet, sciName
      ParseResult<NameUsageMatch> result = NubLookupInterpreter
        .nubLookup(fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], null);
      //      System.out.println(result.getPayload().toString());
      for (String field : fields) {
        os.write(field.getBytes());
        os.write(comma);
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

      os.write("\n".getBytes());
    } os.close();
  }

  private static void writeField(FileOutputStream os, String field) throws IOException {
    String quoted = "\"" + field + "\"";
    os.write(quoted.getBytes());
    os.write(",".getBytes());
  }

  private static void logFields(String[] fields) {
    StringBuilder sb = new StringBuilder();
    for (String field : fields) {
      sb.append(field + " ");
    }
    System.out.println(sb.toString());
  }
}
