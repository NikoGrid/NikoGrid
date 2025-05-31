import { userStore } from "@/store/user";
import { useStore } from "@tanstack/react-store";

export function useUser() {
  const user = useStore(userStore);

  return { user };
}
