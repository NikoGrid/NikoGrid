import { $api } from "@/api/client";
import type { InterestPoint } from "@/api/schemas";
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
}

const iconSize = 40 as const;

export default function Stations({ setId }: StationsProps) {
  const map = useMap();

  const { mutate, data } = $api.useMutation("get", "/api/v1/locations/nearby");

  const [ips, setIps] = useState<InterestPoint[]>([]);
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
  useEffect(getLocations, []);

  return (
    <>
      {ips.map((ip, idx) => {
        switch (ip.t) {
          case "C":
            return (
              <Marker
                key={`c-${idx}`}
                position={{ lat: ip.lat, lng: ip.lon }}
                eventHandlers={{
                  click: () => {
                    map.flyTo({ lat: ip.lat, lng: ip.lon }, map.getZoom() + 2);
                    getLocations();
                  },
                }}
                icon={divIcon({
                  html: ReactDOMServer.renderToString(
                    <div
                      className="h-10 w-10 rounded-full bg-white p-2 outline-3 outline-fuchsia-700"
                      data-test-id={`c-${idx}`}
                      data-test-group="cluster"
                    >
                      <Zap className="h-6 w-6 fill-fuchsia-700 text-fuchsia-700" />
                    </div>,
                  ),
                  className: "bg-none",
                  iconAnchor: [iconSize / 2, iconSize / 2],
                  tooltipAnchor: [0, iconSize / 2],
                })}
              >
                <Tooltip permanent direction="bottom">
                  <p>{ip.numPoints} Stations</p>
                </Tooltip>
              </Marker>
            );
          case "L":
            return (
              <Marker
                key={`l-${ip.id}`}
                position={{ lat: ip.lat, lng: ip.lon }}
                eventHandlers={{
                  click: () => {
                    setId(ip.id);
                  },
                }}
                icon={divIcon({
                  html: ReactDOMServer.renderToString(
                    <div
                      className="h-10 w-10 rounded-full bg-white p-2 outline-3 outline-teal-700"
                      data-test-id={`l-${ip.id}`}
                      data-test-group="location"
                      data-test-name={ip.n}
                    >
                      <Zap className="h-6 w-6 fill-teal-700 text-teal-700" />
                    </div>,
                  ),
                  className: "bg-none",
                  iconAnchor: [iconSize / 2, iconSize / 2],
                  tooltipAnchor: [0, iconSize / 2],
                })}
              >
                <Tooltip direction="bottom">
                  <p>{ip.n}</p>
                </Tooltip>
              </Marker>
            );
        }
      })}
    </>
  );
}
