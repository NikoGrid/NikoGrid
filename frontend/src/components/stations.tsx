import { $api } from "@/api/client";
import type { InterestPoint } from "@/api/schemas";
import { cn } from "@/lib/utils";
import { divIcon } from "leaflet";
import { Zap } from "lucide-react";
import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type Dispatch,
  type SetStateAction,
} from "react";
import ReactDOMServer from "react-dom/server";
import { Marker, Tooltip, useMap, useMapEvent } from "react-leaflet";

function useDebouncedCallback<T extends (...args: Parameters<T>) => void>(
  callback: T,
  delay: number,
): (...args: Parameters<T>) => void {
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const debouncedFunction = useCallback(
    (...args: Parameters<T>) => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }

      timeoutRef.current = setTimeout(() => {
        callback(...args);
      }, delay);
    },
    [callback, delay],
  );

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  return debouncedFunction;
}

interface StationsProps {
  setId: Dispatch<SetStateAction<number | null>>;
  highlightedId?: number | null;
}

export default function Stations({
  setId,
  highlightedId = null,
}: StationsProps) {
  const map = useMap();

  const [ips, setIps] = useState<InterestPoint[]>([]);
  const { mutate, data } = $api.useMutation("get", "/api/v1/locations/nearby");

  useEffect(() => {
    if (data === undefined) return;
    setIps(data);
  }, [data]);

  const getLocations = useCallback(() => {
    const bounds = map.getBounds();
    mutate({
      params: {
        query: {
          w: bounds.getWest(),
          n: bounds.getNorth(),
          e: bounds.getEast(),
          s: bounds.getSouth(),
          z: map.getZoom(),
        },
      },
    });
  }, [map, mutate]);
  const debouncedGetLocations = useDebouncedCallback(getLocations, 300);

  useMapEvent("move", debouncedGetLocations);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(getLocations, []);

  return (
    <>
      {ips.map((ip, idx) => {
        let id: string;
        switch (ip.t) {
          case "C":
            id = `c-${idx}`;
            return (
              <StationMarker
                key={id}
                interestPoint={ip}
                dataTestId={id}
                handleClick={() => {
                  map.flyTo({ lat: ip.lat, lng: ip.lon }, map.getZoom() + 2);
                  getLocations();
                }}
              />
            );
          case "L":
            id = `l-${idx}`;
            return (
              <StationMarker
                key={id}
                interestPoint={ip}
                dataTestId={id}
                handleClick={() => {
                  setId(ip.id);
                }}
                highlighted={ip.id === highlightedId}
              />
            );
        }
      })}
    </>
  );
}

interface StationMarkerProps {
  interestPoint: InterestPoint;
  handleClick(): void;
  dataTestId?: string;
  highlighted?: boolean;
}

const iconSize = 40 as const;

function StationMarker({
  interestPoint,
  handleClick,
  dataTestId,
  highlighted = false,
}: StationMarkerProps) {
  const { fillColor, textColor, outlineColor } =
    interestPoint.t === "C"
      ? {
          fillColor: "fill-fuchsia-700",
          textColor: "text-fuchsia-700",
          outlineColor: "outline-fuchsia-700",
        }
      : {
          fillColor: highlighted ? "fill-red-700" : "fill-teal-700",
          textColor: highlighted ? "text-red-700" : "text-teal-700",
          outlineColor: highlighted ? "outline-red-700" : "outline-teal-700",
        };
  return (
    <Marker
      position={{ lat: interestPoint.lat, lng: interestPoint.lon }}
      eventHandlers={{
        click: handleClick,
      }}
      icon={divIcon({
        html: ReactDOMServer.renderToString(
          <div
            className={cn(
              "h-10 w-10 rounded-full bg-white p-2 outline-3",
              outlineColor,
            )}
            data-test-id={dataTestId}
            data-test-group={interestPoint.t === "C" ? "cluster" : "location"}
            data-test-name={
              interestPoint.t === "L" ? interestPoint.n : undefined
            }
          >
            <Zap className={cn("h-6 w-6", textColor, fillColor)} />
          </div>,
        ),
        className: "bg-none",
        iconAnchor: [iconSize / 2, iconSize / 2],
        tooltipAnchor: [0, iconSize / 2],
      })}
    >
      <Tooltip permanent={interestPoint.t === "C"} direction="bottom">
        <p>
          {interestPoint.t === "L"
            ? interestPoint.n
            : `${interestPoint.numPoints} Stations`}
        </p>
      </Tooltip>
    </Marker>
  );
}
