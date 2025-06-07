import MapSearch from "@/components/map-search";
import StationDetails from "@/components/station-details";
import Stations from "@/components/stations";
import { useCurrentPosition } from "@/hooks/use-current-position";
import { createLazyFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState, type ComponentRef } from "react";
import { MapContainer, TileLayer } from "react-leaflet";

export const Route = createLazyFileRoute("/")({
  component: RouteComponent,
});

const defaultCoords = {
  lat: 40.44686847220579,
  lng: -8.396856383968853,
};

function RouteComponent() {
  const [container, setContainer] = useState<HTMLElement | null>(null);

  const { location } = useCurrentPosition();
  const [stationId, setStationId] = useState<number | null>(null);
  const [higlightedId, setHighlightedId] = useState<number | null>(null);

  const mapRef = useRef<ComponentRef<typeof MapContainer>>(null);

  useEffect(() => {
    if (location === null) return;
    mapRef.current?.setView(location);
  }, [location]);

  return (
    <div ref={setContainer} className="flex grow">
      <StationDetails
        container={container}
        stationId={stationId}
        setStationId={setStationId}
      />
      <main className="relative flex grow flex-col" data-test-id="home-page">
        <span className="sr-only">Map</span>
        <MapContainer
          ref={mapRef}
          center={defaultCoords}
          zoom={13}
          scrollWheelZoom={true}
          className="relative z-0 grow"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <Stations setId={setStationId} highlightedId={higlightedId} />

          <MapSearch
            lat={location?.lat ?? defaultCoords.lat}
            lon={location?.lng ?? defaultCoords.lng}
            setHighlightedLocation={setHighlightedId}
          />
        </MapContainer>
      </main>
    </div>
  );
}
