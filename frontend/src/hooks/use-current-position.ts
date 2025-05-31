import { useEffect, useState } from "react";

export function useCurrentPosition() {
  const [location, setLocation] = useState<GeolocationPosition | null>(null);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        function (position) {
          setLocation(position);
        },
        function (error) {
          console.error("Error occurred. Error message: " + error.message);
        },
      );
    } else {
      console.warn("Geolocation is not supported by this browser.");
    }
  }, []);

  return { location };
}
