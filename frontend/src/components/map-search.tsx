import { $api } from "@/api/client";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useCurrentPosition } from "@/hooks/use-current-position";
import { cn } from "@/lib/utils";
import { OpenStreetMapProvider } from "leaflet-geosearch";
import { LoaderCircle, LocateFixed, SlidersHorizontal } from "lucide-react";
import { useState, type Dispatch, type SetStateAction } from "react";
import { useMap } from "react-leaflet";
import { toast } from "sonner";
import { Button } from "./ui/button";
import { Checkbox } from "./ui/checkbox";
import { Input } from "./ui/input";
import { Label } from "./ui/label";

interface Point {
  lat: number;
  lon: number;
}
function validateCoordinates(input: string) {
  const split = input.split(",");
  if (split.length != 2) {
    return null;
  }

  const lat = parseFloat(split[0].trim());
  const lon = parseFloat(split[1].trim());

  if (isNaN(lat) || isNaN(lon)) {
    return null;
  }

  return { lat: lat, lon: lon };
}

async function translateAddress(
  input: string,
  provider: OpenStreetMapProvider,
  setIsGeoLoading: React.Dispatch<React.SetStateAction<boolean>>,
) {
  setIsGeoLoading(true);
  const result = await provider.search({ query: input });
  setIsGeoLoading(false);
  return { lat: result[0].y, lon: result[0].x };
}

interface MapSearchProps extends Point {
  setHighlightedLocation: Dispatch<SetStateAction<number | null>>;
  showActive: boolean;
  setShowActive: Dispatch<SetStateAction<boolean>>;
}

export default function MapSearch({
  lat,
  lon,
  setHighlightedLocation,
  showActive: available,
  setShowActive: setAvailable,
}: MapSearchProps) {
  const map = useMap();
  const provider = new OpenStreetMapProvider();
  const [input, setInput] = useState("");
  const [isGeoLoading, setIsGeoLoading] = useState(false);
  const { requestUserLocation } = useCurrentPosition();

  const { mutate, isPending } = $api.useMutation(
    "get",
    "/api/v1/locations/closest",
    {
      onError() {
        toast.error("An error occured while getting the closest station");
      },
      onSuccess(data) {
        map.flyTo([data.lat, data.lon], 17, { duration: 1 });
        setIsGeoLoading(false);
        setHighlightedLocation(data.id);
      },
    },
  );

  const findClosest = async () => {
    let p = { lat, lon };
    if (input === "") {
      mutate({ params: { query: { lat: p.lat, lon: p.lon } } });
      return;
    }

    const coordinates = validateCoordinates(input);
    if (coordinates !== null) {
      p = { ...coordinates };
    } else {
      try {
        p = await translateAddress(input, provider, setIsGeoLoading);
      } catch {
        toast.error("An error occured while getting the address");
      }
    }

    mutate({ params: { query: p } });
  };

  const updatePos = async () => {
    let p = { lat, lon };

    if (input === "") {
      const coords = { lat: p.lat, lng: p.lon } as const;
      if (-90 > coords.lat || coords.lat > 90) return;
      if (-180 > coords.lng || coords.lng > 180) return;
      map.setView(coords, 15);

      return;
    }

    const coordinates = validateCoordinates(input);

    if (coordinates !== null) {
      p = { ...coordinates };
    } else {
      try {
        p = await translateAddress(input, provider, setIsGeoLoading);
      } catch {
        toast.error("An error occured while getting the address");
        return;
      }
    }
    const coords = { lat: p.lat, lng: p.lon };
    if (-90 > coords.lat || coords.lat > 90) return;
    if (-180 > coords.lng || coords.lng > 180) return;

    map.setView(coords, 15);
  };

  return (
    <>
      <div className="absolute top-4 right-4 z-500 space-y-2">
        <div className="flex gap-2">
          <Tooltip>
            <TooltipTrigger asChild>
              <Button
                type="button"
                onClick={() => {
                  requestUserLocation();
                  map.setView({ lat, lng: lon });
                }}
              >
                <LocateFixed />
                <span className="sr-only">Current Locattion</span>
              </Button>
            </TooltipTrigger>
            <TooltipContent>Current Location</TooltipContent>
          </Tooltip>
          <Input
            name="address"
            placeholder="Use Current Location"
            data-test-id="address-input"
            className="bg-secondary"
            onChange={(e) => setInput(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-2">
          <Button
            className="flex-grow"
            onClick={findClosest}
            data-test-id="find-closest"
          >
            Find closest
          </Button>
          <Button
            className="flex-grow"
            onClick={updatePos}
            data-test-id="coords-button"
          >
            Go To
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button>
                <SlidersHorizontal />
                <span className="sr-only">Filters</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="mr-5 p-4">
              <div className="flex gap-4">
                <Checkbox
                  id="active"
                  checked={available}
                  onCheckedChange={() => {
                    setAvailable((a) => !a);
                  }}
                />
                <Tooltip>
                  <TooltipTrigger>
                    <Label htmlFor="active">Active</Label>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p>
                      Only shows the stations that have chargers
                      active/available to use
                    </p>
                  </TooltipContent>
                </Tooltip>
              </div>
            </DropdownMenuContent>
          </DropdownMenu>
          <LoaderCircle
            className={cn("animate-spin", {
              "opacity-0": !(isGeoLoading || isPending),
            })}
            data-test-id="location-loading"
          />
        </div>
      </div>
    </>
  );
}
