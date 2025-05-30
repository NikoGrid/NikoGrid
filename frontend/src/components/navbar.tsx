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

function NoAuth({ className, ...props }: ComponentProps<"section">) {
  return (
    <section className={cn("", className)} {...props}>
      <Link
        data-test-id="register-link"
        to="/register"
        className={buttonVariants()}
      >
        Register
      </Link>
    </section>
  );
}

function Logo() {
  return (
    <p className="text-4xl font-extrabold text-fuchsia-800/90">NikoGrid</p>
  );
}
