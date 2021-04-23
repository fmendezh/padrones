package fmendez.padron.dict;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import fmendez.padron.model.DistritoElectoral;
import lombok.SneakyThrows;

public class DistritosElectoralesDictionary implements Serializable {

  private Map<String, DistritoElectoral> cache = new HashMap<>();

  private static DistritoElectoral toElectoralDistrict(String line) {
    String[] fields = line.split(",");
    return DistritoElectoral.builder()
              .codigo(fields[0])
              .provincia(fields[1])
              .canton(fields[2])
              .distrito(fields[3].trim())
              .build();
  }

  @SneakyThrows
  public DistritosElectoralesDictionary(String inputFile) {
    try(Scanner scanner = new Scanner(new FileInputStream(inputFile))) {
      while (scanner.hasNext()) {
        DistritoElectoral distritoElectoral = toElectoralDistrict(scanner.nextLine());
        cache.put(distritoElectoral.getCodigo(), distritoElectoral);
      }
    }
  }

  public DistritoElectoral get(String code) {
    return cache.get(code);
  }
}
