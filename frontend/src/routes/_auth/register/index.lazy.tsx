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
import { createLazyFileRoute, useRouter } from "@tanstack/react-router";
import { useForm, type SubmitHandler } from "react-hook-form";
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

  const router = useRouter();
  const { redirect } = Route.useSearch();
  const submitHandler: SubmitHandler<FormSchema> = (data) => {
    console.log(data);
    router.history.replace(redirect ?? "/");
  };
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(submitHandler)} className="space-y-4">
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
        <Button data-test-id="register-submit-button" type="submit">
          Register
        </Button>
      </form>
    </Form>
  );
}
