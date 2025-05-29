import { createFileRoute, redirect } from "@tanstack/react-router";
import { z } from "zod";

const stationSchema = z.object({
  stationId: z.coerce.number().int().gt(0),
});

export const Route = createFileRoute("/station/$stationId/")({
  component: RouteComponent,
  params: { parse: stationSchema.parse },
  onError(error) {
    if (error?.routerCode === "PARSE_PARAMS") {
      throw redirect({ to: "/" });
    }
  },
});

function RouteComponent() {
  return <div></div>;
}
