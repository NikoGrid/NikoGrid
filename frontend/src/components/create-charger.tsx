import { $api } from "@/api/client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
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
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import { Input } from "./ui/input";

const formSchema = z.object({
  name: z.string().min(1),
  maxPower: z.coerce.number({
    message: "The maximum power of the charger has to be a number",
  }),
});

export default function CreateCharger({ locationId }: { locationId: number }) {
  const [dialogOpen, setDialogOpen] = useState(false);

  const queryClient = useQueryClient();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: "",
      maxPower: 0,
    },
  });

  const { data, isError } = $api.useQuery("get", "/api/v1/auth/me");

  const { mutate } = $api.useMutation(
    "post",
    "/api/v1/locations/{locationId}",
    {
      onSuccess: (values) => {
        toast.success(
          <div data-test-id="charger-creation-success">
            The charger {values.name} was created
          </div>,
        );
        setDialogOpen(false);
        queryClient.invalidateQueries({
          queryKey: [
            "get",
            "/api/v1/locations/{id}",
            {
              params: {
                path: {
                  id: locationId,
                },
              },
            },
          ],
        });
      },
      onError: (err) => {
        toast.error(`An error has occurred: ${err.title}`);
      },
    },
  );

  const onSubmit: SubmitHandler<z.infer<typeof formSchema>> = (values) => {
    mutate({ params: { path: { locationId } }, body: { ...values } });
  };

  if (isError || !data?.admin) return;

  return (
    <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
      <DialogTrigger className="mx-6 mt-auto mb-10" asChild>
        <Button data-test-id="create-charger-button">
          Create a new Station
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create a new Station</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-2"
            data-test-id="create-charger-form"
          >
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Station Name</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder="Station Name"
                      data-test-id="charger-name-input"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="maxPower"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Maximum Charging Power</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      placeholder="Maximum Power"
                      data-test-id="charger-power-input"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" data-test-id="create-charger-submit">
              Submit
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
