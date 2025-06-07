import { $api } from "@/api/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { createLazyFileRoute } from "@tanstack/react-router";
import type { DateTimeFormatOptions } from "luxon";

export const Route = createLazyFileRoute("/profile/")({
  component: RouteComponent,
});

function RouteComponent() {
  const { data } = $api.useQuery("get", "/api/v1/reservations/");
  return (
    <div className="m-8 flex w-max flex-col gap-4">
      <h1 className="text-xl font-bold">My reservations</h1>
      {data &&
        data.map((res) => {
          const isPast = new Date(res.start) <= new Date();
          const dataFormat: DateTimeFormatOptions = {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          };
          return (
            <Card
              className={cn(isPast ? "bg-gray-100 text-gray-700" : "")}
              data-test-group="reservation-card"
            >
              <CardHeader>
                <CardTitle>
                  {res.charger},<br /> {res.location}
                </CardTitle>
              </CardHeader>
              <CardContent className="grid grid-cols-2">
                <p className="font-bold">From: </p>
                <p data-test-id="reservation-start-instant">
                  {new Date(res.start).toLocaleString("en-GB", dataFormat)}
                </p>
                <p className="font-bold">To: </p>
                <p>{new Date(res.end).toLocaleString("en-GB", dataFormat)}</p>
                <p className="font-bold">Power: </p>
                <p>{res.maxPower} kWh</p>
              </CardContent>
            </Card>
          );
        })}
    </div>
  );
}
