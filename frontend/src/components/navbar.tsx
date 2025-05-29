import { Link } from "@tanstack/react-router";

export default function NavBar() {
  return (
    <header className="flex items-center justify-between border-b px-8 py-4 shadow-md">
      <nav>
        <Link to="/">
          <Logo />
        </Link>
      </nav>
      <Auth />
    </header>
  );
}

function Auth() {
  return <p></p>;
}

function Logo() {
  return (
    <p className="text-4xl font-extrabold text-fuchsia-800/90">NikoGrid</p>
  );
}
