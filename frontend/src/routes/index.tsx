import { Drawer, DrawerContent, DrawerHeader } from "@/components/ui/drawer";
import { useCurrentPosition } from "@/hooks/use-current-position";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState, type ComponentRef } from "react";
import { MapContainer, Marker, TileLayer, Tooltip } from "react-leaflet";

export const Route = createFileRoute("/")({
  component: RouteComponent,
});

interface ChargingStation {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  availability: "AVAILABLE" | "UNAVAILABLE";
  power: number;
}

const stations: ChargingStation[] = [
  {
    id: 1,
    latitude: 40.637653,
    longitude: -8.660288,
    power: 200,
    availability: "AVAILABLE",
    address: "somewhere",
    name: "station",
  },
];

function RouteComponent() {
  const { location, locationAvailable } = useCurrentPosition();
  const [station, setStation] = useState<ChargingStation | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const mapRef = useRef<ComponentRef<typeof MapContainer>>(null);

  useEffect(() => {
    const handleClick = () => {
      setStation(null);
    };
    mapRef.current?.on("click", handleClick);
    return () => {
      mapRef.current?.off("click", handleClick);
    };
  }, [mapRef.current]);

  if (location === null && locationAvailable) return null;

  return (
    <main className="flex h-screen flex-col md:h-auto md:flex-row">
      <aside className="hidden w-96 md:block">
        <h1>NikoGrid</h1>
        <p>Browse stations</p>
        {station?.name}
      </aside>
      <div className="h-screen grow overflow-hidden">
        <MapContainer
          center={[
            location?.coords.latitude ?? 0,
            location?.coords.longitude ?? 0,
          ]}
          zoom={8}
          ref={mapRef}
          scrollWheelZoom={true}
          className="h-full w-full"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {stations.map((station) => (
            <Marker
              key={station.id}
              position={{ lat: station.latitude, lng: station.longitude }}
              eventHandlers={{
                click: () => {
                  setStation(station);
                  setDrawerOpen(true);
                },
              }}
            >
              <Tooltip>
                <p className="text-lg font-semibold">{station.name}</p>
                <p className="text-sm">{station.address}</p>
                <ul className="list-inside list-disc">
                  <li>Power: {station.power}</li>
                  <li>Availability: {station.availability}</li>
                </ul>
              </Tooltip>
            </Marker>
          ))}
        </MapContainer>
      </div>
      <aside className="h-20 md:hidden">
        hi:
        {station?.name}
      </aside>

      <Drawer open={drawerOpen} onOpenChange={setDrawerOpen}>
        <DrawerContent>
          <DrawerHeader>{station?.name}</DrawerHeader>
        </DrawerContent>
      </Drawer>
    </main>
  );
}
