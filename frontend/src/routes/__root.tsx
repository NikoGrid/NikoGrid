import NavBar from "@/components/navbar";
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
    links: [{ rel: "icon", href: "favicon.png" }],
  }),
  notFoundComponent: () => "404 Not Found",
});

function RootComponent() {
  return (
    <>
      <HeadContent />
      <div className="flex min-h-screen flex-col">
        <NavBar />
        <Outlet />
      </div>
      <Devtools />
    </>
  );
}
