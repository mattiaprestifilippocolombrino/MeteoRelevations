package com.example.meteo.controller;

import com.example.meteo.model.Meteo;
import com.example.meteo.service.MeteoService;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import java.util.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
Classe che implementa il controller REST. Intercetta le chiamate effettuate tramite le API REST dall'utente,
mappando ogni chiamata ad un metodo java. Questi metodi chiamano una funzione del service che si occupa di
eseguire la logica di business per la relativa operazione.

 */
@RestController
@RequestMapping("/api/meteo")
public class MeteoController {
    private final MeteoService meteoService;

    public MeteoController(MeteoService meteoService) {
        this.meteoService = meteoService;
    }

    /*
    Endpoint POST che permette di effettuare una rilevazione per una singola citta, passata in input
     */
    @PostMapping("rileva/{citta}")
    public Meteo registerRilevazioneCitta(@PathVariable String citta) {
        return meteoService.registerRilevazioneCitta(citta);
    }

    /*
    Endpoint POST che permette di effettuare una rilevazione per tutte le citta gestite dall'applicativo (nel nostro caso 5).
     */
    @PostMapping("rilevaAll")
    public List<Meteo> registerAllCitta(){
        return meteoService.registerAllCitta();
    }

    /*
    Endpoint POST che permette di effettuare rilevazioni meteo per le ultime 5h, per tutte le citta gestite dall'applicativo.
     */
    @PostMapping("rilevaLastHourAll")
    public List<Meteo> registerLastHoursAllCitta(){
        return meteoService.registerLastHoursAllCitta();
    }


    /*
    Endpoint GET che permette di interrogare l'applicativo riguardo la temperatura di una singola citta, passata in input
     */
    @GetMapping("/temperatura/{citta}")
    public Map<String, Object> getTemperatura(@PathVariable String citta) {
        float temperatura = meteoService.getTemperatura(citta);
        return Map.of(
                "citta", citta,
                "temperatura", temperatura,
                "unitaMisura", "°C"
        );
    }

    /*
    Endpoint GET che permette di interrogare l'applicativo riguardo la temperatura media di una singola citta, passata in input
     */
    @GetMapping("/temperaturaMedia/{citta}")
    public Map<String, Object> getMediaTemperatura(@PathVariable String citta) {
        float media = meteoService.getMediaTemperatura(citta);
        return Map.of(
                "citta", citta,
                "temperaturaMedia", media,
                "unitaMisura", "°C"
        );
    }

    /*
    Endpoint GET che permette di interrogare l'applicativo riguardo la temperatura media di ogni singola citta gestita dall'applicativo
     */
    @GetMapping("/temperaturaMediaAll")
    public Map<String, Object> getMediaTemperaturaAll() {
        Map<String, Float> medie = meteoService.getMediaTemperaturaAll();
        List<Map<String, Object>> risultati = new ArrayList<>();

        for (Map.Entry<String, Float> entry : medie.entrySet()) {
            Map<String, Object> risultato = Map.of(
                    "citta", entry.getKey(),
                    "temperaturaMedia", entry.getValue(),
                    "unitaMisura", "°C"
            );
            risultati.add(risultato);
        }
        return Map.of("medie", risultati);
    }

    // Handler che gestiscono gli stati di errore, restitutendo codice di errore e relativo messaggio
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(Exception e) {
        return Map.of("errore", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(Exception e) {
        return Map.of("errore", e.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleMeteoApiError(RestClientException e) {
        return Map.of(
                "errore", "Servizio Open-Meteo temporaneamente non disponibile"
        );
    }
}
