package com.example.meteo.repository;
import com.example.meteo.model.Meteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
Classe repository utilizzata dall'applicativo per interfacciarsi al db ed eseguire query SQL.
 */

public interface MeteoRepository extends JpaRepository<Meteo, Long> {
    // Query che recupera l'ultima rilevazione di una città
    @Query(value = """
            SELECT * FROM meteo WHERE LOWER(citta) = LOWER(:citta) ORDER BY timestamp DESC, id DESC
            LIMIT 1 """, nativeQuery = true)
    Optional<Meteo> findUltimaRilevazioneCitta(@Param("citta") String citta);

    // Query che recupera tutte le rilevazioni di una città
    @Query(value = """ 
        SELECT * FROM meteo WHERE LOWER(citta) = LOWER(:citta)
        ORDER BY timestamp DESC, id DESC """, nativeQuery = true)
    List<Meteo> findAllRilevazioniCitta(@Param("citta") String citta);


}
