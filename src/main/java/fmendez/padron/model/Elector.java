package fmendez.padron.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
public class Elector implements Serializable {

  private final String cedula;
  private final DistritoElectoral distritoElectoral;
  private final Date fechaCaducidad;
  private final String juntaReceptora;
  private final String nombre;
  private final String primerApellido;
  private final String segundoApellido;
  private final PartidoRegistro partidoRegistro;

  @Builder
  public Elector(
    String cedula,
    DistritoElectoral distritoElectoral,
    Date fechaCaducidad,
    String juntaReceptora,
    String nombre,
    String primerApellido,
    String segundoApellido
  ) {
    this.cedula = cedula;
    this.distritoElectoral = distritoElectoral;
    this.fechaCaducidad = fechaCaducidad;
    this.juntaReceptora = juntaReceptora;
    this.nombre = nombre;
    this.primerApellido = primerApellido;
    this.segundoApellido = segundoApellido;
    this.partidoRegistro = PartidoRegistro.fromCodigo(Integer.parseInt(cedula.substring(0,1)));
  }

  public String getNombreCompleto() {
    return String.join(" ", nombre, primerApellido, segundoApellido);
  }

}
