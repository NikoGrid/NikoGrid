import { userStore } from "@/store/user";
import { Navigate } from "@tanstack/react-router";

// The lint is wrong because PropsWithChildren calculates a union type so the
// value cannot be any non nullish type.
/* eslint-disable @typescript-eslint/no-empty-object-type */
export function AuthGate({ children }: React.PropsWithChildren<{}>) {
  const user = userStore.state;
  if (!user) return <Navigate to="/login" />;

  return children;
}
