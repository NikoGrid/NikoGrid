import NavBar from "@/components/navbar";
import { Toaster } from "@/components/ui/sonner";
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
    links: [
      { rel: "icon", href: "favicon.png" },
      { rel: "preconnect", href: "https://a.tile.openstreetmap.org" },
      { rel: "preconnect", href: "https://b.tile.openstreetmap.org" },
      { rel: "preconnect", href: "https://c.tile.openstreetmap.org" },
    ],
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
      <Toaster richColors />
      <Devtools />
      <Toaster />
    </>
  );
}
