package com.example.meteo.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/*
Classe model che si occupa di modellare gli attributi di una rilevazione (utili alla logica del nostro applicativo)
in una tabella del DB.
 */


@Entity
@Table(name = "meteo")
public class Meteo {

    @Id
    @GeneratedValue
    private long id;    //Identificativo autoincrementale

    @Column(nullable = false)
    private String citta;   //Nome della citta
    @Column(nullable = false)
    private float temperatura;  //Temperatura rilevata
    @Column(nullable = false)
    private String unitaMisura; //Unita di misura
    @Column(nullable = false)
    private LocalDateTime timestamp;    //Data e ora di rilevazione


    public Meteo(String citta, float temperatura, String unitaMisura, LocalDateTime timestamp) {
        this.citta = citta;
        this.temperatura = temperatura;
        this.unitaMisura = unitaMisura;
        this.timestamp = timestamp;
    }

    public Meteo() {
    }

    public long getId() {
        return id;
    }

    public String getCitta() {
        return citta;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
