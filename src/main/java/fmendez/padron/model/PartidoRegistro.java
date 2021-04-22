package fmendez.padron.model;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Partido o lugar de inscripción de la persona.
 */
public enum PartidoRegistro {

  SAN_JOSE(1),
  ALAJUELA(2),
  CARTAGO(3),
  HEREDIA(4),
  GUANACASTE(5),
  PUNTARENAS(6),
  LIMON(7),
  NATURALIZADOS(8),
  PARTIDO_ESPECIAL_DE_NACIMIENTOS(9);

  private Integer codigo;

  private static final Map<Integer,PartidoRegistro> CODE_MAP = Arrays.stream(PartidoRegistro.values())
    .map( p -> new AbstractMap.SimpleImmutableEntry<>(p.codigo, p))
    .collect(Collectors.collectingAndThen(
      Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue),
      Collections::unmodifiableMap));

  PartidoRegistro(Integer codigo){
    this.codigo = codigo;
  }

  public static PartidoRegistro fromCodigo(Integer codigo) {
    return CODE_MAP.get(codigo);
  }

}
