package com.damian.pacientes.entity;

import com.damian.commons.enums.EstadoRegistro;
import com.damian.commons.exceptions.ConflictoException;
import com.damian.commons.exceptions.DatoInvalidoException;
import com.damian.commons.utils.StringCustomUtils;
import com.damian.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "PACIENTES")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PACIENTE")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private Short edad;

    @Column(name = "PESO", nullable = false)
    private Double peso;

    @Column(name = "ESTATURA", nullable = false)
    private Double estatura;

    @Column(name = "IMC", nullable = false)
    private Double imc;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "NUM_EXPEDIENTE", nullable = false, length = 20)
    private String numExpediente;

    @Column(name = "TELEFONO", nullable = false, length = 10)
    private String telefono;

    @Column(name = "DIRECCION", nullable = false, length = 150)
    private String direccion;

    @Column(name = "ESTADO_REGISTRO", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EstadoRegistro estadoRegistro;

    private static void validarDatos(
            String nombre, String apellidoPaterno, String apellidoMaterno,
            Short edad, Double peso, Double estatura, String email,
            String telefono, String direccion) {

        StringCustomUtils.validarTamanio(nombre, 1, 50,
                "El nombre es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(apellidoPaterno, 1, 50,
                "El apellido paterno es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(apellidoMaterno, 1, 50,
                "El apellido materno es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(email, 1, 100,
                "El email es requerido y debe tener entre 1 y 100 caracteres");

        StringCustomUtils.validarTamanio(telefono, 10, 10,
                "El teléfono es requerido y debe tener exactamente 10 caracteres");

        StringCustomUtils.validarTamanio(direccion, 1, 150,
                "La dirección es requerida y debe tener entre 1 y 150 caracteres");

        ValoresNumericosUtils.validarNumeroPositivo(edad, "La edad es requerida y debe ser positiva");
        ValoresNumericosUtils.validarNumeroPositivo(peso, "El peso es requerido y debe ser positivo");
        ValoresNumericosUtils.validarNumeroPositivo(estatura, "La estatura es requerida y debe ser positiva");
    }

    private static Double calcularImc(Double peso, Double estatura){
        if (peso <= 0.0 || estatura <= 0.0)
            throw new DatoInvalidoException("El peso y la estatura deben ser positivos");

        return peso / (estatura * estatura);
    }

    private static String generarNoExpediente(String telefono){
        if (telefono == null || telefono.isEmpty()) {
            throw new DatoInvalidoException("El teléfono no puede estar vacío");
        }

        StringBuilder expediente = new StringBuilder();
        for (int i = 0; i < telefono.length(); i++) {
            expediente.append(telefono.charAt(i)).append('X');
        }

        return expediente.toString();
    }

    public void eliminar(){
        if (this.estadoRegistro == EstadoRegistro.ELIMINADO)
            throw new ConflictoException("El paciente ya está eliminado");

        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public void actualizar(String nombre, String apellidoPaterno, String apellidoMaterno,
                           Short edad, Double peso, Double estatura, String email,
                           String telefono, String direccion){

        validarDatos(nombre, apellidoPaterno, apellidoMaterno, edad, peso, estatura, email, telefono, direccion);

        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.edad = edad;
        this.peso = peso;
        this.estatura = estatura;
        this.imc = calcularImc(peso, estatura);
        this.email = email.trim();
        this.numExpediente = generarNoExpediente(telefono);
        this.telefono = telefono.trim();
        this.direccion = direccion;
        this.estadoRegistro = EstadoRegistro.ACTIVO;
    }

    public static Paciente crear(String nombre, String apellidoPaterno, String apellidoMaterno,
                                 Short edad, Double peso, Double estatura, String email,
                                 String telefono, String direccion){

        validarDatos(nombre, apellidoPaterno, apellidoMaterno, edad, peso, estatura, email, telefono, direccion);

        return Paciente.builder()
                .nombre(nombre.trim())
                .apellidoPaterno(apellidoPaterno.trim())
                .apellidoMaterno(apellidoMaterno.trim())
                .edad(edad)
                .peso(peso)
                .estatura(estatura)
                .imc(calcularImc(peso, estatura))
                .email(email)
                .numExpediente(generarNoExpediente(telefono))
                .telefono(telefono)
                .direccion(direccion)
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }
}
