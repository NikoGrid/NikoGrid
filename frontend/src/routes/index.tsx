import StationDetails from "@/components/station-details";
import Stations from "@/components/stations";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
  const [lat, setLat] = useState("");
  const [lon, setLon] = useState("");

  const mapRef = useRef<ComponentRef<typeof MapContainer>>(null);

  const updatePos = () => {
    const coords = { lat: parseFloat(lat), lng: parseFloat(lon) } as const;
    console.log(coords);

    if (-90 > coords.lat || coords.lat > 90) return;
    if (-180 > coords.lng || coords.lng > 180) return;

    mapRef.current?.setView(coords, 9);
  };

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
        className="z-0 grow"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Stations setId={setStationId} />
      </MapContainer>
      <StationDetails stationId={stationId} setStationId={setStationId} />
      <section className="absolute top-4 right-4 flex flex-col gap-2 md:flex-row">
        <Input
          data-test-id="lat-input"
          className="w-32 bg-white"
          placeholder="Latitude"
          onChange={(e) => setLat(e.target.value)}
        />
        <Input
          data-test-id="lon-input"
          className="w-32 bg-white"
          placeholder="Longitude"
          onChange={(e) => setLon(e.target.value)}
        />
        <Button data-test-id="coords-button" type="button" onClick={updatePos}>
          Go to
        </Button>
      </section>
    </main>
  );
}
