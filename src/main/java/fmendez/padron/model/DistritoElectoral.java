package fmendez.padron.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistritoElectoral implements Serializable {

  private final String codigo;
  private final String provincia;
  private final String canton;
  private final String distrito;

}
