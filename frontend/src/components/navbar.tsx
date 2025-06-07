import { $api } from "@/api/client";
import { mediaQueryHelpers, useMediaQuery } from "@/hooks/use-media-query";
import { useUser } from "@/hooks/use-user";
import { cn } from "@/lib/utils";
import { refreshUser, type User } from "@/store/user";
import { useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate } from "@tanstack/react-router";
import { Menu } from "lucide-react";
import type { ComponentProps } from "react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "./ui/alert-dialog";
import { Button, buttonVariants } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { Sheet, SheetContent, SheetHeader, SheetTrigger } from "./ui/sheet";

export default function NavBar() {
  return (
    <header className="flex items-center justify-between border-b px-8 py-4 shadow-md">
      <nav>
        <Link to="/">
          <Logo />
        </Link>
      </nav>
      <AuthSection></AuthSection>
    </header>
  );
}

function AuthSection() {
  const { user } = useUser();
  const isDesktop = useMediaQuery(mediaQueryHelpers.minWidth("48rem"));
  if (isDesktop)
    return <>{user === null ? <NoAuth /> : <Auth user={user} />}</>;

  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button>
          <Menu />
          <span className="sr-only">Navigation menu</span>
        </Button>
      </SheetTrigger>
      <SheetContent>
        <SheetHeader />
        <section className="px-4">
          {user === null ? (
            <NoAuth className="grid gap-2 space-x-0" />
          ) : (
            <Auth user={user} />
          )}
        </section>
      </SheetContent>
    </Sheet>
  );
}

interface AuthProps {
  user: User;
}

function Auth({ user }: AuthProps) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { mutate } = $api.useMutation("get", "/api/v1/auth/logout", {
    onSuccess() {
      refreshUser();
      queryClient.clear();
      navigate({ to: "/" });
    },
  });
  return (
    <AlertDialog>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="link" data-test-id="profile-menu-trigger">
            {user.email}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuLabel className="text-center">Account</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem asChild>
            <Link
              to="/profile"
              className={cn(buttonVariants({ variant: "link" }))}
              data-test-id="go-to-profile"
            >
              Go To Profile
            </Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <AlertDialogTrigger asChild>
              <Button className="w-full" variant="destructive">
                Logout
              </Button>
            </AlertDialogTrigger>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Are you sure you want to logout?</AlertDialogTitle>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>No</AlertDialogCancel>
          <AlertDialogAction onClick={() => mutate({})}>Yes</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

function NoAuth({ className, ...props }: ComponentProps<"section">) {
  return (
    <section className={cn("space-x-4", className)} {...props}>
      <Link
        data-test-id="register-link"
        to="/register"
        className={buttonVariants()}
      >
        Register
      </Link>
      <Link data-test-id="login-link" to="/login" className={buttonVariants()}>
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
