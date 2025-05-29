import createFetchClient from "openapi-fetch";
import createClient from "openapi-react-query";
import type { paths } from "./types";

const fetchClient = createFetchClient<paths>({
  baseUrl: import.meta.env.VITE_BACKEND_BASE_URL,
});

export const $api = createClient(fetchClient);
