import { $api } from "@/api/client";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { zodResolver } from "@hookform/resolvers/zod";
import { createLazyFileRoute } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

export const Route = createLazyFileRoute("/_auth/register/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <main className="container mx-auto my-20 px-4">
      <Card>
        <CardHeader>
          <CardTitle>Create an account</CardTitle>
          <CardDescription>Make a new account at NikoGrid</CardDescription>
        </CardHeader>
        <CardContent>
          <RegisterForm />
        </CardContent>
      </Card>
    </main>
  );
}

const formSchema = z
  .object({
    email: z
      .string()
      .min(1, "Email can't be empty")
      .email("Invalid email format"),
    password: z
      .string()
      .min(1, "Password can't be empty")
      .min(6, "Password must be at least 6 characters long"),
    confirmPassword: z.string().min(1, "Confirm password can't be empty"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type FormSchema = z.infer<typeof formSchema>;

export default function RegisterForm() {
  const form = useForm<FormSchema>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const { redirect: redirctUrl } = Route.useSearch();
  const navigate = Route.useNavigate();

  const { mutate, isPending } = $api.useMutation(
    "post",
    "/api/v1/auth/register",
    {
      onSuccess() {
        toast.success("Account created successfully");
        navigate({
          to: "/login",
          search: { redirect: redirctUrl },
          replace: true,
        });
      },
      onError(error) {
        switch (error.status) {
          case 409:
            toast.error("Email already taken. Please use another.");
            break;
          case 403:
            toast.error("Cannot create account while logged in.");
            break;
          default:
            toast.error("An unknown error has occured. Try again later.");
        }
      },
    },
  );

  const submitHandler: SubmitHandler<FormSchema> = ({ email, password }) => {
    mutate({ body: { email, password } });
  };
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(submitHandler)}
        className="space-y-4"
        data-test-id="register-form"
      >
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input
                  data-test-id="register-email"
                  placeholder="jane@example.com"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Password</FormLabel>
              <FormControl>
                <Input
                  data-test-id="register-password"
                  type="password"
                  placeholder="Password"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="confirmPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Confirm Password</FormLabel>
              <FormControl>
                <Input
                  data-test-id="register-confirm-password"
                  type="password"
                  placeholder="Confirm Password"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <Button
          disabled={isPending}
          data-test-id="register-submit-button"
          type="submit"
        >
          Register
          {isPending && <LoaderCircle className="animate-spin" />}
        </Button>
      </form>
    </Form>
  );
}
