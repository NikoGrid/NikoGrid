import Devtools from "@/utils/devtools";
import { HeadContent, Outlet, createRootRoute } from "@tanstack/react-router";

export const Route = createRootRoute({
  component: RootComponent,
  head: () => ({
    meta: [
      { title: "NikoGrid" },
      {
        name: "description",
        content: "The perfect place for all your charging needs",
      },
    ],
    links: [{ rel: "icon", href: "" }],
  }),
  notFoundComponent: () => "404 Not Found",
});

function RootComponent() {
  return (
    <>
      <HeadContent />
      <div>Hello "__root"!</div>
      <Outlet />
      <Devtools />
    </>
  );
}
