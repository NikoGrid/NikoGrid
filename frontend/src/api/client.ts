import createFetchClient from "openapi-fetch";
import createClient from "openapi-react-query";
import type { paths } from "./types";

const backendBaseUrl = import.meta.env.VITE_BACKEND_BASE_URL;

if (backendBaseUrl === undefined)
  console.warn("No backend base URL has been set, defaulting to current host.");

const fetchClient = createFetchClient<paths>({
  baseUrl: backendBaseUrl,
  credentials: "include",
});

export const $api = createClient(fetchClient);
