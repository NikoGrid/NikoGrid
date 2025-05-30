import { createLazyFileRoute } from "@tanstack/react-router";

export const Route = createLazyFileRoute("/_auth/login/")({
  component: RouteComponent,
});

function RouteComponent() {
  return <div data-test-id="login-page"></div>;
}
