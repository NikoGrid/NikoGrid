import { $api } from "@/api/client";
import ReservationCard from "@/components/reservation-card";
import { createLazyFileRoute } from "@tanstack/react-router";

export const Route = createLazyFileRoute("/profile/")({
  component: RouteComponent,
});

function RouteComponent() {
  const { data, isSuccess, isError } = $api.useQuery(
    "get",
    "/api/v1/reservations/",
  );

  if (isSuccess) {
    return (
      <div className="m-8 flex w-max flex-col gap-4">
        <h1 className="text-xl font-bold">My reservations</h1>
        {data.length === 0 ? (
          <p>No reservations found</p>
        ) : (
          data.map((res) => {
            return <ReservationCard {...res} key={res.id} />;
          })
        )}
      </div>
    );
  }

  if (isError) {
    return (
      <div className="m-8 flex w-max flex-col gap-4">
        <p className="text-xl font-bold">
          An error has occured while getting your reservations
        </p>
      </div>
    );
  }

  return (
    <div className="m-8 flex w-max flex-col gap-4">
      <p className="text-xl font-bold">Loading reservations...</p>
    </div>
  );
}
