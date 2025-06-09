import { $api } from "@/api/client";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { zodResolver } from "@hookform/resolvers/zod";
import { CirclePlus } from "lucide-react";
import { useState } from "react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { toast } from "sonner";
import z from "zod";
import { Button } from "./ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import { Input } from "./ui/input";

const formSchema = z.object({
  name: z.string().min(1),
  lat: z.coerce
    .number({ message: "Latitude has to be a number" })
    .min(-90, { message: "Latitude must be greater than -90" })
    .max(90, { message: "Latitude must be lesser than 90" }),
  lon: z.coerce
    .number({ message: "Longitude has to be a number" })
    .min(-90, { message: "Longitude must be greater than -180" })
    .max(90, { message: "Longitude must be lesser than 180" }),
});

export default function CreateLocation() {
  const [dialogOpen, setDialogOpen] = useState(false);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: "",
    },
  });

  const { mutate } = $api.useMutation("post", "/api/v1/locations/", {
    onSuccess: (values) => {
      toast.success(
        <div datatest-id="location-creation-success">
          The location {values.name} was created
        </div>,
      );
      setDialogOpen(false);
    },
    onError: (err) => {
      toast.error(`An error has occurred: ${err.title}`);
    },
  });

  const onSubmit: SubmitHandler<z.infer<typeof formSchema>> = (values) => {
    mutate({ body: { ...values } });
  };

  const { data, isError } = $api.useQuery("get", "/api/v1/auth/me");

  if (isError || !data?.admin) return;

  return (
    <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
      <DialogTrigger className="absolute right-20 bottom-20" asChild>
        <Button data-test-id="create-location-button">
          <CirclePlus size={48} />
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create a new Location</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-2"
            data-test-id="create-location-form"
          >
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Location Name</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder="Location Name"
                      data-test-id="location-name-input"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="lat"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Latitude</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder="Latitude"
                      data-test-id="location-latitude-input"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="lon"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Longitude</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder="Longitude"
                      data-test-id="location-longitude-input"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" data-test-id="create-location-submit">
              Submit
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
