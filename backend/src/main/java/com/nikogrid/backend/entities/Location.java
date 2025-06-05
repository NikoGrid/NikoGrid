package com.nikogrid.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.Set;

@Data
@Entity
@Table(name = "locations")
@ToString(exclude = "chargers")
@EqualsAndHashCode(exclude = "chargers")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private float lat;

    @Column(nullable = false)
    private float lon;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(columnDefinition = "GEOMETRY", insertable = false, updatable = false)
    private Point<G2D> geom;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private Set<Charger> chargers;
}
