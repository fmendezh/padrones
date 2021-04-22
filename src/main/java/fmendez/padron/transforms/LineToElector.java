package fmendez.padron.transforms;

import java.text.SimpleDateFormat;

import fmendez.padron.dict.DistritosElectoralesDictionary;
import fmendez.padron.model.Elector;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.beam.sdk.transforms.SerializableFunction;

@Data
@RequiredArgsConstructor
public class LineToElector implements SerializableFunction<String, Elector> {

  private final DistritosElectoralesDictionary distritosElectoralesDictionary;

  //Date parsing
  private static final String DATE_FMT = "YYYYMMdd";
  private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FMT);

  @Override
  @SneakyThrows
  public Elector apply(String line) {
    String[] fields = line.split(",");
    return Elector.builder()
            .cedula(fields[0])
            .distritoElectoral(distritosElectoralesDictionary.get(fields[1]))
            .fechaCaducidad(dateFormat.parse(fields[3]))
            .juntaReceptora(fields[4])
            .nombre(fields[5].trim())
            .primerApellido(fields[6].trim())
            .segundoApellido(fields[7].trim())
            .build();
  }
}
