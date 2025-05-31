import { Store } from "@tanstack/react-store";
import { jwtDecode } from "jwt-decode";

export interface User {
  email: string;
}

export const userStore = new Store<User | null>(null);

function getCookie(cname: string) {
  const name = cname + "=";
  const decodedCookie = decodeURIComponent(document.cookie);
  const ca = decodedCookie.split(";");
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == " ") {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

interface Payload {
  sub: string;
}

export function refreshUser() {
  const token = getCookie("AUTH");
  if (token.length === 0) {
    userStore.setState(() => null);
    return;
  }

  let email: string;
  try {
    const { sub } = jwtDecode<Payload>(token);
    email = sub;
  } catch {
    userStore.setState(() => null);
    return;
  }

  userStore.setState(() => {
    return { email };
  });
}
