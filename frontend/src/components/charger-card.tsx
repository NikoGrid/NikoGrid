import { zodResolver } from "@hookform/resolvers/zod";
import { DateTime } from "luxon";
import { useForm, type SubmitHandler } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { $api } from "@/api/client";
import type { components } from "@/api/types";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { LoaderCircle } from "lucide-react";
import { useMemo, useState } from "react";
import { AuthGate } from "./auth-gate";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import { Input } from "./ui/input";

const formSchema = z.object({
  start: z.string().datetime({ local: true }),
  end: z.string().datetime({ local: true }),
});

type FormSchema = z.infer<typeof formSchema>;

export interface BookingDialogProps {
  charger: components["schemas"]["ChargerDTO"];
  closeDialog: () => void;
}

function BookingDialog({ charger, closeDialog }: BookingDialogProps) {
  const timeZone = useMemo(
    () => Intl.DateTimeFormat().resolvedOptions().timeZone,
    [],
  );
  const form = useForm<FormSchema>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      start: "",
      end: "",
    },
  });

  const { mutate, isPending } = $api.useMutation(
    "post",
    "/api/v1/reservations/",
    {
      onSuccess() {
        toast.success(
          <div data-test-id="reservation-success">
            Reservation created successfully
          </div>,
        );
        closeDialog();
      },
      onError(error) {
        switch (error.status) {
          case 400:
            toast.error(error.detail);
            break;
          case 409:
            toast.error(
              "Reservation overlaps with existing reservation for charger",
            );
            break;
          default:
            toast.error("An unknown error has occurred. Try again later.");
            break;
        }
      },
    },
  );

  const submitHandler: SubmitHandler<FormSchema> = (data) => {
    mutate({
      body: {
        chargedId: charger.id,
        start: DateTime.fromISO(data.start, { zone: timeZone }).toISO()!,
        end: DateTime.fromISO(data.end, { zone: timeZone }).toISO()!,
      },
    });
  };

  return (
    <AuthGate>
      <DialogHeader>
        <DialogTitle>Reserve the charger</DialogTitle>
        <DialogDescription>
          Reserve the charger <span className="italic">{charger.name}</span> to
          use it later. The time is based on the{" "}
          <span className="italic">{timeZone}</span> time zone (your current
          time zone).
        </DialogDescription>
      </DialogHeader>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(submitHandler)}
          className="space-y-4"
          data-test-id="book-form"
        >
          <FormField
            control={form.control}
            name="start"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Start</FormLabel>
                <FormControl>
                  <Input
                    data-test-id="book-start"
                    type="datetime-local"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="end"
            render={({ field }) => (
              <FormItem>
                <FormLabel>End</FormLabel>
                <FormControl>
                  <Input
                    data-test-id="book-end"
                    type="datetime-local"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline" type="reset">
                Cancel
              </Button>
            </DialogClose>
            <Button
              data-test-id="book-submit"
              type="submit"
              disabled={isPending}
            >
              Book slot
              {isPending && <LoaderCircle className="animate-spin" />}
            </Button>
          </DialogFooter>
        </form>
      </Form>
    </AuthGate>
  );
}

export interface ChargerCardProps {
  charger: components["schemas"]["ChargerDTO"];
}

export function ChargerCard({ charger }: ChargerCardProps) {
  const [open, setOpen] = useState(false);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <div className="flex flex-col gap-6 rounded-xl border px-6 py-4 shadow-sm">
        <div className="flex justify-between">
          <h1 className="font-semibold">{charger.name}</h1>
          {charger.isAvailable ? (
            <Badge variant="secondary" className="bg-green-500 text-white">
              Operational
            </Badge>
          ) : (
            <Badge variant="destructive">Unavailable</Badge>
          )}
        </div>
        <p>Max charging rate: {charger.maxPower.toFixed(0)} kWh</p>
        {charger.isAvailable && (
          <DialogTrigger asChild>
            <Button variant="outline" data-test-id="book-charger-btn">
              Book a slot
            </Button>
          </DialogTrigger>
        )}
      </div>
      <DialogContent data-test-id="reservation-dialog">
        <BookingDialog charger={charger} closeDialog={() => setOpen(false)} />
      </DialogContent>
    </Dialog>
  );
}
