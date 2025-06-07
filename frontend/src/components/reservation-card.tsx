import { cn } from "@/lib/utils";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";

interface ReservationCardProps {
  charger: string;
  maxPower: number;
  location: string;
  start: string;
  end: string;
}

export default function ReservationCard({
  charger,
  maxPower,
  location,
  start,
  end,
}: ReservationCardProps) {
  const isPast = new Date(start) <= new Date();
  const dataFormat = new Intl.DateTimeFormat("en-GB", {
    dateStyle: "short",
    timeStyle: "short",
  });
  return (
    <Card
      className={cn(isPast && "bg-gray-100 text-gray-700")}
      data-test-group="reservation-card"
    >
      <CardHeader>
        <CardTitle>{charger}</CardTitle>
        <CardDescription>{location}</CardDescription>
      </CardHeader>
      <CardContent className="grid grid-cols-2">
        <p className="font-bold">From: </p>
        <p data-test-id="reservation-start-instant">
          {dataFormat.format(new Date(start))}
        </p>
        <p className="font-bold">To: </p>
        <p>{dataFormat.format(new Date(end))}</p>
        <p className="font-bold">Power: </p>
        <p>{maxPower} kWh</p>
      </CardContent>
    </Card>
  );
}
