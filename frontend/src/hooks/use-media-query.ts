import { useEffect, useState } from "react";

export const mediaQueryHelpers = {
  maxWidth: (value: string) => `(max-width: ${value})`,
  minWidth: (value: string) => `(min-width: ${value})`,
};

export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(
    () => window.matchMedia(query).matches,
  );

  useEffect(() => {
    const mediaQueryList = window.matchMedia(query);

    const updateMatch = (event: MediaQueryListEvent) => {
      setMatches(event.matches);
    };

    setMatches(mediaQueryList.matches);

    mediaQueryList.addEventListener("change", updateMatch);

    return () => mediaQueryList.removeEventListener("change", updateMatch);
  }, [query]);

  return matches;
}
