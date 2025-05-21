import type { components } from "./types";

type schemas = components["schemas"];

export type InterestPoint =
  | schemas["ClusterInterestPoint"]
  | schemas["LocationInterestPoint"];
