import { userStore } from "@/store/user";
import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { zodValidator } from "@tanstack/zod-adapter";
import { z } from "zod";

const searchSchema = z.object({
  redirect: z.string().optional(),
});

export const Route = createFileRoute("/_auth")({
  validateSearch: zodValidator(searchSchema),
  component: Outlet,
  beforeLoad() {
    const user = userStore.state;
    if (user !== null) throw redirect({ to: "/" });
  },
});
