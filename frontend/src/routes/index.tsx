import MapSearch from "@/components/map-search";
import StationDetails from "@/components/station-details";
import Stations from "@/components/stations";
import { useCurrentPosition } from "@/hooks/use-current-position";
import { createFileRoute } from "@tanstack/react-router";
import { useRef, useState, type ComponentRef } from "react";
import { MapContainer, TileLayer } from "react-leaflet";

export const Route = createFileRoute("/")({
  component: RouteComponent,
});

function RouteComponent() {
  const { location, locationAvailable } = useCurrentPosition();
  const [stationId, setStationId] = useState<number | null>(null);

  const mapRef = useRef<ComponentRef<typeof MapContainer>>(null);

  if (location === null && locationAvailable) return null;

  return (
    <main className="relative flex grow flex-col">
      <MapContainer
        ref={mapRef}
        center={[
          location?.coords.latitude ?? 0,
          location?.coords.longitude ?? 0,
        ]}
        zoom={13}
        scrollWheelZoom={true}
        className="relative z-0 grow"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Stations setId={setStationId} />

        <MapSearch
          lat={location?.coords.latitude ?? 0}
          lon={location?.coords.longitude ?? 0}
        />
      </MapContainer>
      <StationDetails stationId={stationId} setStationId={setStationId} />
    </main>
  );
}
