import { mediaQueryHelpers, useMediaQuery } from "@/hooks/use-media-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";

export default function Devtools() {
  const isDesktop = useMediaQuery(mediaQueryHelpers.minWidth("48rem"));
  if (!isDesktop) return null;
  return (
    <>
      <TanStackRouterDevtools />
      <ReactQueryDevtools />
    </>
  );
}
