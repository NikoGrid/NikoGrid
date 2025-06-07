import { $api } from "@/api/client";
import { cn } from "@/lib/utils";
import { useQueryClient } from "@tanstack/react-query";
import { Trash2 } from "lucide-react";
import { toast } from "sonner";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "./ui/alert-dialog";
import { Button } from "./ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";

interface ReservationCardProps {
  id: number;
  charger: string;
  maxPower: number;
  location: string;
  start: string;
  end: string;
}

const dateFormat = new Intl.DateTimeFormat("en-GB", {
  dateStyle: "short",
  timeStyle: "short",
});

export default function ReservationCard({
  id,
  charger,
  maxPower,
  location,
  start,
  end,
}: ReservationCardProps) {
  const isPast = new Date(start) <= new Date();

  const queryClient = useQueryClient();
  const { mutate } = $api.useMutation(
    "delete",
    "/api/v1/reservations/{reservationId}",
    {
      async onSuccess() {
        toast.success(
          <span data-test-group="success">
            Cancelled reservation successfully
          </span>,
        );

        const queryKey = $api.queryOptions(
          "get",
          "/api/v1/reservations/",
        ).queryKey;
        await queryClient.invalidateQueries({ queryKey });
      },
    },
  );

  return (
    <Card
      className={cn(isPast && "bg-gray-100 text-gray-700")}
      data-test-group="reservation-card"
    >
      <CardHeader className="flex justify-between">
        <section>
          <CardTitle>{charger}</CardTitle>
          <CardDescription>{location}</CardDescription>
        </section>
        <AlertDialog>
          <Tooltip>
            <AlertDialogTrigger asChild>
              <TooltipTrigger asChild>
                <Button
                  variant="destructive"
                  size="icon"
                  data-test-id="cancel-button"
                >
                  <Trash2 />
                  <span className="sr-only">Cancel reservation</span>
                </Button>
              </TooltipTrigger>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Are you sure?</AlertDialogTitle>
                <AlertDialogDescription>
                  This action cannot be undone.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>No</AlertDialogCancel>
                <AlertDialogAction
                  data-test-id="confirm-cancel-button"
                  onClick={() => {
                    mutate({ params: { path: { reservationId: id } } });
                  }}
                >
                  Yes
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
            <TooltipContent>Cancel reservation</TooltipContent>
          </Tooltip>
        </AlertDialog>
      </CardHeader>
      <CardContent className="grid grid-cols-2">
        <p className="font-bold">From: </p>
        <p data-test-id="reservation-start-instant">
          {dateFormat.format(new Date(start))}
        </p>
        <p className="font-bold">To: </p>
        <p>{dateFormat.format(new Date(end))}</p>
        <p className="font-bold">Power: </p>
        <p>{maxPower} kWh</p>
      </CardContent>
    </Card>
  );
}
