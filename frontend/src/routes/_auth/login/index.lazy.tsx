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
import { userConfig } from "@/configs/user";
import { refreshUser } from "@/store/user";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import { createLazyFileRoute, Link, useRouter } from "@tanstack/react-router";
import { LoaderCircle } from "lucide-react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

export const Route = createLazyFileRoute("/_auth/login/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <main className="container mx-auto my-20 px-4" data-test-id="login-page">
      <Card>
        <CardHeader>
          <CardTitle>Log into account</CardTitle>
          <CardDescription>Welcome back to NikoGrid</CardDescription>
        </CardHeader>
        <CardContent>
          <RegisterForm />
        </CardContent>
      </Card>
    </main>
  );
}

const formSchema = z.object({
  email: z
    .string()
    .min(1, "Email can't be empty")
    .email("Invalid email format"),
  password: z
    .string()
    .min(1, "Password can't be empty")
    .min(
      userConfig.password.minLength,
      `Password must be at least ${userConfig.password.minLength} characters long`,
    ),
});

type FormSchema = z.infer<typeof formSchema>;

export default function RegisterForm() {
  const form = useForm<FormSchema>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const router = useRouter();
  const { redirect } = Route.useSearch();

  const queryClient = useQueryClient();

  const { mutate, isPending } = $api.useMutation("post", "/api/v1/auth/login", {
    onSuccess() {
      toast.success("Logged in successfully");
      router.history.replace(redirect ?? "/");
      refreshUser();
      queryClient.clear();
    },
    onError(error) {
      switch (error.status) {
        case 401:
          toast.error("Email or password are incorrect.");
          break;
        case 403:
          toast.error("Can't login while logged in.");
          break;
        default:
          toast.error("An unknown error has occurred. Try again later.");
          break;
      }
    },
  });
  const submitHandler: SubmitHandler<FormSchema> = (data) => {
    mutate({ body: data });
  };
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(submitHandler)}
        className="space-y-4"
        data-test-id="login-form"
      >
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input
                  data-test-id="login-email"
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
                  data-test-id="login-password"
                  type="password"
                  placeholder="Password"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <div>
          <Button
            data-test-id="login-submit-button"
            disabled={isPending}
            type="submit"
          >
            Login
            {isPending && <LoaderCircle className="animate-spin" />}
          </Button>
          <p className="text-sm">
            Don't have an account yet?{" "}
            <Link to="/register" className="cursor-pointer underline">
              Register here.
            </Link>
          </p>
        </div>
      </form>
    </Form>
  );
}
