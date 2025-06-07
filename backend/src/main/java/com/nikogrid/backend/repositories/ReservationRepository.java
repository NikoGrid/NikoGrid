package com.nikogrid.backend.repositories;

import com.nikogrid.backend.entities.Reservation;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    @NativeQuery(
            """
                    SELECT *
                    FROM reservation r
                    WHERE
                    user_id = ?1
                    ORDER BY
                        CASE WHEN r.starts_at >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END,
                        CASE WHEN r.starts_at >= CURRENT_TIMESTAMP THEN r.starts_at END ASC,
                        CASE WHEN r.starts_at < CURRENT_TIMESTAMP THEN r.starts_at END DESC
                    """)
    List<Reservation> getUserReservations(UUID userId);

    Optional<Reservation> getReservationById(long reservationId);
}
