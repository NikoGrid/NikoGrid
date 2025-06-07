import { Store, useStore } from "@tanstack/react-store";

interface Coords {
  lat: number;
  lng: number;
}

const locationStore = new Store<Coords | null>(null);

export function useCurrentPosition() {
  const location = useStore(locationStore);

  const requestUserLocation = () => {
    if (location !== null) return;
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        function (position) {
          locationStore.setState({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          });
        },
        function (error) {
          console.error("Error occurred. Error message: " + error.message);
        },
      );
    } else {
      console.warn("Geolocation is not supported by this browser.");
    }
  };

  return { location, requestUserLocation };
}
