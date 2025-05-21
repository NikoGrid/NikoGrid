import { useEffect, useState } from "react";

export function useCurrentPosition() {
  const [location, setLocation] = useState<GeolocationPosition | null>(null);
  const [locationAvailable, setLocationAvailable] = useState(true);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        function (position) {
          setLocation(position);
        },
        function (error) {
          setLocationAvailable(false);
          console.error("Error occurred. Error message: " + error.message);
        },
      );
    } else {
      setLocationAvailable(false);
      console.log("Geolocation is not supported by this browser.");
    }
  }, []);

  return { location, locationAvailable };
}
