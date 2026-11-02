package com.example.meteo.service;

import com.example.meteo.model.Meteo;
import com.example.meteo.repository.MeteoRepository;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

/*
Classe service chiamata dagli endpoint del controller, che si occupa di eseguire le operazioni di business
dell'applicativo.
 */
@Service
public class MeteoService {
    private final MeteoRepository meteoRepository;
    private final RestClient restClient;

    /*
    Mapping costante che associa ad ogni nome di citta le relative coordinate. Viene utilizzato per semplificare la demo
    di uso per 5 citta italiane. In un caso di produzione, passare al controller direttamente le coordinate, supportando qualsiasi citta,
    oppure utilizzare un servizio API che dato il nome di città restituisca le sue coordinate.
     */
    private static final Map<String, double[]> CITTA = Map.of(
            "pisa", new double[]{43.7228, 10.4017},
            "roma", new double[]{41.9028, 12.4964},
            "milano", new double[]{45.4642, 9.1900},
            "napoli", new double[]{40.8518, 14.2681},
            "torino", new double[]{45.0703, 7.6869}
    );

    public MeteoService(MeteoRepository meteoRepository, RestClient.Builder builder) {
        this.meteoRepository = meteoRepository;
        this.restClient = builder.build();
    }

    /*
    Funzione che si occupa di effettuare una rilevazione meteo relativa ad una singola citta,
    effettuando una chiamata GET alle API di open-meteo.com. Dopo aver estratto i dati necessari,
    modella la rilevazione nella classe Meteo e la salva nel db, chiamando la funzione save() del repository.
     */
    public Meteo registerRilevazioneCitta(String citta) {
        if (citta == null || citta.isBlank()) {
            throw new IllegalArgumentException("Città non valida");
        }
        citta = citta.trim().toLowerCase(Locale.ROOT);

        double[] coordinate = CITTA.get(citta);
        if (coordinate == null) {
            throw new IllegalArgumentException("Città non supportata: " + citta);
        }
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude={lat}"
                + "&longitude={lon}"
                + "&current_weather=true"
                + "&temperature_unit=celsius"
                + "&timezone=GMT";
        Map<?, ?> risposta = restClient.get()
                .uri(url, coordinate[0], coordinate[1])
                .retrieve()
                .body(Map.class);
        if (risposta == null) {
            throw new IllegalStateException("Risposta API vuota");
        }


        Map<?, ?> meteo = (Map<?, ?>) risposta.get("current_weather");
        Map<?, ?> unita = (Map<?, ?>) risposta.get("current_weather_units");

        if (meteo == null || unita == null) {
            throw new IllegalStateException("Dati meteo non disponibili");
        }

        float temperatura = ((Number) meteo.get("temperature")).floatValue();

        String unitaMisura = (String) unita.get("temperature");
        String date = (String) meteo.get("time");
        LocalDateTime timestamp = LocalDateTime.parse(date);

        Meteo rilevazione = new Meteo(citta, temperatura, unitaMisura, timestamp);
        return meteoRepository.save(rilevazione);
    }


    // Funzione che registra una rilevazione per ciascuna delle cinque città, utilizzando per ognuna la funzione registerRilevazioneCitta()
    public List<Meteo> registerAllCitta() {
        List<Meteo> rilevazioni = new ArrayList<>();
        for (String citta : CITTA.keySet()) {
            Meteo rilevazione = registerRilevazioneCitta(citta);
            rilevazioni.add(rilevazione);
        }
        return rilevazioni;
    }

    /*
     Funzione che registra le rilevazioni delle ultime 5h per ciascuna delle cinque città.
     In modo analogo alle funzioni precedenti, effettua una chiamata GET alle API di open-meteo.com. Dopo aver estratto i dati necessari,
    modella la rilevazione nella classe Meteo e la salva nel db, chiamando la funzione save() del repository.
     */
    public List<Meteo> registerLastHoursAllCitta(){
        List<Meteo> rilevazioni = new ArrayList<>();
        for(String citta:CITTA.keySet()){
            double[] coordinate = CITTA.get(citta);
            String url = "https://api.open-meteo.com/v1/forecast"
                    + "?latitude={lat}"
                    + "&longitude={lon}"
                    + "&past_hours=5"
                    + "&forecast_hours=0"
                    + "&hourly=temperature_2m"
                    + "&temperature_unit=celsius"
                    + "&timezone=GMT";
            Map<?,?> risposta = restClient.get().uri(url,coordinate[0], coordinate[1]).retrieve().body(Map.class);
            if (risposta == null) {
                throw new IllegalStateException("Risposta API vuota");
            }
            Map<?,?> hourly = (Map<?, ?>) risposta.get("hourly");
            if (hourly == null) {
                throw new IllegalStateException("Dati meteo non disponibili");
            }
            List<String> orari = (List<String>) hourly.get("time");
            List<Number> temperature = (List<Number>) hourly.get("temperature_2m");
            if (orari == null || temperature == null
                    || orari.size() < 5 || temperature.size() < 5) {
                throw new IllegalStateException("Rilevazioni insufficienti");
            }
            for(int i = 0; i<5; i++){
                Meteo rilevazione = new Meteo(
                        citta,
                        temperature.get(i).floatValue(),
                        "°C",
                        LocalDateTime.parse(orari.get(i))
                );
                rilevazioni.add(meteoRepository.save(rilevazione));
            }
        }
        return rilevazioni;
    }


    /*
    Funzione che restituisce l'ultima rilevazione di temperatura per una citta.
     */
    public float getTemperatura(String citta) {
        Optional<Meteo> rilevazione = meteoRepository.findUltimaRilevazioneCitta(citta);
        if (rilevazione.isEmpty()) {
            throw new NoSuchElementException(
                    "Nessuna rilevazione per " + citta);
        }
        return rilevazione.get().getTemperatura();
    }

    /*
    Funzione che restituisce la media della temperatura per una citta.
     */
    public float getMediaTemperatura(String citta) {
        List<Meteo> rilevazioni = meteoRepository.findAllRilevazioniCitta(citta);
        if (rilevazioni.isEmpty()) {
            throw new NoSuchElementException(
                    "Nessuna rilevazione per " + citta);
        }
        float somma = 0;
        for (Meteo m : rilevazioni) {
            somma += m.getTemperatura();
        }
        return somma / rilevazioni.size();
    }

    /*
    Funzione che restituisce la media della temperatura per tutte le citta gestite dall'applicativo.
     */
    public Map<String, Float> getMediaTemperaturaAll() {
        Map<String, Float> medie = new HashMap<>();
        for (String citta : CITTA.keySet()) {
            float media = getMediaTemperatura(citta);
            medie.put(citta, media);
        }
        return medie;
    }


}
