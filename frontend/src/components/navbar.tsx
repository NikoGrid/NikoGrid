import { cn } from "@/lib/utils";
import { Link } from "@tanstack/react-router";
import type { ComponentProps } from "react";
import { buttonVariants } from "./ui/button";

export default function NavBar() {
  return (
    <header className="flex items-center justify-between border-b px-8 py-4 shadow-md">
      <nav>
        <Link to="/">
          <Logo />
        </Link>
      </nav>
      <NoAuth />
    </header>
  );
}

function Auth() {
  return <p></p>;
}

function NoAuth({ className, ...props }: ComponentProps<"section">) {
  return (
    <section className={cn("", className)} {...props}>
      <Link to="/login" className={buttonVariants()}>
        Login
      </Link>
    </section>
  );
}

function Logo() {
  return (
    <p className="text-4xl font-extrabold text-fuchsia-800/90">NikoGrid</p>
  );
}
