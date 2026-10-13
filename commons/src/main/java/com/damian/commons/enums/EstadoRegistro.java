package com.damian.commons.enums;

import com.damian.commons.exceptions.DatoInvalidoException;
import com.damian.commons.utils.StringCustomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EstadoRegistro {

    ACTIVO("Activo"),
    ELIMINADO("Eliminado");

    private final String descripcion;

    public static EstadoRegistro obtenerEstadoRegistroPorDescripcion(String descripcion){
        StringCustomUtils.validarNoVacio(descripcion, "La descripción es requerida");
        String descripcionNormalizada = StringCustomUtils.quitarAcentos(descripcion);

        for (EstadoRegistro categoria : values()){
            if (StringCustomUtils.quitarAcentos(categoria.descripcion).equalsIgnoreCase(descripcionNormalizada))
                return categoria;
        }

        throw new DatoInvalidoException("No existe un estado registro con la descripción: " + descripcion);
    }
}
